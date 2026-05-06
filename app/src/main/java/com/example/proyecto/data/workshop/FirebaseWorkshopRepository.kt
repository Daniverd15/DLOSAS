package com.example.proyecto.data.workshop

import com.example.proyecto.ReservaTaller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseWorkshopRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : WorkshopRepository {

    override suspend fun saveReservation(reserva: ReservaTaller): String {
        db.collection("reservas")
            .document(reserva.reservaId)
            .set(reserva)
            .await()
        return reserva.reservaId
    }

    override suspend fun getCurrentUserNameAndPhone(): Pair<String, String> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        val userDoc = db.collection("users").document(uid).get().await()
        val userName = userDoc.getString("username") ?: "Usuario"
        val userPhone = userDoc.getString("phone") ?: "No disponible"
        return userName to userPhone
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun getCurrentUserEmail(): String? = auth.currentUser?.email
}
