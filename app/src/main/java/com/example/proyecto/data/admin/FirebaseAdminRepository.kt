package com.example.proyecto.data.admin

import com.example.proyecto.Solicitud
import com.example.proyecto.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseAdminRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {

    override suspend fun getAllSolicitudes(): List<Solicitud> {
        val todasSolicitudes = mutableListOf<Solicitud>()

        val reservasTaller = db.collection("reservas")
            .orderBy("fechaReserva", Query.Direction.DESCENDING)
            .get()
            .await()

        reservasTaller.documents.forEach { doc ->
            todasSolicitudes.add(
                Solicitud(
                    id = doc.getString("reservaId") ?: doc.id,
                    clienteNombre = doc.getString("userName") ?: "Sin nombre",
                    clienteTelefono = doc.getString("userPhone") ?: "Sin teléfono",
                    direccion = doc.getString("lubricentroDireccion") ?: "Sin dirección",
                    tipoServicio = "Taller - ${doc.getString("lubricentroNombre") ?: ""}",
                    estado = doc.getString("estado") ?: "PENDIENTE",
                    fecha = doc.getTimestamp("fechaReserva")?.toDate() ?: Date(),
                    precio = doc.getLong("precio")?.toInt() ?: 0,
                    notas = doc.getString("notas") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    userId = doc.getString("userId") ?: "",
                    coleccion = "reservas"
                )
            )
        }

        val solicitudesDomicilio = db.collection("solicitudes_domicilio")
            .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
            .get()
            .await()

        solicitudesDomicilio.documents.forEach { doc ->
            todasSolicitudes.add(
                Solicitud(
                    id = doc.getString("solicitudId") ?: doc.id,
                    clienteNombre = doc.getString("userName") ?: "Sin nombre",
                    clienteTelefono = doc.getString("telefonoContacto") ?: doc.getString("userPhone") ?: "Sin teléfono",
                    direccion = doc.getString("direccion") ?: "Sin dirección",
                    tipoServicio = "Domicilio",
                    estado = doc.getString("estado") ?: "PENDIENTE",
                    fecha = doc.getTimestamp("fechaSolicitud")?.toDate() ?: Date(),
                    precio = 0,
                    notas = doc.getString("notas") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    userId = doc.getString("userId") ?: "",
                    coleccion = "solicitudes_domicilio"
                )
            )
        }

        return todasSolicitudes.sortedByDescending { it.fecha }
    }

    override suspend fun updateSolicitudEstado(coleccion: String, documentId: String, nuevoEstado: String) {
        db.collection(coleccion)
            .document(documentId)
            .update("estado", nuevoEstado)
            .await()
    }

    override suspend fun getAllUsuarios(): List<Usuario> {
        val usuariosSnapshot = db.collection("users").get().await()
        val listaUsuarios = mutableListOf<Usuario>()

        usuariosSnapshot.documents.forEach { doc ->
            val userId = doc.id
            val reservas = db.collection("reservas")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .size()

            val solicitudes = db.collection("solicitudes_domicilio")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .size()

            listaUsuarios.add(
                Usuario(
                    id = userId,
                    username = doc.getString("username") ?: "Sin nombre",
                    email = doc.getString("email") ?: "Sin email",
                    phone = doc.getString("phone") ?: "Sin teléfono",
                    isAdmin = doc.getBoolean("isAdmin") ?: false,
                    isBanned = doc.getBoolean("isBanned") ?: false,
                    createdAt = doc.getTimestamp("createdAt")?.toDate(),
                    totalServicios = reservas + solicitudes
                )
            )
        }

        return listaUsuarios.sortedByDescending { it.createdAt }
    }

    override suspend fun setUserBanned(userId: String, banned: Boolean) {
        db.collection("users")
            .document(userId)
            .update("isBanned", banned)
            .await()
    }
}
