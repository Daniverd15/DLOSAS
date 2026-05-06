package com.example.proyecto.data.delivery

import com.example.proyecto.ui.theme.SolicitudDomicilio
import com.example.proyecto.ui.theme.VehiculoSimple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseDeliveryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DeliveryRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun getCurrentUserEmail(): String? = auth.currentUser?.email

    override fun getCurrentUserDisplayName(): String? = auth.currentUser?.displayName

    override suspend fun getCurrentUserPhone(): String? {
        val uid = auth.currentUser?.uid ?: return null
        val userDoc = db.collection("users").document(uid).get().await()
        return userDoc.getString("phone")
    }

    override suspend fun getCurrentUserNameAndPhone(fallbackPhone: String): Pair<String, String> {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val userDoc = db.collection("users").document(currentUser.uid).get().await()
        val userName = userDoc.getString("username") ?: currentUser.displayName ?: "Usuario"
        val userPhone = userDoc.getString("phone") ?: fallbackPhone
        return userName to userPhone
    }

    override suspend fun getCurrentUserVehicles(): List<VehiculoSimple> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("vehiculos")
            .whereEqualTo("userId", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            runCatching {
                VehiculoSimple(
                    id = doc.getString("id") ?: "",
                    marca = doc.getString("marca") ?: "",
                    modelo = doc.getString("modelo") ?: "",
                    placa = doc.getString("placa") ?: "",
                    tipo = doc.getString("tipo") ?: "auto"
                )
            }.getOrNull()
        }
    }

    override suspend fun saveSolicitud(solicitud: SolicitudDomicilio): String {
        db.collection("solicitudes_domicilio")
            .document(solicitud.solicitudId)
            .set(solicitud)
            .await()
        return solicitud.solicitudId
    }
}
