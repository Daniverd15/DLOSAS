package com.example.proyecto.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): SignInResult {
        auth.signInWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid.orEmpty()

        if (uid.isBlank()) {
            throw IllegalStateException("UID no disponible tras autenticación")
        }

        if (isUserBanned(uid)) {
            auth.signOut()
            throw IllegalStateException("USUARIO BANEADO")
        }

        return SignInResult(uid = uid, isAdmin = checkAdmin(uid))
    }

    override suspend fun sendReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun signUp(input: RegisterUserInput): String {
        val authResult = auth.createUserWithEmailAndPassword(input.email, input.password).await()
        val user = authResult.user ?: throw IllegalStateException("No fue posible crear el usuario")
        val uid = user.uid

        val newUser = hashMapOf(
            "uid" to uid,
            "username" to input.username,
            "email" to input.email,
            "phone" to input.phone,
            "isAdmin" to false,
            "isBanned" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid).set(newUser).await()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(input.username)
            .build()
        user.updateProfile(profileUpdates).await()

        return uid
    }

    override suspend fun isUserBanned(uid: String): Boolean {
        return try {
            val snap = db.collection("users").document(uid).get().await()
            snap.getBoolean("isBanned") ?: false
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun checkAdmin(uid: String): Boolean {
        val currentUser = auth.currentUser
        if (currentUser?.email == "admin@admin.com") {
            return true
        }

        return try {
            val document = db.collection("users").document(uid).get().await()
            document.getBoolean("isAdmin") ?: false
        } catch (_: Exception) {
            auth.currentUser?.email == "admin@admin.com"
        }
    }
}
