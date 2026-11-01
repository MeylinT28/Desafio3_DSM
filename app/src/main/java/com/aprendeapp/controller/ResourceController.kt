
package com.aprendeapp.controller

import com.aprendeapp.model.Resource
import com.aprendeapp.network.ApiService

class ResourceController(
    private val api: ApiService
) {

    suspend fun list(): List<Resource> {
        return api.getResources()
    }

    suspend fun create(resource: Resource): Resource {
        return api.createResource(resource)
    }

    suspend fun update(id: String, resource: Resource): Resource {
        return api.updateResource(id, resource)
    }

    suspend fun delete(id: String) {
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

        val newCount = resource.cantidadCalificaciones + 1

        val newAverage = (
                resource.promedio * resource.cantidadCalificaciones + score
                ) / newCount.toDouble()

        val values = mapOf(
            "promedio" to newAverage,
            "cantidadCalificaciones" to newCount
        )

        // Actualiza solamente los campos de calificación.
        api.updateRating(id, values)
    }
}
