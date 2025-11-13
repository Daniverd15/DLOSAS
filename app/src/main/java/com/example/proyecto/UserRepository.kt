package com.example.proyecto

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Verifica si un usuario es administrador consultando Firestore
     */
    suspend fun isAdmin(uid: String): Boolean {
        return try {
            val document = db.collection("users")
                .document(uid)
                .get()
                .await()

            // Obtener el campo isAdmin del documento
            document.getBoolean("isAdmin") ?: false
        } catch (e: Exception) {
            // Si hay error (ej: usuario no existe), retornar false
            false
        }
    }

    /**
     * Obtiene los datos completos del usuario desde Firestore
     */
    suspend fun getUserData(uid: String): Map<String, Any?>? {
        return try {
            val document = db.collection("users")
                .document(uid)
                .get()
                .await()

            document.data
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza el rol de admin de un usuario (solo para panel de admin)
     */
    suspend fun setAdminRole(uid: String, isAdmin: Boolean): Boolean {
        return try {
            db.collection("users")
                .document(uid)
                .update("isAdmin", isAdmin)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}