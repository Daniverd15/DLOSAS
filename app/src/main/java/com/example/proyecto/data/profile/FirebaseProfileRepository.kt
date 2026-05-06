package com.example.proyecto.data.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override suspend fun getCurrentProfile(): ProfileData? {
        val currentUser = auth.currentUser ?: return null
        val uid = currentUser.uid
        val document = db.collection("users").document(uid).get().await()

        return ProfileData(
            uid = uid,
            username = document.getString("username") ?: currentUser.displayName.orEmpty(),
            email = document.getString("email") ?: currentUser.email.orEmpty(),
            phone = document.getString("phone") ?: "No disponible",
            isAdmin = document.getBoolean("isAdmin") ?: false,
            createdAt = document.getTimestamp("createdAt")
        )
    }

    override suspend fun getCurrentProfileStats(): ProfileStats {
        val currentUser = auth.currentUser ?: return ProfileStats(0, 0, 0)
        val uid = currentUser.uid

        val reservasSnapshot = db.collection("reservas")
            .whereEqualTo("userId", uid)
            .get()
            .await()
        val cantidadReservas = reservasSnapshot.size()

        val solicitudesSnapshot = db.collection("solicitudes_domicilio")
            .whereEqualTo("userId", uid)
            .get()
            .await()
        val cantidadSolicitudes = solicitudesSnapshot.size()

        var cantidadServicios = 0
        try {
            val serviciosSnapshot = db.collection("servicios")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            cantidadServicios = serviciosSnapshot.size()
        } catch (_: Exception) {
            // optional collection
        }

        val totalReservas = cantidadReservas
        val totalServicios = cantidadReservas + cantidadSolicitudes + cantidadServicios
        val completados = reservasSnapshot.documents.count {
            it.getString("estado") == "COMPLETADO"
        } + solicitudesSnapshot.documents.count {
            it.getString("estado") == "COMPLETADO"
        }

        return ProfileStats(
            totalServicios = totalServicios,
            totalReservas = totalReservas,
            puntosAcumulados = completados * 10
        )
    }

    override suspend fun updateCurrentProfile(username: String, phone: String) {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val uid = currentUser.uid

        db.collection("users").document(uid)
            .update(
                mapOf(
                    "username" to username,
                    "phone" to phone
                )
            )
            .await()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        currentUser.updateProfile(profileUpdates).await()
    }
}
