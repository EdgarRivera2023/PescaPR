package com.bradmir.pescapr.network

import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

data class TidePrediction(
    val t: String, // Time
    val v: String, // Value
    val type: String // H or L
)
data class NoaaTideResponse(val predictions: List<TidePrediction>?)

data class TideStation(val id: String, val lat: Double, val lon: Double, val name: String)

val NOAA_STATIONS_PR = listOf(
    TideStation("9755371", 18.459, -66.116, "San Juan"),
    TideStation("9753216", 18.335, -65.631, "Fajardo"),
    TideStation("9759110", 17.971, -67.045, "Magueyes Island"),
    TideStation("9752235", 18.301, -65.302, "Culebra"),
    TideStation("9752695", 18.093, -65.470, "Vieques"),
    TideStation("9757487", 17.96960068, -66.61990356, "Ponce"),
    TideStation("9759394", 18.220, -67.158, "Mayaguez"),
    TideStation("9757811", 18.480, -66.701, "Arecibo")
)

interface NoaaTideService {
    @GET("api/prod/datagetter")
    suspend fun getTidePredictions(
        @Query("date") date: String? = null,
        @Query("begin_date") beginDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("station") station: String,
        @Query("product") product: String = "predictions",
        @Query("datum") datum: String = "mllw",
        @Query("units") units: String = "english",
        @Query("time_zone") timeZone: String = "lst_ldt",
        @Query("format") format: String = "json",
        @Query("interval") interval: String = "hilo"
    ): NoaaTideResponse
}

fun findNearestTideStation(lat: Double, lon: Double): TideStation {
    return NOAA_STATIONS_PR.minByOrNull { station ->
        val dLat = Math.toRadians(station.lat - lat)
        val dLon = Math.toRadians(station.lon - lon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(station.lat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        6371 * c // Distance in km
    } ?: NOAA_STATIONS_PR[0]
}

fun calculateTideFactor(predictions: List<TidePrediction>): Triple<Float, String, String> {
    if (predictions.size < 2) return Triple(0.5f, "Sin datos", "")

    val now = java.util.Date()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val timeFormatter = java.text.SimpleDateFormat("h:mm a", Locale.US)

    // Sort predictions by time
    val sorted = predictions.mapNotNull { pred ->
        try { sdf.parse(pred.t)?.let { it to pred } } catch (e: Exception) { null }
    }.sortedBy { it.first }

    // Find where "now" sits
    for (i in 0 until sorted.size - 1) {
        val (p1Time, p1Data) = sorted[i]
        val (p2Time, _) = sorted[i+1]

        if (now.after(p1Time) && now.before(p2Time)) {
            val totalTime = p2Time.time - p1Time.time
            val elapsedTime = now.time - p1Time.time
            val progress = elapsedTime.toFloat() / totalTime.toFloat()

            val formattedNextTime = timeFormatter.format(p2Time)

            // Rising: Map 0.0-1.0 progress to 0.0-0.5 needle factor
            // Falling: Map 0.0-1.0 progress to 0.5-1.0 needle factor
            return if (p1Data.type == "L") {
                Triple((progress * 0.5f), "Subiendo (${(progress * 100).toInt()}%)", formattedNextTime)
            } else {
                Triple((0.5f + progress * 0.5f), "Bajando (${(progress * 100).toInt()}%)", formattedNextTime)
            }
        }
    }

    // If not between, use the closest or last state
    return Triple(0.5f, "Estable", "")
}
