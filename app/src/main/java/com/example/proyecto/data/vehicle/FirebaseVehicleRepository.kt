package com.example.proyecto.data.vehicle

import com.example.proyecto.Vehiculo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseVehicleRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VehicleRepository {

    override suspend fun getCurrentUserVehicles(): List<Vehiculo> {
        val currentUser = auth.currentUser ?: return emptyList()
        val snapshot = db.collection("vehiculos")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                Vehiculo(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: "",
                    marca = doc.getString("marca") ?: "",
                    modelo = doc.getString("modelo") ?: "",
                    año = doc.getLong("año")?.toInt() ?: 0,
                    placa = doc.getString("placa") ?: "",
                    kilometraje = doc.getLong("kilometraje")?.toInt() ?: 0,
                    color = doc.getString("color") ?: "",
                    tipo = doc.getString("tipo") ?: "auto",
                    tipoAceite = doc.getString("tipoAceite") ?: "",
                    fechaUltimoCambio = doc.getString("fechaUltimoCambio") ?: "",
                    imagenUrl = doc.getString("imagenUrl") ?: ""
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun saveVehicle(input: NewVehicleInput): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val vehiculoId = db.collection("vehiculos").document().id

        val vehiculo = hashMapOf(
            "id" to vehiculoId,
            "userId" to currentUser.uid,
            "marca" to input.marca,
            "modelo" to input.modelo,
            "año" to input.anio,
            "placa" to input.placa.uppercase(),
            "kilometraje" to input.kilometraje,
            "color" to input.color,
            "tipo" to input.tipoVehiculo,
            "tipoAceite" to "",
            "fechaUltimoCambio" to "",
            "imagenUrl" to "",
            "fechaCreacion" to FieldValue.serverTimestamp()
        )

        db.collection("vehiculos")
            .document(vehiculoId)
            .set(vehiculo)
            .await()

        return vehiculoId
    }
}
