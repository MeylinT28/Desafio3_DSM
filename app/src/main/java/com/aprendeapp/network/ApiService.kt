package com.aprendeapp.network

import com.aprendeapp.model.Resource
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("recursos") suspend fun getResources(): List<Resource>
    @POST("recursos") suspend fun createResource(@Body resource: Resource): Resource
    @PUT("recursos/{id}") suspend fun updateResource(@Path("id") id: String, @Body resource: Resource): Resource
    @DELETE("recursos/{id}") suspend fun deleteResource(@Path("id") id: String): Response<Unit>
    @PATCH("recursos/{id}") suspend fun updateRating(@Path("id") id: String, @Body values: Map<String, Any>): Resource
}
