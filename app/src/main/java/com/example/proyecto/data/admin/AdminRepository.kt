package com.example.proyecto.data.admin

import com.example.proyecto.Solicitud
import com.example.proyecto.Usuario

interface AdminRepository {
    suspend fun getAllSolicitudes(): List<Solicitud>
    suspend fun updateSolicitudEstado(coleccion: String, documentId: String, nuevoEstado: String)
    suspend fun getAllUsuarios(): List<Usuario>
    suspend fun setUserBanned(userId: String, banned: Boolean)
}
