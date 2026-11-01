package com.aprendeapp.model

data class Resource(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "Libro",
    val enlace: String = "",
    val imagen: String = "",
    val promedio: Double = 0.0,
    val cantidadCalificaciones: Int = 0
)
