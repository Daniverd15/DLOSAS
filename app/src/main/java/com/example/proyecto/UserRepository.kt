package com.example.proyecto

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    suspend fun isAdmin(uid: String): Boolean {
        val snap = db.getReference("users").child(uid).child("role").get().await()
        return snap.value == "admin"
    }
}
