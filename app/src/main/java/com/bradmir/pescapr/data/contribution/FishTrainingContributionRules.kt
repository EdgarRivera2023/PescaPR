package com.bradmir.pescapr.data.contribution

enum class ContributionValidationError {
    MISSING_CONSENT_REFERENCE,
    CONSENT_REFERENCE_MISMATCH,
    CONSENT_VERSION_NOT_FOUND,
    CONSENT_VERSION_NOT_VALID_AT_ACCEPTANCE,
    CONSENT_LOCALE_MISMATCH,
    OWNERSHIP_NOT_CONFIRMED,
    ML_TRAINING_NOT_ALLOWED,
    CONSENT_WITHDRAWN,
    SUBMISSION_WITHDRAWN_OR_EXCLUDED,
    SUBMISSION_NOT_APPROVED,
    RIGHTS_NOT_APPROVED,
    PRIVACY_NOT_APPROVED,
    UNRESOLVED_PRIVACY_FINDING,
    LABEL_NOT_APPROVED,
    QUALITY_NOT_APPROVED,
    MISSING_APPROVED_FICHA_PEZ_ID,
    UNKNOWN_FICHA_PEZ_ID,
    ASSET_SUBMISSION_MISMATCH,
    ASSET_LABEL_MISMATCH,
    INVALID_SHA256,
    MISSING_GROUP_ID,
    ELIGIBLE_ASSET_HAS_EXCLUSION
}

object FishTrainingContributionRules {
    private val sha256Pattern = Regex("^[0-9a-fA-F]{64}$")

    fun submissionErrors(
        submission: ContributionSubmission,
        consent: ContributionConsent,
        validFichaPezIds: Set<String>
    ): Set<ContributionValidationError> = submissionPrerequisiteErrors(
        submission, consent, validFichaPezIds, requireApprovedStatus = true
    )

    fun submissionPrerequisiteErrors(
        submission: ContributionSubmission,
        consent: ContributionConsent,
        validFichaPezIds: Set<String>,
        requireApprovedStatus: Boolean = false
    ): Set<ContributionValidationError> = buildSet {
        if (submission.consentId.isBlank() || submission.consentVersion.isBlank()) {
            add(ContributionValidationError.MISSING_CONSENT_REFERENCE)
        }
        if (submission.consentId != consent.consentId || submission.consentVersion != consent.consentVersion) {
            add(ContributionValidationError.CONSENT_REFERENCE_MISMATCH)
        }
        if (!consent.ownershipConfirmed) add(ContributionValidationError.OWNERSHIP_NOT_CONFIRMED)
        if (!consent.mlTrainingAllowed) add(ContributionValidationError.ML_TRAINING_NOT_ALLOWED)
        if (consent.isWithdrawn) add(ContributionValidationError.CONSENT_WITHDRAWN)
        if (submission.status == ContributionStatus.WITHDRAWN ||
            submission.status == ContributionStatus.EXCLUDED ||
            submission.withdrawnAtEpochMillis != null ||
            submission.excludedAtEpochMillis != null
        ) add(ContributionValidationError.SUBMISSION_WITHDRAWN_OR_EXCLUDED)
        if (requireApprovedStatus && submission.status != ContributionStatus.APPROVED) {
            add(ContributionValidationError.SUBMISSION_NOT_APPROVED)
        }
        if (submission.rightsReviewState != ReviewState.APPROVED) {
            add(ContributionValidationError.RIGHTS_NOT_APPROVED)
        }
        if (submission.privacyReviewState != ReviewState.APPROVED) {
            add(ContributionValidationError.PRIVACY_NOT_APPROVED)
        }
        if (submission.privacyFindings.any { it.state == PrivacyFindingState.OPEN }) {
            add(ContributionValidationError.UNRESOLVED_PRIVACY_FINDING)
        }
        if (submission.labelReviewState != ReviewState.APPROVED) {
            add(ContributionValidationError.LABEL_NOT_APPROVED)
        }
        if (submission.qualityReviewState != ReviewState.APPROVED) {
            add(ContributionValidationError.QUALITY_NOT_APPROVED)
        }
        val approvedId = submission.approvedFichaPezId
        if (approvedId.isNullOrBlank()) add(ContributionValidationError.MISSING_APPROVED_FICHA_PEZ_ID)
        else if (approvedId !in validFichaPezIds) add(ContributionValidationError.UNKNOWN_FICHA_PEZ_ID)
    }

