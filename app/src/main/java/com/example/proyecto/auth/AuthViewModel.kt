package com.example.proyecto.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ----------------------------------------------------
// MODELO PARA FIRESTORE
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: com.google.firebase.firestore.FieldValue =
        com.google.firebase.firestore.FieldValue.serverTimestamp()
)
// ----------------------------------------------------

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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    // ----------------------------------------
    // NUEVA FUNCIÓN: verificar si está baneado
    // ----------------------------------------
    suspend fun isUserBanned(uid: String): Boolean {
        return try {
            val snap = db.collection("users").document(uid).get().await()
            snap.getBoolean("isBanned") ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ----------------------------------------
    // LOGIN
    // ----------------------------------------
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

                        //  Cambiado de "BANNED" a "USUARIO BANEADO"
                        if (isUserBanned(uid)) {
                            _state.update { it.copy(error = "USUARIO BANEADO") }
                            _events.send(AuthEvent.Error("USUARIO BANEADO"))
                            auth.signOut()
                            return@launch
                        }

                        val admin = checkAdmin(uid)
                        _events.send(AuthEvent.Success(admin))

                    } else {
                        _events.send(
                            AuthEvent.Error(
                                task.exception?.localizedMessage ?: "Error de autenticación"
                            )
                        )
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

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { authTask ->
                viewModelScope.launch {
                    _state.update { it.copy(loading = false) }

                    if (authTask.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        if (uid != null) {
                            val newUser = User(
                                uid = uid,
                                username = username,
                                email = email,
                                phone = phone,
                                isAdmin = false,
                                isBanned = false
                            )

                            db.collection("users").document(uid).set(newUser)
                                .addOnCompleteListener { firestoreTask ->
                                    viewModelScope.launch {

                                        if (firestoreTask.isSuccessful) {

                                            val profileUpdates =
                                                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                    .setDisplayName(username)
                                                    .build()

                                            user.updateProfile(profileUpdates)
                                                .addOnCompleteListener {
                                                    viewModelScope.launch {
                                                        _events.send(AuthEvent.Success(false))
                                                        onUidReceived(uid)
                                                    }
                                                }

                                        } else {
                                            _events.send(
                                                AuthEvent.Error(
                                                    firestoreTask.exception?.localizedMessage
                                                        ?: "Error guardando datos en Firestore."
                                                )
                                            )
                                            onUidReceived(null)
                                        }
                                    }
                                }
                        } else {
                            _events.send(AuthEvent.Error("Error interno: UID no disponible."))
                            onUidReceived(null)
                        }
                    } else {
                        _events.send(
                            AuthEvent.Error(
                                authTask.exception?.localizedMessage ?: "Error al crear cuenta"
                            )
                        )
                        onUidReceived(null)
                    }
                }
            }
    }
}