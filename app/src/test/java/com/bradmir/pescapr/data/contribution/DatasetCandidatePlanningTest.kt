package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatasetCandidatePlanningTest {
    private val fishId = "RO2iuTVLAX11dy3aNgdf"
    private val approvedRegistry = registry(ConsentVersionStatus.APPROVED)

    @Test fun `fully approved synthetic contribution is eligible`() {
        assertTrue(evaluate() is DatasetCandidateEvaluation.Eligible)
    }

    @Test fun `missing ML training permission is ineligible`() {
        assertReason(
            DatasetCandidateIneligibilityReason.ML_TRAINING_NOT_ALLOWED,
            aggregate().let { it.copy(consent = it.consent.copy(mlTrainingAllowed = false)) }
        )
    }

    @Test fun `public display alone does not satisfy ML permission`() {
        assertReason(
            DatasetCandidateIneligibilityReason.ML_TRAINING_NOT_ALLOWED,
            aggregate().let { it.copy(consent = it.consent.copy(
                mlTrainingAllowed = false, publicDisplayAllowed = true
            )) }
        )
    }

    @Test fun `ML permission does not require public display permission`() {
        val aggregate = aggregate()
        assertFalse(aggregate.consent.publicDisplayAllowed)
        assertTrue(evaluate(aggregate) is DatasetCandidateEvaluation.Eligible)
    }

    @Test fun `unapproved consent version blocks eligibility`() {
        assertReason(
            DatasetCandidateIneligibilityReason.CONSENT_VERSION_INVALID,
            aggregate(), registry(ConsentVersionStatus.DRAFT)
        )
    }

    @Test fun `historically valid consent remains valid after retirement`() {
        val retired = registry(ConsentVersionStatus.RETIRED, retiredAt = 1_500)
        assertTrue(evaluate(aggregate(), registry = retired) is DatasetCandidateEvaluation.Eligible)
    }

    @Test fun `unresolved privacy blocks eligibility`() {
        assertReason(
            DatasetCandidateIneligibilityReason.PRIVACY_UNRESOLVED,
            aggregate().let { it.copy(submission = it.submission.copy(
                privacyFindings = listOf(PrivacyFinding(
                    PrivacyFlag.FACE_VISIBLE, PrivacyFindingState.OPEN
                ))
            )) }
        )
    }

    @Test fun `missing approved canonical label blocks eligibility`() {
        assertReason(
            DatasetCandidateIneligibilityReason.APPROVED_LABEL_MISSING_OR_UNKNOWN,
            aggregate().let { it.copy(submission = it.submission.copy(approvedFichaPezId = null)) }
        )
    }

    @Test fun `missing quality approval blocks eligibility`() {
        assertReason(
            DatasetCandidateIneligibilityReason.QUALITY_NOT_APPROVED,
            aggregate().let { it.copy(submission = it.submission.copy(
                qualityReviewState = ReviewState.PENDING
            )) }
        )
    }

    @Test fun `missing dataset approval blocks eligibility`() {
        assertReason(
            DatasetCandidateIneligibilityReason.NOT_DATASET_APPROVED,
            aggregate().let { it.copy(submission = it.submission.copy(
                status = ContributionStatus.DATASET_REVIEW
            )) }
        )
    }

    @Test fun `rejected contribution is ineligible`() {
        assertReason(
            DatasetCandidateIneligibilityReason.CONTRIBUTION_REJECTED,
            withStatus(ContributionStatus.REJECTED)
        )
    }

    @Test fun `excluded contribution is ineligible`() {
        assertReason(
            DatasetCandidateIneligibilityReason.CONTRIBUTION_EXCLUDED,
            withStatus(ContributionStatus.EXCLUDED)
        )
    }

    @Test fun `withdrawn contribution is ineligible for new planning`() {
        assertReason(
            DatasetCandidateIneligibilityReason.CONTRIBUTION_WITHDRAWN,
            withStatus(ContributionStatus.WITHDRAWN).let {
                it.copy(consent = it.consent.copy(withdrawnAtEpochMillis = 2_000))
            }
        )
    }

    @Test fun `missing or unavailable sanitized asset blocks eligibility`() {
        val aggregate = aggregate()
        val missing = evaluator().evaluate(aggregate.copy(trainingAsset = null), null)
        assertContains(DatasetCandidateIneligibilityReason.MISSING_SANITIZED_TRAINING_ASSET, missing)
        val unavailable = evaluate(
            aggregate,
            assetMetadata(aggregate).copy(availability = ControlledAssetAvailability.UNAVAILABLE)
        )
        assertContains(DatasetCandidateIneligibilityReason.SANITIZED_TRAINING_ASSET_UNAVAILABLE, unavailable)
    }

    @Test fun `eligible candidate preserves canonical ID and evaluated revision`() {
        val eligible = evaluate(aggregate(revision = 7)) as DatasetCandidateEvaluation.Eligible
        assertEquals(fishId, eligible.candidate.approvedFichaPezId)
        assertEquals(7L, eligible.candidate.evaluatedContributionRevision)
        assertEquals(ContributionId("synthetic-submission"), eligible.candidate.contributionId)
    }

    @Test fun `snapshot planner includes only eligible candidates and typed exclusions`() {
        val eligible = evaluate()
        val ineligible = evaluate(withStatus(ContributionStatus.REJECTED))
        val result = DatasetSnapshotPlanner().plan("synthetic-plan", listOf(ineligible, eligible))
            as DatasetSnapshotPlanningResult.Planned
        assertEquals(1, result.plan.candidates.size)
        assertEquals(1, result.plan.exclusions.size)
    }

    @Test fun `snapshot ordering is deterministic regardless of input order`() {
        val a = candidate("contribution-a", "asset-b")
        val b = candidate("contribution-b", "asset-a")
        val planner = DatasetSnapshotPlanner()
        val first = planner.plan("plan", listOf(eligible(b), eligible(a)))
        val second = planner.plan("plan", listOf(eligible(a), eligible(b)))
        assertEquals(first, second)
    }

    @Test fun `duplicate and conflicting candidate identities are typed failures`() {
        val duplicate = candidate("contribution-a", "asset-a")
        val duplicateResult = DatasetSnapshotPlanner().plan(
            "plan", listOf(eligible(duplicate), eligible(duplicate.copy(contentSha256 = "d".repeat(64))))
        ) as DatasetSnapshotPlanningResult.Rejected
        assertTrue(DatasetSnapshotPlanningError.DUPLICATE_CONTRIBUTION_CANDIDATE in duplicateResult.errors)

        val conflictResult = DatasetSnapshotPlanner().plan(
            "plan", listOf(
                eligible(candidate("contribution-a", "shared-asset")),
                eligible(candidate("contribution-b", "shared-asset"))
            )
        ) as DatasetSnapshotPlanningResult.Rejected
        assertTrue(DatasetSnapshotPlanningError.CONFLICTING_TRAINING_ASSET_IDENTITY in conflictResult.errors)
    }

    @Test fun `planned membership is copied from mutable source collection`() {
        val source = mutableListOf<DatasetCandidateEvaluation>(eligible(candidate("a", "asset-a")))
        val planned = DatasetSnapshotPlanner().plan("plan", source) as DatasetSnapshotPlanningResult.Planned
        source.clear()
        source += eligible(candidate("b", "asset-b"))
        assertEquals(listOf(ContributionId("a")), planned.plan.candidates.map { it.contributionId })
    }

    @Test fun `ML consent infers neither public display nor marketing permission`() {
        val aggregate = aggregate()
        val before = aggregate.consent
        assertTrue(evaluate(aggregate) is DatasetCandidateEvaluation.Eligible)
        assertTrue(before.mlTrainingAllowed)
        assertFalse(before.publicDisplayAllowed)
        assertFalse(before.marketingAllowed)
        assertEquals(before, aggregate.consent)
    }

    private fun evaluate(
        aggregate: ModerationAggregate = aggregate(),
        metadata: ControlledAssetMetadata = assetMetadata(aggregate),
        registry: ConsentVersionRegistry = approvedRegistry
    ) = evaluator(registry).evaluate(aggregate, metadata)

    private fun evaluator(registry: ConsentVersionRegistry = approvedRegistry) =
        DatasetCandidateEligibilityEvaluator(setOf(fishId), registry)

    private fun assertReason(
        reason: DatasetCandidateIneligibilityReason,
        aggregate: ModerationAggregate,
        registry: ConsentVersionRegistry = approvedRegistry
    ) = assertContains(reason, evaluate(aggregate, assetMetadata(aggregate), registry))

    private fun assertContains(
        reason: DatasetCandidateIneligibilityReason,
        evaluation: DatasetCandidateEvaluation
    ) {
        val ineligible = evaluation as DatasetCandidateEvaluation.Ineligible
        assertTrue(reason in ineligible.reasons)
    }

    private fun registry(status: ConsentVersionStatus, retiredAt: Long? = null) =
        InMemoryConsentVersionRegistry().apply {
            register(ConsentVersionMetadata(
                consentVersionId = "synthetic-consent-version",
                status = status,
                effectiveFromEpochMillis = 100,
                approvedAtEpochMillis = if (status in setOf(
                        ConsentVersionStatus.APPROVED, ConsentVersionStatus.RETIRED
                    )) 90 else null,
                retiredAtEpochMillis = retiredAt,
                locale = "en-US",
                contentIdentifier = "synthetic-consent-content",
                contentSha256 = "a".repeat(64),
                selectableForNewContributions = status == ConsentVersionStatus.APPROVED
            ))
        }

    private fun aggregate(revision: Long = 3): ModerationAggregate {
        val consent = ContributionConsent(
            consentId = "synthetic-consent",
            consentVersion = "synthetic-consent-version",
            consentLocale = "en-US",
            acceptedAtEpochMillis = 1_000,
            ownershipConfirmed = true,
            mlTrainingAllowed = true
        )
        val submission = ContributionSubmission(
            submissionId = "synthetic-submission",
            contributorUserId = "synthetic-contributor",
            quarantinedAssetReference = "opaque-synthetic-quarantine",
            provisionalFichaPezId = fishId,
            approvedFichaPezId = fishId,
            consentId = consent.consentId,
            consentVersion = consent.consentVersion,
            submittedAtEpochMillis = 1_100,
            updatedAtEpochMillis = 1_200,
            status = ContributionStatus.APPROVED,
            rightsReviewState = ReviewState.APPROVED,
            privacyReviewState = ReviewState.APPROVED,
            labelReviewState = ReviewState.APPROVED,
            qualityReviewState = ReviewState.APPROVED
        )
        return ModerationAggregate(
            submission = submission,
            consent = consent,
            trainingAsset = TrainingAsset(
                trainingAssetId = "synthetic-training-asset",
                sourceSubmissionId = submission.submissionId,
                sanitizedObjectReference = "opaque-synthetic-sanitized",
                approvedFichaPezId = fishId,
                sha256 = "b".repeat(64),
                derivativeGroupId = "synthetic-derivative",
                sessionGroupId = "synthetic-session",
                individualFishGroupId = "synthetic-fish",
                contributorGroupId = "synthetic-contributor-group",
                approvedAtEpochMillis = 1_200,
                eligibilityState = TrainingEligibilityState.ELIGIBLE
            ),
            revision = revision
        )
    }

    private fun withStatus(status: ContributionStatus) = aggregate().let {
        it.copy(submission = it.submission.copy(
            status = status,
            excludedAtEpochMillis = if (status == ContributionStatus.EXCLUDED) 2_000 else null,
            withdrawnAtEpochMillis = if (status == ContributionStatus.WITHDRAWN) 2_000 else null
        ))
    }

    private fun assetMetadata(aggregate: ModerationAggregate) = ControlledAssetMetadata(
        assetId = ControlledAssetId(aggregate.trainingAsset?.trainingAssetId ?: "missing"),
        kind = ControlledAssetKind.SANITIZED_TRAINING,
        sourceSubmissionId = ContributionId(aggregate.submission.submissionId),
        contentSha256 = aggregate.trainingAsset?.sha256 ?: "0".repeat(64),
        mediaType = "image/jpeg",
        byteCount = 123
    )

    private fun candidate(contributionId: String, assetId: String) = DatasetCandidate(
        ContributionId(contributionId), ControlledAssetId(assetId), fishId, 1, "c".repeat(64)
    )

    private fun eligible(candidate: DatasetCandidate) = DatasetCandidateEvaluation.Eligible(candidate)
}
