package com.example.proyecto.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.auth.AuthRepository
import com.example.proyecto.data.auth.FirebaseAuthRepository
import com.example.proyecto.data.auth.RegisterUserInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

sealed interface AuthEvent {
    data class Success(val isAdmin: Boolean) : AuthEvent
    data class Error(val message: String) : AuthEvent
}

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun updateEmail(e: String) = _state.update { it.copy(email = e) }
    fun updatePassword(p: String) = _state.update { it.copy(password = p) }
    fun updateUsername(u: String) = _state.update { it.copy(username = u) }
    fun updateConfirmPassword(cp: String) = _state.update { it.copy(confirmPassword = cp) }
    fun updatePhone(ph: String) = _state.update { it.copy(phone = ph) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun signIn() {
        val email = state.value.email.trim()
        val pass = state.value.password

        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Completa usuario y contraseña")) }
            return
        }

        _state.update { it.copy(loading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = authRepository.signIn(email, pass)
                _events.send(AuthEvent.Success(result.isAdmin))
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Error de autenticación"
                if (message == "USUARIO BANEADO") {
                    _state.update { it.copy(error = "USUARIO BANEADO") }
                }
                _events.send(AuthEvent.Error(message))
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun sendReset(onSent: () -> Unit) {
        val email = state.value.email.trim()
        if (email.isBlank()) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Escribe tu correo en Usuario")) }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.sendReset(email)
                onSent()
            } catch (e: Exception) {
                _events.send(AuthEvent.Error(e.localizedMessage ?: "No fue posible enviar el correo"))
            }
        }
    }

    // ----------------------------------------
    // REGISTRO
    // ----------------------------------------
    fun signUp(onUidReceived: (String?) -> Unit) {
        val s = state.value
        val email = s.email.trim()
        val pass = s.password
        val confirmPass = s.confirmPassword
        val username = s.username.trim()
        val phone = s.phone.trim()

        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank() ||
            username.isBlank() || phone.isBlank()
        ) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Completa todos los campos.")) }
            return
        }
        if (pass != confirmPass) {
            viewModelScope.launch { _events.send(AuthEvent.Error("Las contraseñas no coinciden.")) }
            return
        }
        if (pass.length < 6) {
            viewModelScope.launch { _events.send(AuthEvent.Error("La contraseña debe tener al menos 6 caracteres.")) }
            return
        }

        _state.update { it.copy(loading = true, error = null) }

        viewModelScope.launch {
            try {
                val uid = authRepository.signUp(
                    RegisterUserInput(
                        email = email,
                        password = pass,
                        username = username,
                        phone = phone
                    )
                )
                _events.send(AuthEvent.Success(false))
                onUidReceived(uid)
            } catch (e: Exception) {
                _events.send(AuthEvent.Error(e.localizedMessage ?: "Error al crear cuenta"))
                onUidReceived(null)
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}