package com.bradmir.pescapr.data.model

data class RecordPesca(
    val id: String = "0", // Local Room ID as String
    val nombrePez: String = "",
    val peso: String = "",
    val longitud: String = "",
    val lugar: String = "",
    val fecha: String = "",
    val fotosUrls: List<String> = emptyList(),
    val spotId: String = "0", // Local Spot ID as String
    val fishId: String? = null,
    val climaTemp: String = "",
    val climaWind: String = "",
    val climaPressure: String = "",
    val climaTide: String = ""
)
