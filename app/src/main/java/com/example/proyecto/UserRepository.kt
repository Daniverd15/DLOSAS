package com.example.proyecto

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Verifica si un usuario es administrador
     * Método 1: Por email específico
     * Método 2: Por campo isAdmin en Firestore
     */
    suspend fun isAdmin(uid: String): Boolean {
        return try {
            // Primero verificar si es el email de admin específico
            val currentUser = auth.currentUser
            if (currentUser?.email == "admin@admin.com") {
                return true
            }

            // Si no es el email específico, verificar en Firestore
            val document = db.collection("users")
                .document(uid)
                .get()
                .await()

            document.getBoolean("isAdmin") ?: false
        } catch (e: Exception) {
            // En caso de error, verificar solo por email
            auth.currentUser?.email == "admin@admin.com"
        }
    }

    /**
     * Obtiene los datos del usuario actual desde Firestore
     */
    suspend fun getCurrentUserData(): User? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val document = db.collection("users")
                .document(uid)
                .get()
                .await()

            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza los datos del usuario en Firestore
     */
    suspend fun updateUserData(username: String, phone: String): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false

            val updates = hashMapOf<String, Any>(
                "username" to username,
                "phone" to phone
            )

            db.collection("users")
                .document(uid)
                .update(updates)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}

// Modelo de datos de usuario (debe coincidir con el de AuthViewModel)
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Any? = null
)