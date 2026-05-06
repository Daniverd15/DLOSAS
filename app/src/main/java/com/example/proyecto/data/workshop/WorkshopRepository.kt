package com.example.proyecto.data.workshop

import com.example.proyecto.ReservaTaller

interface WorkshopRepository {
    suspend fun saveReservation(reserva: ReservaTaller): String
    suspend fun getCurrentUserNameAndPhone(): Pair<String, String>
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}
