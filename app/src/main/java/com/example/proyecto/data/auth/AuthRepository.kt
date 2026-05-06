package com.example.proyecto.data.auth

data class RegisterUserInput(
    val email: String,
    val password: String,
    val username: String,
    val phone: String
)

data class SignInResult(
    val uid: String,
    val isAdmin: Boolean
)

interface AuthRepository {
    suspend fun signIn(email: String, password: String): SignInResult
    suspend fun sendReset(email: String)
    suspend fun signUp(input: RegisterUserInput): String
    suspend fun isUserBanned(uid: String): Boolean
}
