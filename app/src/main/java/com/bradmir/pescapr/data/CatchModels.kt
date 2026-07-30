package com.bradmir.pescapr.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class CatchLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fishName: String = "",
    val weight: String = "",
    val length: String = "",
    val spotName: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val photosUrls: List<String> = emptyList(),
    val environmentalData: EnvironmentalMetadata? = null,
    val aiInsights: String? = null,
    @get:PropertyName("isPrivate") val isPrivate: Boolean = true
)

data class EnvironmentalMetadata(
    val temp: String = "",
    val pressure: String = "",
    val windSpeed: String = "",
    val tidePhase: String = "",
    val moonPhase: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
