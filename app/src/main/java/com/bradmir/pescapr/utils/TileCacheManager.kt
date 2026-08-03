package com.bradmir.pescapr.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class TileCacheManager(context: Context) {

    private val TAG = "TileCacheManager"
    private val maxCacheSize = 50L * 1024L * 1024L // 50 MB strict size cap

    private val tileCacheDir = File(context.cacheDir, "tile_cache")

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(tileCacheDir, maxCacheSize))
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(7, TimeUnit.DAYS)
                .build()
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .removeHeader("Pragma")
                .build()
        }
        .addInterceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build()
            }
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    suspend fun getTileBytes(urlString: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(urlString)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; PescaPR/1.0)")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    Log.d(TAG, "Tile fetched successfully -> $urlString, Size: ${bytes?.size ?: 0}")
                    bytes
                } else {
                    Log.w(TAG, "Tile fetch failed with code ${response.code} -> $urlString")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tile: $urlString", e)
            null
        }
    }
}

class CachedTileProvider(
    private val tileCacheManager: TileCacheManager,
    private val minZoom: Int = 1,
    private val tileSize: Int = 256
) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        if (zoom < minZoom) return TileProvider.NO_TILE

        val urlString =
            "https://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Base/MapServer/tile/$zoom/$y/$x"

        return runBlocking(Dispatchers.IO) {
            val bytes = tileCacheManager.getTileBytes(urlString)
            if (bytes != null && bytes.isNotEmpty()) {
                Tile(tileSize, tileSize, bytes)
            } else {
                TileProvider.NO_TILE
            }
        }
    }
}
