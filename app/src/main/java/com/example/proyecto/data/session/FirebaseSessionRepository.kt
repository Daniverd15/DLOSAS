package com.example.proyecto.data.session

import com.google.firebase.auth.FirebaseAuth

class FirebaseSessionRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : SessionRepository {
    override fun signOut() {
        auth.signOut()
    }
}
