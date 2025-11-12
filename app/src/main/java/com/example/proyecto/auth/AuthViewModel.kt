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
    val username: String = "",
    val confirmPassword: String = "",
    val phone: String = "",

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

    // 2. **AGREGAR UPDATE FUNCTIONS**
    fun updateUsername(u: String) = _state.update { it.copy(username = u) }
    fun updateConfirmPassword(cp: String) = _state.update { it.copy(confirmPassword = cp) }
    fun updatePhone(ph: String) = _state.update { it.copy(phone = ph) }

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

    // Agrega la función para crear un nuevo usuario
    fun signUp(onUidReceived: (String?) -> Unit) {
        val s = state.value
        val email = s.email.trim()
        val pass = s.password
        val confirmPass = s.confirmPassword
        val username = s.username.trim()
        val phone = s.phone.trim() // Aunque no se usa directamente en Firebase Auth estándar, es bueno validar

        // 1. Validaciones
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank() || username.isBlank() || phone.isBlank()) {
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

        // 2. Creación de Usuario en Firebase
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                viewModelScope.launch { // Corrutina 1: Manejo principal
                    _state.update { it.copy(loading = false) }

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        // 3. Actualización de Perfil (Username)
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->

                                // Corrutina 2: Manejo de la sub-tarea de actualización de perfil
                                viewModelScope.launch {
                                    if (profileTask.isSuccessful) {
                                        // Éxito completo: usuario creado y nombre guardado
                                        _events.send(AuthEvent.Success(isAdmin = false))
                                        onUidReceived(uid)
                                    } else {
                                        // Error al guardar el nombre
                                        _events.send(AuthEvent.Error(profileTask.exception?.localizedMessage ?: "Registro exitoso, pero falló al guardar el nombre."))
                                        onUidReceived(null)
                                    }
                                }
                            }
                    } else {
                        // Error en la creación inicial de la cuenta
                        val errorMessage = task.exception?.localizedMessage ?: "Error desconocido al crear cuenta"
                        _events.send(AuthEvent.Error(errorMessage))
                        onUidReceived(null)
                    }
                } // Cierre de Corrutina 1
            }
    }
}
