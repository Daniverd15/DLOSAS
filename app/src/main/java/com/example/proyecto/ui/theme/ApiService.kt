package com.example.proyecto.ui.theme

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("v2/directions/driving-car")
    suspend fun getRoute(
        @Header("Authorization") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): Response<RouteResponseORS>
}
