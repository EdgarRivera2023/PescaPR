package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContributionModerationEngineTest {
    private val fishId = "RO2iuTVLAX11dy3aNgdf"
    private val correctedFishId = "XTLHUX6xHya0BOisyR6E"
    private val registry = InMemoryConsentVersionRegistry().apply {
        register(ConsentVersionMetadata(
            consentVersionId = "fixture-approved-v1",
            status = ConsentVersionStatus.APPROVED,
            effectiveFromEpochMillis = 100,
            approvedAtEpochMillis = 90,
            locale = "en-US",
            contentIdentifier = "fixture://consent/approved-v1",
            contentSha256 = "c".repeat(64),
            selectableForNewContributions = true
        ))
    }
    private val engine = ContributionModerationEngine(setOf(fishId, correctedFishId), registry)

    @Test
    fun `rights approval does not approve unrelated reviews`() {
        val aggregate = aggregate(ContributionStatus.SUBMITTED)
        val accepted = engine.apply(aggregate, ApproveRightsCommand(
            "cmd-rights", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.SUBMITTED, 0
        )) as ModerationResult.Accepted
        assertEquals(ReviewState.APPROVED, accepted.aggregate.submission.rightsReviewState)
        assertEquals(ReviewState.NOT_REVIEWED, accepted.aggregate.submission.privacyReviewState)
        assertEquals(ReviewState.NOT_REVIEWED, accepted.aggregate.submission.labelReviewState)
        assertEquals(ReviewState.NOT_REVIEWED, accepted.aggregate.submission.qualityReviewState)
    }

    @Test
    fun `submission rejects a consent version that lacks legal approval`() {
        val draftRegistry = InMemoryConsentVersionRegistry().apply {
            register(ConsentVersionMetadata(
                consentVersionId = "fixture-draft-v1",
                status = ConsentVersionStatus.DRAFT,
                effectiveFromEpochMillis = 100,
                locale = "en-US",
                contentIdentifier = "fixture://consent/draft-v1",
                contentSha256 = "e".repeat(64)
            ))
        }
        val draftEngine = ContributionModerationEngine(setOf(fishId), draftRegistry)
        val aggregate = aggregate(ContributionStatus.DRAFT).let {
            it.copy(
                consent = it.consent.copy(consentVersion = "fixture-draft-v1"),
                submission = it.submission.copy(consentVersion = "fixture-draft-v1")
            )
        }
        val rejected = draftEngine.apply(aggregate, SubmitForReviewCommand(
            "cmd-submit-draft", "submission-fixture", "user-fixture", 2_000,
            ContributionStatus.DRAFT, 0
        )) as ModerationResult.Rejected
        assertTrue(rejected.errors.contains(ModerationCommandError.CONSENT_VERSION_INVALID))
        assertTrue(rejected.validationErrors.contains(
            ContributionValidationError.CONSENT_VERSION_NOT_VALID_AT_ACCEPTANCE
        ))
    }

    @Test
    fun `label correction preserves prior history and emits correction audit`() {
        val prior = ContributionReviewRecord(
            "review-old", "submission-fixture", ContributionReviewType.LABEL,
            "reviewer-old", 1_500, ContributionReviewDecision.APPROVED,
            proposedFichaPezId = fishId
        )
        val aggregate = aggregate(ContributionStatus.LABEL_REVIEW).copy(reviews = listOf(prior))
        val accepted = engine.apply(aggregate, CorrectLabelCommand(
            "cmd-correct", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.LABEL_REVIEW, 0, correctedFichaPezId = correctedFishId
        )) as ModerationResult.Accepted
        assertEquals(2, accepted.aggregate.reviews.size)
        assertEquals(prior, accepted.aggregate.reviews.first())
        assertEquals(correctedFishId, accepted.aggregate.submission.approvedFichaPezId)
        assertEquals(ContributionEventType.LABEL_CORRECTED, accepted.generatedEvents.single().eventType)
    }

    @Test
    fun `dataset approval fails when prerequisites are incomplete`() {
        val aggregate = aggregate(ContributionStatus.DATASET_REVIEW)
        val rejected = engine.apply(aggregate, ApproveForDatasetCommand(
            "cmd-dataset-bad", "submission-fixture", "approver-fixture", 2_000,
            ContributionStatus.DATASET_REVIEW, 0
        )) as ModerationResult.Rejected
        assertTrue(rejected.errors.contains(ModerationCommandError.DATASET_PREREQUISITES_INCOMPLETE))
    }

    @Test
    fun `dataset approval succeeds only with complete eligible state`() {
        val aggregate = aggregate(ContributionStatus.DATASET_REVIEW).let {
            it.copy(submission = it.submission.copy(
                approvedFichaPezId = fishId,
                rightsReviewState = ReviewState.APPROVED,
                privacyReviewState = ReviewState.APPROVED,
                labelReviewState = ReviewState.APPROVED,
                qualityReviewState = ReviewState.APPROVED
            ))
        }
        val accepted = engine.apply(aggregate, ApproveForDatasetCommand(
            "cmd-dataset-good", "submission-fixture", "approver-fixture", 2_000,
            ContributionStatus.DATASET_REVIEW, 0
        )) as ModerationResult.Accepted
        assertEquals(ContributionStatus.APPROVED, accepted.aggregate.submission.status)
        assertEquals(TrainingEligibilityState.ELIGIBLE, accepted.aggregate.trainingAsset?.eligibilityState)
    }

    @Test
    fun `stale command is rejected without mutation`() {
        val rejected = engine.apply(aggregate(ContributionStatus.PRIVACY_REVIEW), ApprovePrivacyCommand(
            "cmd-stale", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.LABEL_REVIEW, 99
        )) as ModerationResult.Rejected
        assertTrue(rejected.errors.contains(ModerationCommandError.STALE_STATUS))
        assertTrue(rejected.errors.contains(ModerationCommandError.STALE_REVISION))
    }

    @Test
    fun `terminal state command is rejected`() {
        val rejected = engine.apply(aggregate(ContributionStatus.REJECTED), ApproveRightsCommand(
            "cmd-terminal", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.REJECTED, 0
        )) as ModerationResult.Rejected
        assertTrue(rejected.errors.contains(ModerationCommandError.TERMINAL_STATE))
    }

    @Test
    fun `privacy approval cannot silently resolve open findings`() {
        val aggregate = aggregate(ContributionStatus.PRIVACY_REVIEW).let {
            it.copy(submission = it.submission.copy(
                privacyFindings = listOf(PrivacyFinding(PrivacyFlag.FACE_VISIBLE, PrivacyFindingState.OPEN))
            ))
        }
        val rejected = engine.apply(aggregate, ApprovePrivacyCommand(
            "cmd-privacy", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.PRIVACY_REVIEW, 0
        )) as ModerationResult.Rejected
        assertTrue(rejected.errors.contains(ModerationCommandError.OPEN_PRIVACY_FINDINGS))
    }

    @Test
    fun `withdrawal removes asset eligibility and records event`() {
        val pending = engine.apply(aggregate(ContributionStatus.APPROVED), InitiateWithdrawalCommand(
            "cmd-withdraw-start", "submission-fixture", "user-fixture", 2_000,
            ContributionStatus.APPROVED, 0
        )) as ModerationResult.Accepted
        val completed = engine.apply(pending.aggregate, CompleteWithdrawalCommand(
            "cmd-withdraw-complete", "submission-fixture", "reviewer-fixture", 2_100,
            ContributionStatus.WITHDRAWAL_PENDING, 1
        )) as ModerationResult.Accepted
        assertEquals(ContributionStatus.WITHDRAWN, completed.aggregate.submission.status)
        assertEquals(TrainingEligibilityState.WITHDRAWN, completed.aggregate.trainingAsset?.eligibilityState)
        assertTrue(completed.aggregate.consent.isWithdrawn)
        assertEquals(ContributionEventType.CONSENT_WITHDRAWN, completed.generatedEvents.single().eventType)
    }

    @Test
    fun `rejection creates an auditable event with typed reason`() {
        val accepted = engine.apply(aggregate(ContributionStatus.LABEL_REVIEW), RejectContributionCommand(
            "cmd-reject", "submission-fixture", "reviewer-fixture", 2_000,
            ContributionStatus.LABEL_REVIEW, 0, ModerationReasonCode.LABEL_UNCERTAIN
        )) as ModerationResult.Accepted
        assertEquals(ContributionStatus.REJECTED, accepted.aggregate.submission.status)
        assertEquals("LABEL_UNCERTAIN", accepted.generatedEvents.single().reasonCode)
        assertEquals(ContributionReviewDecision.REJECTED, accepted.generatedReviews.single().decision)
        assertFalse(accepted.aggregate.events.isEmpty())
    }

    private fun aggregate(status: ContributionStatus): ModerationAggregate {
        val consent = ContributionConsent(
            consentId = "consent-fixture",
            consentVersion = "fixture-approved-v1",
            consentLocale = "en-US",
            acceptedAtEpochMillis = 1_000,
            ownershipConfirmed = true,
            mlTrainingAllowed = true
        )
        val submission = ContributionSubmission(
            submissionId = "submission-fixture",
            contributorUserId = "user-fixture",
            quarantinedAssetReference = "fixture://quarantine",
            provisionalFichaPezId = fishId,
            consentId = consent.consentId,
            consentVersion = consent.consentVersion,
            submittedAtEpochMillis = 1_100,
            updatedAtEpochMillis = 1_100,
            status = status
        )
        val asset = TrainingAsset(
            trainingAssetId = "asset-fixture",
            sourceSubmissionId = submission.submissionId,
            sanitizedObjectReference = "fixture://sanitized",
            approvedFichaPezId = fishId,
            sha256 = "d".repeat(64),
            derivativeGroupId = "derivative-fixture",
            sessionGroupId = "session-fixture",
            individualFishGroupId = "fish-fixture",
            contributorGroupId = "contributor-fixture",
            approvedAtEpochMillis = 0,
            eligibilityState = if (status == ContributionStatus.APPROVED) {
                TrainingEligibilityState.ELIGIBLE
            } else TrainingEligibilityState.PENDING
        )
        return ModerationAggregate(submission, consent, asset)
    }
}
