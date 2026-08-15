package com.bradmir.pescapr.data.contribution

enum class DatasetCandidateIneligibilityReason {
    CONSENT_REFERENCE_INVALID,
    CONSENT_VERSION_INVALID,
    OWNERSHIP_NOT_CONFIRMED,
    ML_TRAINING_NOT_ALLOWED,
    RIGHTS_NOT_APPROVED,
    PRIVACY_NOT_APPROVED,
    PRIVACY_UNRESOLVED,
    LABEL_NOT_APPROVED,
    APPROVED_LABEL_MISSING_OR_UNKNOWN,
    QUALITY_NOT_APPROVED,
    NOT_DATASET_APPROVED,
    CONTRIBUTION_REJECTED,
    CONTRIBUTION_EXCLUDED,
    CONTRIBUTION_WITHDRAWN,
    MISSING_SANITIZED_TRAINING_ASSET,
    SANITIZED_TRAINING_ASSET_UNAVAILABLE,
    TRAINING_ASSET_NOT_ELIGIBLE,
    PROVENANCE_OR_CONTENT_INVARIANT_INVALID
}

data class DatasetCandidate(
    val contributionId: ContributionId,
    val trainingAssetId: ControlledAssetId,
    val approvedFichaPezId: String,
    val evaluatedContributionRevision: Long,
    val contentSha256: String
)

sealed interface DatasetCandidateEvaluation {
    val contributionId: ContributionId

    data class Eligible(val candidate: DatasetCandidate) : DatasetCandidateEvaluation {
        override val contributionId: ContributionId = candidate.contributionId
    }

    data class Ineligible(
        override val contributionId: ContributionId,
        val reasons: Set<DatasetCandidateIneligibilityReason>
    ) : DatasetCandidateEvaluation
}

/** Evaluates current eligibility for future planning; it does not create snapshot membership. */
class DatasetCandidateEligibilityEvaluator(
    private val validFichaPezIds: Set<String>,
    private val consentRegistry: ConsentVersionRegistry
) {
    fun evaluate(
        aggregate: ModerationAggregate,
        sanitizedAssetMetadata: ControlledAssetMetadata?
    ): DatasetCandidateEvaluation {
        val contributionId = ContributionId(aggregate.submission.submissionId)
        val reasons = linkedSetOf<DatasetCandidateIneligibilityReason>()
        val asset = aggregate.trainingAsset

        reasons += FishTrainingContributionRules.submissionErrors(
            aggregate.submission, aggregate.consent, validFichaPezIds
        ).map { mapValidationError(it, aggregate.submission) }
        reasons += FishTrainingContributionRules.consentRegistryErrors(
            aggregate.consent, consentRegistry
        ).map { mapValidationError(it, aggregate.submission) }

        when (aggregate.submission.status) {
            ContributionStatus.REJECTED -> reasons += DatasetCandidateIneligibilityReason.CONTRIBUTION_REJECTED
            ContributionStatus.EXCLUDED -> reasons += DatasetCandidateIneligibilityReason.CONTRIBUTION_EXCLUDED
            ContributionStatus.WITHDRAWN, ContributionStatus.WITHDRAWAL_PENDING ->
                reasons += DatasetCandidateIneligibilityReason.CONTRIBUTION_WITHDRAWN
            else -> Unit
        }

        if (asset == null || sanitizedAssetMetadata == null) {
            reasons += DatasetCandidateIneligibilityReason.MISSING_SANITIZED_TRAINING_ASSET
        } else {
            reasons += FishTrainingContributionRules.trainingAssetErrors(
                asset, aggregate.submission, aggregate.consent, validFichaPezIds
            ).map { mapValidationError(it, aggregate.submission) }
            if (asset.eligibilityState != TrainingEligibilityState.ELIGIBLE) {
                reasons += DatasetCandidateIneligibilityReason.TRAINING_ASSET_NOT_ELIGIBLE
            }
            if (sanitizedAssetMetadata.availability != ControlledAssetAvailability.AVAILABLE) {
                reasons += DatasetCandidateIneligibilityReason.SANITIZED_TRAINING_ASSET_UNAVAILABLE
            }
            if (sanitizedAssetMetadata.kind != ControlledAssetKind.SANITIZED_TRAINING ||
                sanitizedAssetMetadata.assetId.value != asset.trainingAssetId ||
                sanitizedAssetMetadata.sourceSubmissionId != contributionId ||
                sanitizedAssetMetadata.contentSha256 != asset.sha256
            ) reasons += DatasetCandidateIneligibilityReason.PROVENANCE_OR_CONTENT_INVARIANT_INVALID
        }

        if (reasons.isNotEmpty()) {
            return DatasetCandidateEvaluation.Ineligible(contributionId, reasons)
        }
        return DatasetCandidateEvaluation.Eligible(
            DatasetCandidate(
                contributionId = contributionId,
                trainingAssetId = ControlledAssetId(asset!!.trainingAssetId),
                approvedFichaPezId = aggregate.submission.approvedFichaPezId!!,
                evaluatedContributionRevision = aggregate.revision,
                contentSha256 = asset.sha256
            )
        )
    }

    private fun mapValidationError(
        error: ContributionValidationError,
        submission: ContributionSubmission
    ): DatasetCandidateIneligibilityReason = when (error) {
        ContributionValidationError.MISSING_CONSENT_REFERENCE,
        ContributionValidationError.CONSENT_REFERENCE_MISMATCH ->
            DatasetCandidateIneligibilityReason.CONSENT_REFERENCE_INVALID
        ContributionValidationError.CONSENT_VERSION_NOT_FOUND,
        ContributionValidationError.CONSENT_VERSION_NOT_VALID_AT_ACCEPTANCE,
        ContributionValidationError.CONSENT_LOCALE_MISMATCH ->
            DatasetCandidateIneligibilityReason.CONSENT_VERSION_INVALID
        ContributionValidationError.OWNERSHIP_NOT_CONFIRMED ->
            DatasetCandidateIneligibilityReason.OWNERSHIP_NOT_CONFIRMED
        ContributionValidationError.ML_TRAINING_NOT_ALLOWED ->
            DatasetCandidateIneligibilityReason.ML_TRAINING_NOT_ALLOWED
        ContributionValidationError.CONSENT_WITHDRAWN ->
            DatasetCandidateIneligibilityReason.CONTRIBUTION_WITHDRAWN
        ContributionValidationError.SUBMISSION_WITHDRAWN_OR_EXCLUDED ->
            if (submission.status in setOf(
                    ContributionStatus.WITHDRAWN, ContributionStatus.WITHDRAWAL_PENDING
                ) || submission.withdrawnAtEpochMillis != null
            ) DatasetCandidateIneligibilityReason.CONTRIBUTION_WITHDRAWN
            else DatasetCandidateIneligibilityReason.CONTRIBUTION_EXCLUDED
        ContributionValidationError.SUBMISSION_NOT_APPROVED ->
            DatasetCandidateIneligibilityReason.NOT_DATASET_APPROVED
        ContributionValidationError.RIGHTS_NOT_APPROVED ->
            DatasetCandidateIneligibilityReason.RIGHTS_NOT_APPROVED
        ContributionValidationError.PRIVACY_NOT_APPROVED ->
            DatasetCandidateIneligibilityReason.PRIVACY_NOT_APPROVED
        ContributionValidationError.UNRESOLVED_PRIVACY_FINDING ->
            DatasetCandidateIneligibilityReason.PRIVACY_UNRESOLVED
        ContributionValidationError.LABEL_NOT_APPROVED ->
            DatasetCandidateIneligibilityReason.LABEL_NOT_APPROVED
        ContributionValidationError.QUALITY_NOT_APPROVED ->
            DatasetCandidateIneligibilityReason.QUALITY_NOT_APPROVED
        ContributionValidationError.MISSING_APPROVED_FICHA_PEZ_ID,
        ContributionValidationError.UNKNOWN_FICHA_PEZ_ID ->
            DatasetCandidateIneligibilityReason.APPROVED_LABEL_MISSING_OR_UNKNOWN
        ContributionValidationError.ASSET_SUBMISSION_MISMATCH,
        ContributionValidationError.ASSET_LABEL_MISMATCH,
        ContributionValidationError.INVALID_SHA256,
        ContributionValidationError.MISSING_GROUP_ID,
        ContributionValidationError.ELIGIBLE_ASSET_HAS_EXCLUSION ->
            DatasetCandidateIneligibilityReason.PROVENANCE_OR_CONTENT_INVARIANT_INVALID
    }
}