    fun consentRegistryErrors(
        consent: ContributionConsent,
        registry: ConsentVersionRegistry
    ): Set<ContributionValidationError> = buildSet {
        val version = registry.resolve(consent.consentVersion)
        if (version == null) {
            add(ContributionValidationError.CONSENT_VERSION_NOT_FOUND)
        } else {
            if (version.locale != consent.consentLocale) {
                add(ContributionValidationError.CONSENT_LOCALE_MISMATCH)
            }
            if (!registry.wasValidAtAcceptance(consent)) {
                add(ContributionValidationError.CONSENT_VERSION_NOT_VALID_AT_ACCEPTANCE)
            }
        }
    }

    fun trainingAssetErrors(
        asset: TrainingAsset,
        submission: ContributionSubmission,
        consent: ContributionConsent,
        validFichaPezIds: Set<String>
    ): Set<ContributionValidationError> = buildSet {
        addAll(submissionErrors(submission, consent, validFichaPezIds))
        if (asset.sourceSubmissionId != submission.submissionId) {
            add(ContributionValidationError.ASSET_SUBMISSION_MISMATCH)
        }
        if (asset.approvedFichaPezId != submission.approvedFichaPezId) {
            add(ContributionValidationError.ASSET_LABEL_MISMATCH)
        }
        if (asset.approvedFichaPezId.isBlank()) {
            add(ContributionValidationError.MISSING_APPROVED_FICHA_PEZ_ID)
        } else if (asset.approvedFichaPezId !in validFichaPezIds) {
            add(ContributionValidationError.UNKNOWN_FICHA_PEZ_ID)
        }
        if (!sha256Pattern.matches(asset.sha256)) add(ContributionValidationError.INVALID_SHA256)
        if (asset.derivativeGroupId.isBlank() || asset.contributorGroupId.isBlank()) {
            add(ContributionValidationError.MISSING_GROUP_ID)
        }
        if (asset.eligibilityState == TrainingEligibilityState.ELIGIBLE &&
            (asset.excludedAtEpochMillis != null || !asset.exclusionReason.isNullOrBlank())
        ) add(ContributionValidationError.ELIGIBLE_ASSET_HAS_EXCLUSION)
    }

    fun isTrainingEligible(
        asset: TrainingAsset,
        submission: ContributionSubmission,
        consent: ContributionConsent,
        validFichaPezIds: Set<String>
    ): Boolean = asset.eligibilityState == TrainingEligibilityState.ELIGIBLE &&
        trainingAssetErrors(asset, submission, consent, validFichaPezIds).isEmpty()
}

object ContributionStatusTransitions {
    private val reviewStates = setOf(
        ContributionStatus.RIGHTS_REVIEW,
        ContributionStatus.PRIVACY_REVIEW,
        ContributionStatus.LABEL_REVIEW,
        ContributionStatus.QUALITY_REVIEW,
        ContributionStatus.SPECIALIST_PENDING,
        ContributionStatus.DATASET_REVIEW
    )

    fun isAllowed(from: ContributionStatus, to: ContributionStatus): Boolean {
        if (from in reviewStates) {
            return to in reviewStates || to == ContributionStatus.REJECTED ||
                to == ContributionStatus.WITHDRAWAL_PENDING ||
                (from == ContributionStatus.DATASET_REVIEW && to == ContributionStatus.APPROVED)
        }
        return when (from) {
        ContributionStatus.DRAFT -> to == ContributionStatus.SUBMITTED
        ContributionStatus.SUBMITTED -> to in reviewStates || to == ContributionStatus.REJECTED ||
            to == ContributionStatus.WITHDRAWAL_PENDING || to == ContributionStatus.WITHDRAWN
        ContributionStatus.APPROVED -> to == ContributionStatus.WITHDRAWAL_PENDING ||
            to == ContributionStatus.WITHDRAWN || to == ContributionStatus.EXCLUDED ||
            to == ContributionStatus.ARCHIVED
        ContributionStatus.WITHDRAWAL_PENDING -> to == ContributionStatus.WITHDRAWN ||
            to == ContributionStatus.EXCLUDED
        ContributionStatus.REJECTED -> to == ContributionStatus.ARCHIVED
        ContributionStatus.WITHDRAWN -> to == ContributionStatus.ARCHIVED
        ContributionStatus.EXCLUDED -> to == ContributionStatus.ARCHIVED
        ContributionStatus.ARCHIVED -> false
        else -> false
        }
    }
}
