
package com.aprendeapp.controller

import com.aprendeapp.model.Resource
import com.aprendeapp.network.ApiService

class ResourceController(
    private val api: ApiService
) {

    private val allowedTypes =
        listOf("Libro", "Video", "Artículo", "Tutorial")

    private fun validateResource(resource: Resource) {
        require(resource.titulo.isNotBlank()) {
            "El título es obligatorio"
        }

        require(resource.descripcion.isNotBlank()) {
            "La descripción es obligatoria"
        }

        require(resource.tipo in allowedTypes) {
            "El tipo de recurso no es válido"
        }

        require(
            resource.enlace.startsWith("https://") ||
                    resource.enlace.startsWith("http://")
        ) {
            "El enlace debe comenzar con http:// o https://"
        }
    }

    suspend fun list(): List<Resource> {
        return api.getResources()
    }

    suspend fun create(resource: Resource): Resource {
        validateResource(resource)
        return api.createResource(resource)
    }

    suspend fun update(id: String, resource: Resource): Resource {
        require(id.isNotBlank()) {
            "El ID del recurso es obligatorio"
        }

        validateResource(resource)
        return api.updateResource(id, resource)
    }

    suspend fun delete(id: String) {
        require(id.isNotBlank()) {
            "El ID del recurso es obligatorio"
        }

        val response = api.deleteResource(id)

        if (!response.isSuccessful) {
            throw Exception(
                "Error al eliminar: HTTP ${response.code()}"
            )
        }
    }

    suspend fun rate(resource: Resource, score: Int) {
        require(score in 1..5) {
            "La calificación debe estar entre 1 y 5"
        }

        val id = resource.id
            ?: throw Exception("El recurso no tiene ID")

        require(resource.cantidadCalificaciones >= 0) {
            "La cantidad de calificaciones no puede ser negativa"
        }

        val newCount = resource.cantidadCalificaciones + 1

        val newAverage = (
                resource.promedio * resource.cantidadCalificaciones + score
                ) / newCount.toDouble()

        val values = mapOf(
            "promedio" to newAverage,
            "cantidadCalificaciones" to newCount
        )

        api.updateRating(id, values)
    }
}
