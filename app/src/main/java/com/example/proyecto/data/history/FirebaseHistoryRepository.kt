package com.example.proyecto.data.history

import com.example.proyecto.HistorialItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseHistoryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : HistoryRepository {

    override suspend fun getCurrentUserHistory(): List<HistorialItem> {
        val currentUser = auth.currentUser ?: return emptyList()
        val items = mutableListOf<HistorialItem>()

        runCatching {
            val reservas = db.collection("reservas")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            reservas.documents.forEach { doc ->
                val fecha = doc.getTimestamp("fechaReserva")?.toDate()
                    ?: doc.getTimestamp("fecha")?.toDate()
                    ?: doc.getTimestamp("timestamp")?.toDate()
                    ?: Date()
                items.add(
                    HistorialItem(
                        id = doc.id,
                        tipo = "Taller",
                        nombre = doc.getString("lubricentroNombre")
                            ?: doc.getString("nombreLubricentro")
                            ?: doc.getString("taller")
                            ?: "Servicio de Taller",
                        direccion = doc.getString("lubricentroDireccion")
                            ?: doc.getString("direccion")
                            ?: "Sin dirección",
                        estado = doc.getString("estado") ?: "PENDIENTE",
                        fecha = fecha,
                        precio = (doc.getLong("precio") ?: doc.getLong("costo") ?: 0L).toInt(),
                        notas = doc.getString("notas")
                            ?: doc.getString("observaciones")
                            ?: ""
                    )
                )
            }
        }

        runCatching {
            val solicitudes = db.collection("solicitudes_domicilio")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            solicitudes.documents.forEach { doc ->
                val fecha = doc.getTimestamp("fechaSolicitud")?.toDate()
                    ?: doc.getTimestamp("fecha")?.toDate()
                    ?: doc.getTimestamp("timestamp")?.toDate()
                    ?: Date()
                items.add(
                    HistorialItem(
                        id = doc.id,
                        tipo = "Domicilio",
                        nombre = "Servicio a Domicilio",
                        direccion = doc.getString("direccion")
                            ?: doc.getString("ubicacion")
                            ?: "Sin dirección",
                        estado = doc.getString("estado") ?: "PENDIENTE",
                        fecha = fecha,
                        precio = (doc.getLong("precio") ?: 0L).toInt(),
                        notas = doc.getString("notas")
                            ?: doc.getString("observaciones")
                            ?: doc.getString("detalles")
                            ?: ""
                    )
                )
            }
        }

        runCatching {
            val servicios = db.collection("servicios")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            servicios.documents.forEach { doc ->
                val fecha = doc.getTimestamp("fecha")?.toDate()
                    ?: doc.getTimestamp("timestamp")?.toDate()
                    ?: Date()
                items.add(
                    HistorialItem(
                        id = doc.id,
                        tipo = doc.getString("tipo") ?: "Servicio",
                        nombre = doc.getString("nombre") ?: "Servicio",
                        direccion = doc.getString("direccion") ?: "Sin dirección",
                        estado = doc.getString("estado") ?: "PENDIENTE",
                        fecha = fecha,
                        precio = (doc.getLong("precio") ?: 0L).toInt(),
                        notas = doc.getString("notas") ?: ""
                    )
                )
            }
        }

        return items.sortedByDescending { it.fecha }
    }
}
