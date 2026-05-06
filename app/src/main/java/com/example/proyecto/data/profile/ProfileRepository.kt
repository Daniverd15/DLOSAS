package com.example.proyecto.data.profile

import com.google.firebase.Timestamp

data class ProfileData(
    val uid: String,
    val username: String,
    val email: String,
    val phone: String,
    val isAdmin: Boolean,
    val createdAt: Timestamp?
)

data class ProfileStats(
    val totalServicios: Int,
    val totalReservas: Int,
    val puntosAcumulados: Int
)

interface ProfileRepository {
    suspend fun getCurrentProfile(): ProfileData?
    suspend fun getCurrentProfileStats(): ProfileStats
    suspend fun updateCurrentProfile(username: String, phone: String)
}
