package com.bradmir.pescapr.data

data class MainData(
    val temp: Float,
    val pressure: Int
)

data class WindData(
    val speed: Float
)

data class WeatherResponse(
    val main: MainData,
    val wind: WindData,
    val waterTemp: Float? = null,
    val weeklyWaterTemps: List<Float> = emptyList()
)