data class DatasetSnapshotPlan(
    val planId: String,
    val candidates: List<DatasetCandidate>,
    val exclusions: List<DatasetCandidateEvaluation.Ineligible>
)

enum class DatasetSnapshotPlanningError {
    INVALID_PLAN_ID,
    INVALID_CANDIDATE,
    DUPLICATE_CONTRIBUTION_CANDIDATE,
    CONFLICTING_TRAINING_ASSET_IDENTITY
}

sealed interface DatasetSnapshotPlanningResult {
    data class Planned(val plan: DatasetSnapshotPlan) : DatasetSnapshotPlanningResult
    data class Rejected(val errors: Set<DatasetSnapshotPlanningError>) : DatasetSnapshotPlanningResult
}

/** Deterministic in-memory planning only. It performs no snapshot creation or export. */
class DatasetSnapshotPlanner {
    fun plan(
        planId: String,
        evaluations: Iterable<DatasetCandidateEvaluation>
    ): DatasetSnapshotPlanningResult {
        val copied = evaluations.toList()
        val candidates = copied.filterIsInstance<DatasetCandidateEvaluation.Eligible>()
            .map { it.candidate }
        val errors = linkedSetOf<DatasetSnapshotPlanningError>()
        if (planId.isBlank()) errors += DatasetSnapshotPlanningError.INVALID_PLAN_ID
        if (candidates.any(::isInvalid)) errors += DatasetSnapshotPlanningError.INVALID_CANDIDATE
        if (candidates.groupBy { it.contributionId }.any { it.value.size > 1 }) {
            errors += DatasetSnapshotPlanningError.DUPLICATE_CONTRIBUTION_CANDIDATE
        }
        if (candidates.groupBy { it.trainingAssetId }.any { entry ->
                entry.value.map { it.contributionId }.distinct().size > 1
            }
        ) errors += DatasetSnapshotPlanningError.CONFLICTING_TRAINING_ASSET_IDENTITY
        if (errors.isNotEmpty()) return DatasetSnapshotPlanningResult.Rejected(errors)

        return DatasetSnapshotPlanningResult.Planned(
            DatasetSnapshotPlan(
                planId = planId,
                candidates = candidates.sortedWith(compareBy(
                    { it.contributionId.value }, { it.trainingAssetId.value }
                )).toList(),
                exclusions = copied.filterIsInstance<DatasetCandidateEvaluation.Ineligible>()
                    .sortedBy { it.contributionId.value }
                    .map { it.copy(reasons = it.reasons.toSet()) }
            )
        )
    }

    private fun isInvalid(candidate: DatasetCandidate): Boolean =
        candidate.contributionId.value.isBlank() || candidate.trainingAssetId.value.isBlank() ||
            candidate.approvedFichaPezId.isBlank() || candidate.evaluatedContributionRevision < 0 ||
            !Regex("^[0-9a-fA-F]{64}$").matches(candidate.contentSha256)
}
