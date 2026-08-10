package com.bradmir.pescapr.ui

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import com.bradmir.pescapr.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

internal const val MORPHOLOGY_REMOTE_PATH = "morphology/coastal_morphology.geojson"
internal const val MORPHOLOGY_MAX_DOWNLOAD_BYTES = 5L * 1024L * 1024L
internal const val MORPHOLOGY_SUCCESS_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L
internal const val MORPHOLOGY_FAILURE_RETRY_INTERVAL_MS = 15L * 60L * 1000L

internal enum class MorphologyDataSource {
    NONE,
    BUNDLED,
    CACHE,
    REMOTE
}

internal data class MorphologyRepositoryState(
    val data: MorphologyParsedData = MorphologyParsedData(),
    val source: MorphologyDataSource = MorphologyDataSource.NONE
)

internal data class MorphologyRemoteMetadata(
    val generation: String?,
    val sizeBytes: Long
)

internal interface MorphologyRemoteSource {
    suspend fun metadata(): MorphologyRemoteMetadata
    suspend fun download(maxBytes: Long): ByteArray
}

internal interface MorphologyCacheStore {
    fun readCachedJson(): String?
    fun writeCachedJson(jsonText: String, generation: String?)
    fun discardCache()
    fun cachedGeneration(): String?
    fun lastSuccessfulCheckMillis(): Long
    fun lastAttemptMillis(): Long
    fun recordAttempt(nowMillis: Long)
    fun recordSuccessfulCheck(nowMillis: Long)
}

internal interface MorphologyLogger {
    fun warning(message: String, error: Throwable)
    fun error(message: String, error: Throwable)
}

private object AndroidMorphologyLogger : MorphologyLogger {
    override fun warning(message: String, error: Throwable) {
        Log.w("MorphologyRepository", message, error)
    }

    override fun error(message: String, error: Throwable) {
        Log.e("MorphologyRepository", message, error)
    }
}

internal class FirebaseMorphologyRemoteSource(
    storage: FirebaseStorage = FirebaseStorage.getInstance()
) : MorphologyRemoteSource {
    private val reference = storage.reference.child(MORPHOLOGY_REMOTE_PATH)

    override suspend fun metadata(): MorphologyRemoteMetadata {
        val metadata = reference.metadata.await()
        return MorphologyRemoteMetadata(
            generation = metadata.generation,
            sizeBytes = metadata.sizeBytes
        )
    }

    override suspend fun download(maxBytes: Long): ByteArray = reference.getBytes(maxBytes).await()
}

internal class AndroidMorphologyCacheStore(context: Context) : MorphologyCacheStore {
    private val cacheFile = File(context.filesDir, "morphology/coastal_morphology.geojson")
    private val atomicFile = AtomicFile(cacheFile)
    private val preferences = context.getSharedPreferences(
        "coastal_morphology_cache",
        Context.MODE_PRIVATE
    )

    override fun readCachedJson(): String? = cacheFile
        .takeIf(File::isFile)
        ?.readBytes()
        ?.decodeStrictUtf8()

