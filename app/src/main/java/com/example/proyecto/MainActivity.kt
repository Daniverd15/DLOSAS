package com.example.proyecto

import android.R
import android.os.Bundle
import android.util.Patterns
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.auth.AuthEvent
import com.example.proyecto.auth.AuthViewModel
import com.example.proyecto.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    HOME,
    PROFILE,
    EDIT_PROFILE,
    TALLER,
    ADMIN_PANEL,
    HISTORIAL,
    VEHICULOS,
    AGREGAR_VEHICULO,
    DETALLE_VEHICULO
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val vm: AuthViewModel = viewModel()
                val state by vm.state.collectAsState()
                val repo = remember { UserRepository() }

                val snackBarHost = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                var isAdmin by remember { mutableStateOf(false) }

                // Variable para almacenar el vehículo seleccionado
                var selectedVehiculo by remember { mutableStateOf<Vehiculo?>(null) }

                // Manejo de eventos
                LaunchedEffect(Unit) {
                    vm.events.collect { e ->
                        when (e) {
                            is AuthEvent.Success -> {
                                scope.launch {
                                    isAdmin = e.isAdmin
                                    snackBarHost.showSnackbar(
                                        if (e.isAdmin) "Bienvenido Admin" else "Inicio de sesión exitoso"
                                    )
                                    currentScreen = if (e.isAdmin) Screen.ADMIN_PANEL else Screen.HOME
                                }
                            }
                            is AuthEvent.Error -> {
                                scope.launch {
                                    snackBarHost.showSnackbar(e.message)
                                }
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackBarHost) }
                ) { padding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                    ) {
                        when (currentScreen) {
                            Screen.LOGIN -> {
                                LoginScreen(
                                    email = state.email,
                                    password = state.password,
                                    onEmailChange = vm::updateEmail,
                                    onPasswordChange = vm::updatePassword,
                                    onLoginClick = {
                                        when {
                                            state.email.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa tu correo") }
                                            }

                                            state.password.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa tu contraseña") }
                                            }

                                            state.password.length < 6 -> {
                                                scope.launch { snackBarHost.showSnackbar("La contraseña debe tener al menos 6 caracteres") }
                                            }

                                            else -> {
                                                vm.signIn { uid -> repo.isAdmin(uid) }
                                            }
                                        }
                                    },
                                    onForgotClick = {
                                        currentScreen = Screen.FORGOT_PASSWORD
                                    },
                                    onRegisterClick = {
                                        currentScreen = Screen.REGISTER
                                    },
                                    loading = state.loading,
                                    banPopup = state.error == "USUARIO BANEADO",
                                    onDismissBan = {
                                        vm.clearError() // Esto limpia el error y cierra el popup
                                    }
                                )
                            }

                            Screen.REGISTER -> {
                                RegisterScreen(
                                    email = state.email,
                                    password = state.password,
                                    username = state.username,
                                    confirmPassword = state.confirmPassword,
                                    phone = state.phone,
                                    onEmailChange = vm::updateEmail,
                                    onPasswordChange = vm::updatePassword,
                                    onUsernameChange = vm::updateUsername,
                                    onConfirmPasswordChange = vm::updateConfirmPassword,
                                    onPhoneChange = vm::updatePhone,
                                    onRegisterClick = {
                                        when {
                                            state.username.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa un nombre de usuario") }
                                            }
                                            state.email.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa tu correo") }
                                            }
                                            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa un correo válido") }
                                            }
                                            state.password.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa una contraseña") }
                                            }
                                            state.password.length < 6 -> {
                                                scope.launch { snackBarHost.showSnackbar("La contraseña debe tener al menos 6 caracteres") }
                                            }
                                            state.password != state.confirmPassword -> {
                                                scope.launch { snackBarHost.showSnackbar("Las contraseñas no coinciden") }
                                            }
                                            state.phone.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa un teléfono") }
                                            }
                                            else -> {
                                                vm.signUp { uid ->
                                                    if (uid != null) {
                                                        scope.launch {
                                                            snackBarHost.showSnackbar("Registro exitoso. Inicia sesión.")
                                                            currentScreen = Screen.LOGIN
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onBackToLoginClick = {
                                        currentScreen = Screen.LOGIN
                                    },
                                    loading = state.loading
                                )
                            }

                            Screen.FORGOT_PASSWORD -> {
                                ForgotPasswordScreen(
                                    email = state.email,
                                    onEmailChange = vm::updateEmail,
                                    onSendCodeClick = {
                                        when {
                                            state.email.isBlank() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa tu correo") }
                                            }
                                            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                                                scope.launch { snackBarHost.showSnackbar("Por favor ingresa un correo válido") }
                                            }
                                            else -> {
                                                vm.sendReset {
                                                    scope.launch {
                                                        snackBarHost.showSnackbar("Correo de recuperación enviado")
                                                        currentScreen = Screen.LOGIN
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onBackToLoginClick = {
                                        currentScreen = Screen.LOGIN
                                    },
                                    loading = state.loading
                                )
                            }

                            Screen.HOME -> {
                                HomeScreen(
                                    onNavigateToDomicilio = {
                                        supportFragmentManager.beginTransaction()
                                            .replace(
                                                R.id.content,
                                                DomicilioFragment()
                                            )
                                            .addToBackStack(null)
                                            .commit()
                                    },
                                    onNavigateToTaller = {
                                        currentScreen = Screen.TALLER
                                    },
                                    onNavigateToProfile = {
                                        currentScreen = Screen.PROFILE
                                    },
                                    onNavigateToHistorial = {
                                        currentScreen = Screen.HISTORIAL
                                    },
                                    onNavigateToVehiculos = {  //  NUEVO
                                        currentScreen = Screen.VEHICULOS
                                    },
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        isAdmin = false
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }

                            Screen.PROFILE -> {
                                ProfileScreen(
                                    onBackClick = {
                                        currentScreen = if (isAdmin) Screen.ADMIN_PANEL else Screen.HOME
                                    },
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        isAdmin = false
                                        currentScreen = Screen.LOGIN
                                    },
                                    onNavigateToEditProfile = {
                                        currentScreen = Screen.EDIT_PROFILE
                                    },
                                    onNavigateToChangePassword = {
                                        currentScreen = Screen.FORGOT_PASSWORD
                                    }
                                )
                            }

                            Screen.EDIT_PROFILE -> {
                                EditProfileScreen(
                                    onBackClick = {
                                        currentScreen = Screen.PROFILE
                                    },
                                    onProfileUpdated = {
                                        scope.launch {
                                            snackBarHost.showSnackbar("Perfil actualizado exitosamente")
                                        }
                                        currentScreen = Screen.PROFILE
                                    }
                                )
                            }

                            Screen.TALLER -> {
                                TallerScreen(
                                    onBackClick = {
                                        currentScreen = Screen.HOME
                                    }
                                )
                            }

                            Screen.ADMIN_PANEL -> {
                                AdminPanelScreen(
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        isAdmin = false
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }

                            Screen.HISTORIAL -> {
                                HistorialScreen(
                                    onBackClick = {
                                        currentScreen = Screen.HOME
                                    }
                                )
                            }

                            // PANTALLAS NUEVAS DE VEHÍCULOS
                            Screen.VEHICULOS -> {
                                VehiculosScreen(
                                    onBackClick = {
                                        currentScreen = Screen.HOME
                                    },
                                    onNavigateToAgregarVehiculo = {
                                        currentScreen = Screen.AGREGAR_VEHICULO
                                    },
                                    onVehiculoClick = { vehiculo ->
                                        selectedVehiculo = vehiculo
                                        currentScreen = Screen.DETALLE_VEHICULO
                                    }
                                )
                            }

                            Screen.AGREGAR_VEHICULO -> {
                                AgregarVehiculoScreen(
                                    onBackClick = {
                                        currentScreen = Screen.VEHICULOS
                                    },
                                    onVehiculoGuardado = {
                                        scope.launch {
                                            snackBarHost.showSnackbar("Vehículo guardado exitosamente")
                                        }
                                        currentScreen = Screen.VEHICULOS
                                    }
                                )
                            }

                            Screen.DETALLE_VEHICULO -> {
                                selectedVehiculo?.let { vehiculo ->
                                    DetalleVehiculoScreen(
                                        vehiculo = vehiculo,
                                        onBackClick = {
                                            currentScreen = Screen.VEHICULOS
                                        },
                                        onEditClick = {
                                            // TODO: Implementar edición
                                            scope.launch {
                                                snackBarHost.showSnackbar("Función de edición próximamente")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}