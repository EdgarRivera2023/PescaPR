package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ContributionModerationServiceTest {
    private val ownerId = "synthetic-owner"
    private val moderatorId = "synthetic-moderator"
    private val fishId = "RO2iuTVLAX11dy3aNgdf"
    private val registry = InMemoryConsentVersionRegistry().apply {
        register(ConsentVersionMetadata(
            consentVersionId = "synthetic-approved-test-version",
            status = ConsentVersionStatus.APPROVED,
            effectiveFromEpochMillis = 100,
            approvedAtEpochMillis = 90,
            locale = "en-US",
            contentIdentifier = "synthetic-consent-content",
            contentSha256 = "a".repeat(64),
            selectableForNewContributions = true
        ))
    }

    @Test
    fun `authorized moderation succeeds and persists history`() {
        val store = storeWith(aggregate(ContributionStatus.SUBMITTED))
        val result = service(store).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveRightsCommand(
                "cmd-rights", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertTrue(result is ContributionModerationServiceResult.Success)
        val persisted = store.find(ContributionId("synthetic-submission"))!!
        assertEquals(1, persisted.revision)
        assertEquals(ContributionStatus.PRIVACY_REVIEW, persisted.submission.status)
        assertEquals(1, persisted.reviews.size)
        assertEquals(1, persisted.events.size)
    }

    @Test
    fun `unauthorized actor is denied before aggregate load or mutation`() {
        val backing = storeWith(aggregate(ContributionStatus.SUBMITTED))
        val observing = ObservingStore(backing)
        val original = backing.find(ContributionId("synthetic-submission"))

        val result = service(observing).execute(request(
            principal(moderatorId),
            ApproveRightsCommand(
                "cmd-denied", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertDenied(ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED, result)
        assertEquals(0, observing.findCalls)
        assertEquals(0, observing.replaceCalls)
        assertEquals(original, backing.find(ContributionId("synthetic-submission")))
    }

    @Test
    fun `unknown contribution returns typed not found`() {
        val result = service(InMemoryContributionAggregateStore()).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveRightsCommand(
                "cmd-missing", "missing", moderatorId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertSame(ContributionModerationServiceResult.ContributionNotFound, result)
    }

    @Test
    fun `general moderator cannot approve dataset`() {
        val result = service(storeWith(eligibleDatasetAggregate())).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveForDatasetCommand(
                "cmd-dataset-denied", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.DATASET_REVIEW, 0
            )
        ))

        assertDenied(ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED, result)
    }

    @Test
    fun `dataset approver can approve complete eligible aggregate`() {
        val store = storeWith(eligibleDatasetAggregate())
        val approverId = "synthetic-dataset-approver"

        val result = service(store).execute(request(
            principal(approverId, ContributionCapability.APPROVE_DATASET),
            ApproveForDatasetCommand(
                "cmd-dataset", "synthetic-submission", approverId, 2_000,
                ContributionStatus.DATASET_REVIEW, 0
            )
        ))

        assertTrue(result is ContributionModerationServiceResult.Success)
        val stored = store.find(ContributionId("synthetic-submission"))!!
        assertEquals(ContributionStatus.APPROVED, stored.submission.status)
        assertEquals(TrainingEligibilityState.ELIGIBLE, stored.trainingAsset?.eligibilityState)
    }

    @Test
    fun `specialist label action is denied without specialist capability`() {
        val result = service(storeWith(aggregate(ContributionStatus.SPECIALIST_PENDING))).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveLabelCommand(
                "cmd-label", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.SPECIALIST_PENDING, 0, fishId
            )
        ))

        assertDenied(ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED, result)
    }

    @Test
    fun `contributor ownership alone does not grant moderation`() {
        val result = service(storeWith(aggregate(ContributionStatus.SUBMITTED))).execute(request(
            principal(ownerId),
            ApproveRightsCommand(
                "cmd-owner-moderate", "synthetic-submission", ownerId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertDenied(ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED, result)
    }

    @Test
    fun `authorized owner can initiate withdrawal`() {
        val store = storeWith(aggregate(ContributionStatus.SUBMITTED))
        val result = service(store).execute(request(
            principal(ownerId, ContributionCapability.INITIATE_OWN_WITHDRAWAL),
            InitiateWithdrawalCommand(
                "cmd-withdraw", "synthetic-submission", ownerId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertTrue(result is ContributionModerationServiceResult.Success)
        assertEquals(
            ContributionStatus.WITHDRAWAL_PENDING,
            store.find(ContributionId("synthetic-submission"))?.submission?.status
        )
    }

    @Test
    fun `owner capability cannot be used on another contribution`() {
        val result = service(storeWith(aggregate(ContributionStatus.SUBMITTED))).execute(request(
            principal("synthetic-other-owner", ContributionCapability.INITIATE_OWN_WITHDRAWAL),
            InitiateWithdrawalCommand(
                "cmd-other-withdraw", "synthetic-submission", "synthetic-other-owner", 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertDenied(ContributionAuthorizationDenial.NOT_RESOURCE_OWNER, result)
    }

    @Test
    fun `wrong expected lifecycle and revision are moderation rejections without mutation`() {
        val store = storeWith(aggregate(ContributionStatus.SUBMITTED))
        val original = store.find(ContributionId("synthetic-submission"))
        val result = service(store).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveRightsCommand(
                "cmd-stale-request", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.RIGHTS_REVIEW, 99
            )
        ))

        val rejected = result as ContributionModerationServiceResult.ModerationRejected
        assertTrue(ModerationCommandError.STALE_STATUS in rejected.rejection.errors)
        assertTrue(ModerationCommandError.STALE_REVISION in rejected.rejection.errors)
        assertEquals(original, store.find(ContributionId("synthetic-submission")))
    }

    @Test
    fun `domain moderation rejection does not replace aggregate`() {
        val store = storeWith(aggregate(ContributionStatus.PRIVACY_REVIEW).let {
            it.copy(submission = it.submission.copy(
                privacyFindings = listOf(PrivacyFinding(
                    PrivacyFlag.FACE_VISIBLE, PrivacyFindingState.OPEN
                ))
            ))
        })
        val observing = ObservingStore(store)
        val result = service(observing).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApprovePrivacyCommand(
                "cmd-open-privacy", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.PRIVACY_REVIEW, 0
            )
        ))

        assertTrue(result is ContributionModerationServiceResult.ModerationRejected)
        assertEquals(0, observing.replaceCalls)
    }

    @Test
    fun `compare and replace conflict is surfaced once without retry`() {
        val backing = storeWith(aggregate(ContributionStatus.SUBMITTED))
        val observing = ObservingStore(backing) { id, expectedRevision ->
            val concurrent = backing.find(id)!!.copy(revision = expectedRevision + 1)
            backing.replace(id, expectedRevision, concurrent)
        }
        val result = service(observing).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveRightsCommand(
                "cmd-conflict", "synthetic-submission", moderatorId, 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        val conflict = result as ContributionModerationServiceResult.StorageConflict
        assertEquals(ContributionReplaceResult.StaleRevision(1), conflict.failure)
        assertEquals(1, observing.replaceCalls)
        assertEquals(1L, backing.find(ContributionId("synthetic-submission"))?.revision)
        assertEquals(
            ContributionStatus.SUBMITTED,
            backing.find(ContributionId("synthetic-submission"))?.submission?.status
        )
    }

    @Test
    fun `principal cannot attribute a command to another actor`() {
        val observing = ObservingStore(storeWith(aggregate(ContributionStatus.SUBMITTED)))
        val result = service(observing).execute(request(
            principal(moderatorId, ContributionCapability.MODERATE_CONTRIBUTION),
            ApproveRightsCommand(
                "cmd-attribution", "synthetic-submission", "different-actor", 2_000,
                ContributionStatus.SUBMITTED, 0
            )
        ))

        assertDenied(ContributionAuthorizationDenial.ACTOR_ID_MISMATCH, result)
        assertEquals(0, observing.findCalls)
    }

    private fun service(store: ContributionAggregateStore) = ContributionModerationService(
        CapabilityContributionAuthorizationPolicy(),
        store,
        ContributionModerationEngine(setOf(fishId), registry)
    )

    private fun request(principal: ContributionPrincipal, command: ModerationCommand) =
        ContributionModerationRequest(principal, command)

    private fun principal(id: String, vararg capabilities: ContributionCapability) =
        ContributionPrincipal(ContributionPrincipalId(id), capabilities.toSet())

    private fun storeWith(aggregate: ModerationAggregate) = InMemoryContributionAggregateStore().apply {
        create(aggregate)
    }

    private fun eligibleDatasetAggregate(): ModerationAggregate =
        aggregate(ContributionStatus.DATASET_REVIEW).let { aggregate ->
            aggregate.copy(
                submission = aggregate.submission.copy(
                    approvedFichaPezId = fishId,
                    rightsReviewState = ReviewState.APPROVED,
                    privacyReviewState = ReviewState.APPROVED,
                    labelReviewState = ReviewState.APPROVED,
                    qualityReviewState = ReviewState.APPROVED
                ),
                trainingAsset = TrainingAsset(
                    trainingAssetId = "synthetic-training-asset",
                    sourceSubmissionId = "synthetic-submission",
                    sanitizedObjectReference = "opaque-synthetic-sanitized-asset",
                    approvedFichaPezId = fishId,
                    sha256 = "b".repeat(64),
                    derivativeGroupId = "synthetic-derivative-group",
                    sessionGroupId = "synthetic-session-group",
                    individualFishGroupId = "synthetic-fish-group",
                    contributorGroupId = "synthetic-contributor-group",
                    approvedAtEpochMillis = 0,
                    eligibilityState = TrainingEligibilityState.PENDING
                )
            )
        }

    private fun aggregate(status: ContributionStatus): ModerationAggregate {
        val consent = ContributionConsent(
            consentId = "synthetic-consent",
            consentVersion = "synthetic-approved-test-version",
            consentLocale = "en-US",
            acceptedAtEpochMillis = 1_000,
            ownershipConfirmed = true,
            mlTrainingAllowed = true
        )
        return ModerationAggregate(
            submission = ContributionSubmission(
                submissionId = "synthetic-submission",
                contributorUserId = ownerId,
                quarantinedAssetReference = "opaque-synthetic-quarantine-asset",
                provisionalFichaPezId = fishId,
                consentId = consent.consentId,
                consentVersion = consent.consentVersion,
                submittedAtEpochMillis = 1_100,
                updatedAtEpochMillis = 1_100,
                status = status
            ),
            consent = consent
        )
    }

    private fun assertDenied(
        reason: ContributionAuthorizationDenial,
        result: ContributionModerationServiceResult
    ) {
        assertEquals(
            ContributionModerationServiceResult.AuthorizationDenied(
                ContributionAuthorizationDecision.Denied(reason)
            ),
            result
        )
    }

    private class ObservingStore(
        private val delegate: ContributionAggregateStore,
        private val beforeReplace: ((ContributionId, Long) -> Unit)? = null
    ) : ContributionAggregateStore by delegate {
        var findCalls = 0
        var replaceCalls = 0

        override fun find(id: ContributionId): ModerationAggregate? {
            findCalls += 1
            return delegate.find(id)
        }

        override fun replace(
            id: ContributionId,
            expectedRevision: Long,
            replacement: ModerationAggregate
        ): ContributionReplaceResult {
            replaceCalls += 1
            beforeReplace?.invoke(id, expectedRevision)
            return delegate.replace(id, expectedRevision, replacement)
        }
    }
}
