package com.bradmir.pescapr.data

import com.google.android.gms.maps.model.LatLng

data class PuntoPesca(
    val id: String = "0",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val nombre: String = "",
    val descripcion: String = "",
    val fotosUrls: List<String> = emptyList(),
    val userId: String = ""
) {
    val coordenada: LatLng
        get() = LatLng(latitude, longitude)
}
