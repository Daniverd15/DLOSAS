package com.example.proyecto.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

sealed interface AuthEvent {
    data class Success(val isAdmin: Boolean): AuthEvent
    data class Error(val message: String): AuthEvent
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun updateEmail(e: String) = _state.update { it.copy(email = e) }
    fun updatePassword(p: String) = _state.update { it.copy(password = p) }

    fun signIn(checkAdmin: suspend (String) -> Boolean) {
        val email = state.value.email.trim()
        val pass = state.value.password
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Completa usuario y contraseña")) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    _state.update { it.copy(loading = false) }
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid.orEmpty()
                        val admin = if (uid.isNotEmpty()) checkAdmin(uid) else false
                        _events.send(AuthEvent.Success(admin))
                    } else {
                        _events.send(AuthEvent.Error(task.exception?.localizedMessage ?: "Error de autenticación"))
                    }
                }
            }
    }

    fun sendReset(onSent: () -> Unit) {
        val email = state.value.email.trim()
        if (email.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Escribe tu correo en Usuario")) }
            return
        }
        auth.sendPasswordResetEmail(email).addOnSuccessListener { onSent() }
    }
}