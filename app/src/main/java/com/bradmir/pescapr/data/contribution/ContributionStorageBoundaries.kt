package com.bradmir.pescapr.data.contribution

/** Opaque application-layer identifiers. They carry no backend location semantics. */
@JvmInline value class ContributionId(val value: String)
@JvmInline value class ControlledAssetId(val value: String)

data class ContributionQuery(
    val statuses: Set<ContributionStatus> = emptySet(),
    val limit: Int = 100
)

sealed interface ContributionCreateResult {
    data class Created(val aggregate: ModerationAggregate) : ContributionCreateResult
    data object AlreadyExists : ContributionCreateResult
}

sealed interface ContributionReplaceResult {
    data class Replaced(val aggregate: ModerationAggregate) : ContributionReplaceResult
    data object NotFound : ContributionReplaceResult
    data class StaleRevision(val currentRevision: Long) : ContributionReplaceResult
    data object InvalidNextRevision : ContributionReplaceResult
    data object HistoryWouldBeRewritten : ContributionReplaceResult
}

/**
 * Boundary for contribution metadata and its immutable history. Implementations must use
 * optimistic revision checks and must not silently overwrite a newer aggregate.
 */
interface ContributionAggregateStore {
    fun create(aggregate: ModerationAggregate): ContributionCreateResult
    fun find(id: ContributionId): ModerationAggregate?
    fun replace(
        id: ContributionId,
        expectedRevision: Long,
        replacement: ModerationAggregate
    ): ContributionReplaceResult
    fun query(query: ContributionQuery): List<ModerationAggregate>
    fun events(id: ContributionId): List<ContributionLifecycleEvent>
    fun reviews(id: ContributionId): List<ContributionReviewRecord>
}

enum class ControlledAssetKind { QUARANTINE_SOURCE, SANITIZED_TRAINING }
enum class ControlledAssetAvailability { AVAILABLE, UNAVAILABLE, EXCLUDED, WITHDRAWN }

data class ControlledAssetMetadata(
    val assetId: ControlledAssetId,
    val kind: ControlledAssetKind,
    val sourceSubmissionId: ContributionId,
    val contentSha256: String,
    val mediaType: String,
    val byteCount: Long,
    val availability: ControlledAssetAvailability = ControlledAssetAvailability.AVAILABLE
)

sealed interface ControlledAssetChangeResult {
    data class Changed(val metadata: ControlledAssetMetadata) : ControlledAssetChangeResult
    data object NotFound : ControlledAssetChangeResult
}

/** Metadata-only boundary. Asset IDs deliberately reveal no path, URL, bucket, or transport. */
interface ControlledAssetCatalog {
    fun find(assetId: ControlledAssetId): ControlledAssetMetadata?
    fun markAvailability(
        assetId: ControlledAssetId,
        availability: ControlledAssetAvailability
    ): ControlledAssetChangeResult
}

/** Read boundary for already-defined dataset membership and training provenance records. */
interface DatasetProvenanceLookup {
    fun membershipsForAsset(trainingAssetId: String): List<DatasetSnapshotMembership>
    fun findTrainingRun(trainingRunId: String): ModelTrainingRunProvenance?
}

/**
 * Deterministic test/development fake only. It has no persistence or production registration.
 */
class InMemoryContributionAggregateStore : ContributionAggregateStore {
    private val aggregates = linkedMapOf<ContributionId, ModerationAggregate>()

    override fun create(aggregate: ModerationAggregate): ContributionCreateResult {
        val id = ContributionId(aggregate.submission.submissionId)
        if (id in aggregates) return ContributionCreateResult.AlreadyExists
        aggregates[id] = aggregate
        return ContributionCreateResult.Created(aggregate)
    }

    override fun find(id: ContributionId): ModerationAggregate? = aggregates[id]

    override fun replace(
        id: ContributionId,
        expectedRevision: Long,
        replacement: ModerationAggregate
    ): ContributionReplaceResult {
        val current = aggregates[id] ?: return ContributionReplaceResult.NotFound
        if (current.revision != expectedRevision) {
            return ContributionReplaceResult.StaleRevision(current.revision)
        }
        if (replacement.submission.submissionId != id.value ||
            replacement.revision != expectedRevision + 1
        ) return ContributionReplaceResult.InvalidNextRevision
        if (!replacement.events.startsWith(current.events) ||
            !replacement.reviews.startsWith(current.reviews)
        ) return ContributionReplaceResult.HistoryWouldBeRewritten
        aggregates[id] = replacement
        return ContributionReplaceResult.Replaced(replacement)
    }

    override fun query(query: ContributionQuery): List<ModerationAggregate> {
        require(query.limit >= 0) { "Query limit must not be negative" }
        return aggregates.values.asSequence()
            .filter { query.statuses.isEmpty() || it.submission.status in query.statuses }
            .take(query.limit)
            .toList()
    }

    override fun events(id: ContributionId): List<ContributionLifecycleEvent> =
        aggregates[id]?.events.orEmpty()

    override fun reviews(id: ContributionId): List<ContributionReviewRecord> =
        aggregates[id]?.reviews.orEmpty()

    private fun <T> List<T>.startsWith(prefix: List<T>): Boolean =
        size >= prefix.size && subList(0, prefix.size) == prefix
}

/** Metadata-only test/development fake; it never stores or reads binary content. */
class InMemoryControlledAssetCatalog(
    initialMetadata: Iterable<ControlledAssetMetadata> = emptyList()
) : ControlledAssetCatalog {
    private val metadata = initialMetadata.associateByTo(linkedMapOf()) { it.assetId }

    override fun find(assetId: ControlledAssetId): ControlledAssetMetadata? = metadata[assetId]

    override fun markAvailability(
        assetId: ControlledAssetId,
        availability: ControlledAssetAvailability
    ): ControlledAssetChangeResult {
        val current = metadata[assetId] ?: return ControlledAssetChangeResult.NotFound
        val changed = current.copy(availability = availability)
        metadata[assetId] = changed
        return ControlledAssetChangeResult.Changed(changed)
    }
}

/** Test/development lookup fake populated only by explicit synthetic inputs. */
class InMemoryDatasetProvenanceLookup(
    memberships: Iterable<DatasetSnapshotMembership> = emptyList(),
    trainingRuns: Iterable<ModelTrainingRunProvenance> = emptyList()
) : DatasetProvenanceLookup {
    private val memberships = memberships.toList()
    private val trainingRuns = trainingRuns.associateBy { it.trainingRunId }

    override fun membershipsForAsset(trainingAssetId: String): List<DatasetSnapshotMembership> =
        memberships.filter { it.trainingAssetId == trainingAssetId }

    override fun findTrainingRun(trainingRunId: String): ModelTrainingRunProvenance? =
        trainingRuns[trainingRunId]
}