    override fun writeCachedJson(jsonText: String, generation: String?) {
        cacheFile.parentFile?.mkdirs()
        val output = atomicFile.startWrite()
        try {
            output.write(jsonText.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(output)
        } catch (error: Exception) {
            atomicFile.failWrite(output)
            throw error
        }
        preferences.edit().apply {
            if (generation == null) remove(KEY_GENERATION) else putString(KEY_GENERATION, generation)
        }.apply()
    }

    override fun discardCache() {
        atomicFile.delete()
        preferences.edit().remove(KEY_GENERATION).apply()
    }

    override fun cachedGeneration(): String? = preferences.getString(KEY_GENERATION, null)

    override fun lastSuccessfulCheckMillis(): Long = preferences.getLong(KEY_LAST_SUCCESS, 0L)

    override fun lastAttemptMillis(): Long = preferences.getLong(KEY_LAST_ATTEMPT, 0L)

    override fun recordAttempt(nowMillis: Long) {
        preferences.edit().putLong(KEY_LAST_ATTEMPT, nowMillis).apply()
    }

    override fun recordSuccessfulCheck(nowMillis: Long) {
        preferences.edit().putLong(KEY_LAST_SUCCESS, nowMillis).apply()
    }

    private companion object {
        const val KEY_GENERATION = "generation"
        const val KEY_LAST_SUCCESS = "last_successful_check"
        const val KEY_LAST_ATTEMPT = "last_attempt"
    }
}

internal class CoastalMorphologyRepository(
    private val bundledJson: () -> String,
    private val cacheStore: MorphologyCacheStore,
    private val remoteSource: MorphologyRemoteSource,
    private val parser: CoastalMorphologyParser = CoastalMorphologyParser(),
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val logger: MorphologyLogger = AndroidMorphologyLogger
) {
    constructor(context: Context) : this(
        bundledJson = {
            context.resources.openRawResource(R.raw.coastal_morphology_geojson).use { input ->
                input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        },
        cacheStore = AndroidMorphologyCacheStore(context.applicationContext),
        remoteSource = FirebaseMorphologyRemoteSource()
    )

    private val updateMutex = Mutex()
    private val _state = MutableStateFlow(MorphologyRepositoryState())
    val state: StateFlow<MorphologyRepositoryState> = _state.asStateFlow()

    suspend fun loadAndRefresh() = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            loadBestLocalData()
            refreshRemoteIfDue()
        }
    }

    private fun loadBestLocalData() {
        val cachedJson = try {
            cacheStore.readCachedJson()
        } catch (error: Exception) {
            logger.warning("Unable to read morphology cache", error)
            cacheStore.discardCache()
            null
        }

        if (cachedJson != null) {
            try {
                _state.value = MorphologyRepositoryState(
                    parser.parse(cachedJson),
                    MorphologyDataSource.CACHE
                )
                return
            } catch (error: Exception) {
                logger.warning("Discarding invalid morphology cache", error)
                cacheStore.discardCache()
            }
        }

        try {
            _state.value = MorphologyRepositoryState(
                parser.parse(bundledJson()),
                MorphologyDataSource.BUNDLED
            )
        } catch (error: Exception) {
            logger.error("Bundled morphology dataset is invalid", error)
            _state.value = MorphologyRepositoryState()
        }
    }

    private suspend fun refreshRemoteIfDue() {
        val now = nowMillis()
        if (!isRemoteCheckDue(now)) return
        cacheStore.recordAttempt(now)

        try {
            val metadata = remoteSource.metadata()
            require(metadata.sizeBytes in 1..MORPHOLOGY_MAX_DOWNLOAD_BYTES) {
                "Remote morphology dataset has invalid size: ${metadata.sizeBytes}"
            }

            val cachedGeneration = cacheStore.cachedGeneration()
            if (metadata.generation != null && metadata.generation == cachedGeneration) {
                cacheStore.recordSuccessfulCheck(now)
                return
            }

            val remoteBytes = remoteSource.download(MORPHOLOGY_MAX_DOWNLOAD_BYTES)
            require(remoteBytes.isNotEmpty()) { "Remote morphology dataset is empty" }
            val remoteJson = remoteBytes.decodeStrictUtf8()
            val parsed = parser.parse(remoteJson)

            cacheStore.writeCachedJson(remoteJson, metadata.generation)
            cacheStore.recordSuccessfulCheck(now)
            _state.value = MorphologyRepositoryState(parsed, MorphologyDataSource.REMOTE)
        } catch (error: Exception) {
            logger.warning("Remote morphology update failed; retaining local data", error)
        }
    }

    private fun isRemoteCheckDue(now: Long): Boolean {
        val lastSuccess = cacheStore.lastSuccessfulCheckMillis()
        if (lastSuccess > 0L && now - lastSuccess < MORPHOLOGY_SUCCESS_CHECK_INTERVAL_MS) {
            return false
        }
        val lastAttempt = cacheStore.lastAttemptMillis()
        return lastAttempt <= 0L || now - lastAttempt >= MORPHOLOGY_FAILURE_RETRY_INTERVAL_MS
    }
}

private fun ByteArray.decodeStrictUtf8(): String = Charsets.UTF_8
    .newDecoder()
    .onMalformedInput(CodingErrorAction.REPORT)
    .onUnmappableCharacter(CodingErrorAction.REPORT)
    .decode(ByteBuffer.wrap(this))
    .toString()
