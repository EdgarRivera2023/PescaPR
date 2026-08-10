package com.bradmir.pescapr.ui

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoastalMorphologyRepositoryTest {
    @Test
    fun firstInstallShowsBundledThenPromotesValidRemote() = runBlocking {
        val cache = FakeCacheStore()
        val remote = FakeRemoteSource(
            metadataValue = MorphologyRemoteMetadata("2", 500),
            bytes = featureCollection(polygonFeature("remote")).toByteArray()
        )
        val repository = repository(cache, remote)

        repository.loadAndRefresh()

        assertEquals(MorphologyDataSource.REMOTE, repository.state.value.source)
        assertEquals("remote", repository.state.value.data.polygons.single().metadata.id)
        assertEquals("2", cache.generation)
        assertEquals(1, remote.downloadCount)
    }

    @Test
    fun offlineFirstInstallKeepsBundledData() = runBlocking {
        val repository = repository(
            FakeCacheStore(),
            FakeRemoteSource(metadataError = IllegalStateException("offline"))
        )

        repository.loadAndRefresh()

        assertEquals(MorphologyDataSource.BUNDLED, repository.state.value.source)
        assertEquals("bundled", repository.state.value.data.polygons.single().metadata.id)
    }

    @Test
    fun validCacheWinsAndMatchingGenerationSkipsDownload() = runBlocking {
        val cache = FakeCacheStore(
            json = featureCollection(polygonFeature("cached")),
            generation = "7"
        )
        val remote = FakeRemoteSource(MorphologyRemoteMetadata("7", 500))
        val repository = repository(cache, remote)

        repository.loadAndRefresh()

        assertEquals(MorphologyDataSource.CACHE, repository.state.value.source)
        assertEquals("cached", repository.state.value.data.polygons.single().metadata.id)
        assertEquals(0, remote.downloadCount)
        assertEquals(NOW, cache.lastSuccess)
    }

    @Test
    fun malformedCacheAndMalformedRemoteNeverReplaceBundledFallback() = runBlocking {
        val cache = FakeCacheStore(json = "not-json", generation = "old")
        val remote = FakeRemoteSource(
            metadataValue = MorphologyRemoteMetadata("new", 50),
            bytes = "not-json".toByteArray()
        )
        val repository = repository(cache, remote)

        repository.loadAndRefresh()

        assertTrue(cache.discarded)
        assertEquals(MorphologyDataSource.BUNDLED, repository.state.value.source)
        assertEquals("bundled", repository.state.value.data.polygons.single().metadata.id)
        assertNull(cache.json)
        assertEquals(0L, cache.lastSuccess)
    }

    @Test
    fun failedDownloadKeepsPreviousValidCacheAndUsesRetryBackoff() = runBlocking {
        val cache = FakeCacheStore(
            json = featureCollection(polygonFeature("cached")),
            generation = "1"
        )
        val remote = FakeRemoteSource(
            metadataValue = MorphologyRemoteMetadata("2", 500),
            downloadError = IllegalStateException("download failed")
        )
        val repository = repository(cache, remote)

        repository.loadAndRefresh()
        repository.loadAndRefresh()

        assertEquals(MorphologyDataSource.CACHE, repository.state.value.source)
        assertEquals("cached", repository.state.value.data.polygons.single().metadata.id)
        assertEquals(1, remote.metadataCount)
        assertEquals(1, remote.downloadCount)
    }

    @Test
    fun oversizedRemoteIsRejectedBeforeDownload() = runBlocking {
        val remote = FakeRemoteSource(
            MorphologyRemoteMetadata("2", MORPHOLOGY_MAX_DOWNLOAD_BYTES + 1)
        )
        val repository = repository(FakeCacheStore(), remote)

        repository.loadAndRefresh()

        assertEquals(MorphologyDataSource.BUNDLED, repository.state.value.source)
        assertEquals(0, remote.downloadCount)
    }

    private fun repository(
        cache: FakeCacheStore,
        remote: FakeRemoteSource
    ) = CoastalMorphologyRepository(
        bundledJson = { featureCollection(polygonFeature("bundled")) },
        cacheStore = cache,
        remoteSource = remote,
        nowMillis = { NOW },
        logger = NoOpMorphologyLogger
    )

    private companion object {
        const val NOW = 2_000_000L
    }
}

private object NoOpMorphologyLogger : MorphologyLogger {
    override fun warning(message: String, error: Throwable) = Unit
    override fun error(message: String, error: Throwable) = Unit
}

private class FakeRemoteSource(
    private val metadataValue: MorphologyRemoteMetadata = MorphologyRemoteMetadata("1", 500),
    private val bytes: ByteArray = featureCollection(polygonFeature("remote")).toByteArray(),
    private val metadataError: Exception? = null,
    private val downloadError: Exception? = null
) : MorphologyRemoteSource {
    var metadataCount = 0
    var downloadCount = 0

    override suspend fun metadata(): MorphologyRemoteMetadata {
        metadataCount++
        metadataError?.let { throw it }
        return metadataValue
    }

    override suspend fun download(maxBytes: Long): ByteArray {
        downloadCount++
        downloadError?.let { throw it }
        return bytes
    }
}

private class FakeCacheStore(
    var json: String? = null,
    var generation: String? = null
) : MorphologyCacheStore {
    var lastSuccess = 0L
    var lastAttempt = 0L
    var discarded = false

    override fun readCachedJson(): String? = json

    override fun writeCachedJson(jsonText: String, generation: String?) {
        json = jsonText
        this.generation = generation
    }

    override fun discardCache() {
        discarded = true
        json = null
        generation = null
    }

    override fun cachedGeneration(): String? = generation
    override fun lastSuccessfulCheckMillis(): Long = lastSuccess
    override fun lastAttemptMillis(): Long = lastAttempt
    override fun recordAttempt(nowMillis: Long) {
        lastAttempt = nowMillis
    }

    override fun recordSuccessfulCheck(nowMillis: Long) {
        lastSuccess = nowMillis
    }
}
