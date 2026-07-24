package com.bradmir.pescapr.network

import com.bradmir.pescapr.data.SwellResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MarineWeatherService {
    @GET("v1/marine")
    suspend fun getSwellData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "wave_height,wave_period,wave_direction",
        @Query("length_unit") lengthUnit: String = "imperial",
        @Query("timezone") timezone: String = "America/Puerto_Rico",
        @Query("forecast_days") forecastDays: Int = 1
    ): SwellResponse
}
