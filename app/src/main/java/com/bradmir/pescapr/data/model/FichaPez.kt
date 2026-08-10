package com.bradmir.pescapr.data.model

data class FichaPez(
    val id: String = "",
    val nombreCientifico: String = "",
    val nombreComun: String = "", // Común y locales
    val nombreIngles: String = "",
    val regulacionComercial: String = "",
    val regulacionRecreativa: String = "",
    val caracteristicas: List<String> = emptyList(),
    val puedeSerConfundidoCon: String = "",
    val fotosUrls: List<String> = emptyList(),
    val localThumbResName: String = ""
)
