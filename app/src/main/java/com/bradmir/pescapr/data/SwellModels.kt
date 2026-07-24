package com.bradmir.pescapr.data

import com.google.gson.annotations.SerializedName

data class SwellResponse(
    val hourly: HourlySwell?
)

data class HourlySwell(
    val time: List<String>,
    @SerializedName("wave_height") val waveHeight: List<Float?>,
    @SerializedName("wave_period") val wavePeriod: List<Float?>,
    @SerializedName("wave_direction") val waveDirection: List<Float?>
)

data class ProSwellMetrics(
    val heightFt: Float,
    val periodSec: Float,
    val directionDeg: Float,
    val score: Int
)
