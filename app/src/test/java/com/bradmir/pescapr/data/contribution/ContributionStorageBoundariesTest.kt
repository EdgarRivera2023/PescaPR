package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ContributionStorageBoundariesTest {
    private val id = ContributionId("synthetic-submission")

    @Test
    fun `create and retrieve synthetic aggregate`() {
        val store = InMemoryContributionAggregateStore()
        val aggregate = aggregate()

        assertEquals(ContributionCreateResult.Created(aggregate), store.create(aggregate))
        assertEquals(aggregate, store.find(id))
    }

    @Test
    fun `duplicate create is rejected without replacement`() {
        val store = InMemoryContributionAggregateStore()
        val original = aggregate()
        store.create(original)

        assertSame(ContributionCreateResult.AlreadyExists, store.create(aggregate(revision = 4)))
        assertEquals(original, store.find(id))
    }

    @Test
    fun `replace succeeds only for current revision and next aggregate revision`() {
        val store = InMemoryContributionAggregateStore()
        store.create(aggregate())
        val next = aggregate(revision = 1, status = ContributionStatus.SUBMITTED)

        assertEquals(ContributionReplaceResult.Replaced(next), store.replace(id, 0, next))
        assertEquals(next, store.find(id))
    }

    @Test
    fun `stale replace reports current revision without mutation`() {
        val store = InMemoryContributionAggregateStore()
        val current = aggregate(revision = 2)
        store.create(current)

        assertEquals(
            ContributionReplaceResult.StaleRevision(2),
            store.replace(id, 1, aggregate(revision = 2, status = ContributionStatus.SUBMITTED))
        )
        assertEquals(current, store.find(id))
    }

    @Test
    fun `unknown contribution lookup and replace are explicit`() {
        val store = InMemoryContributionAggregateStore()

        assertNull(store.find(ContributionId("missing")))
        assertSame(
            ContributionReplaceResult.NotFound,
            store.replace(ContributionId("missing"), 0, aggregate(revision = 1))
        )
    }

    @Test
    fun `replace cannot rewrite existing event history`() {
        val event = ContributionLifecycleEvent(
            eventId = "synthetic-event", submissionId = id.value, occurredAtEpochMillis = 100,
            actorId = "synthetic-actor", actorType = ContributionActorType.SYSTEM,
            eventType = ContributionEventType.SUBMITTED
        )
        val current = aggregate().copy(events = listOf(event))
        val store = InMemoryContributionAggregateStore()
        store.create(current)

        assertSame(
            ContributionReplaceResult.HistoryWouldBeRewritten,
            store.replace(id, 0, aggregate(revision = 1))
        )
        assertEquals(listOf(event), store.events(id))
    }

    @Test
    fun `metadata-only asset fake marks logical availability`() {
        val assetId = ControlledAssetId("synthetic-quarantine-asset")
        val original = ControlledAssetMetadata(
            assetId, ControlledAssetKind.QUARANTINE_SOURCE, id, "a".repeat(64),
            "image/jpeg", 123, ControlledAssetAvailability.AVAILABLE
        )
        val catalog = InMemoryControlledAssetCatalog(listOf(original))

        val result = catalog.markAvailability(assetId, ControlledAssetAvailability.WITHDRAWN)

        assertTrue(result is ControlledAssetChangeResult.Changed)
        assertEquals(ControlledAssetAvailability.WITHDRAWN, catalog.find(assetId)?.availability)
        assertSame(
            ControlledAssetChangeResult.NotFound,
            catalog.markAvailability(ControlledAssetId("missing"), ControlledAssetAvailability.EXCLUDED)
        )
    }

    @Test
    fun `fake starts empty and contains no production identities or consent`() {
        val store = InMemoryContributionAggregateStore()
        val assets = InMemoryControlledAssetCatalog()

        assertTrue(store.query(ContributionQuery()).isEmpty())
        assertTrue(store.events(ContributionId("production")).isEmpty())
        assertNull(assets.find(ControlledAssetId("production")))
    }

    private fun aggregate(
        revision: Long = 0,
        status: ContributionStatus = ContributionStatus.DRAFT
    ): ModerationAggregate {
        val consent = ContributionConsent(
            consentId = "synthetic-consent",
            consentVersion = "synthetic-non-production-version",
            consentLocale = "en-US",
            acceptedAtEpochMillis = 100,
            ownershipConfirmed = true,
            mlTrainingAllowed = true
        )
        return ModerationAggregate(
            submission = ContributionSubmission(
                submissionId = id.value,
                contributorUserId = "synthetic-principal",
                quarantinedAssetReference = "opaque-synthetic-reference",
                provisionalFichaPezId = null,
                consentId = consent.consentId,
                consentVersion = consent.consentVersion,
                submittedAtEpochMillis = 100,
                updatedAtEpochMillis = 100,
                status = status
            ),
            consent = consent,
            revision = revision
        )
    }
}
