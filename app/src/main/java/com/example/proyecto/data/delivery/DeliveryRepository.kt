package com.example.proyecto.data.delivery

import com.example.proyecto.ui.theme.SolicitudDomicilio
import com.example.proyecto.ui.theme.VehiculoSimple

interface DeliveryRepository {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun getCurrentUserDisplayName(): String?
    suspend fun getCurrentUserPhone(): String?
    suspend fun getCurrentUserNameAndPhone(fallbackPhone: String): Pair<String, String>
    suspend fun getCurrentUserVehicles(): List<VehiculoSimple>
    suspend fun saveSolicitud(solicitud: SolicitudDomicilio): String
}
