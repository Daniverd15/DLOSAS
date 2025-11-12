package com.example.proyecto

import android.os.Bundle
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
    TALLER,
    ADMIN_PANEL
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
                                        vm.signIn { uid -> repo.isAdmin(uid) }
                                    },
                                    onForgotClick = {
                                        currentScreen = Screen.FORGOT_PASSWORD
                                    },
                                    onRegisterClick = {
                                        currentScreen = Screen.REGISTER
                                    },
                                    loading = state.loading
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
                                        vm.signUp { uid ->
                                            if (uid != null) {
                                                scope.launch {
                                                    snackBarHost.showSnackbar("Registro exitoso. Inicia sesión.")
                                                    currentScreen = Screen.LOGIN
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
                                        vm.sendReset {
                                            scope.launch {
                                                snackBarHost.showSnackbar("Correo de recuperación enviado")
                                                currentScreen = Screen.LOGIN
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
                                        // Cambiar a Fragment para el mapa
                                        supportFragmentManager.beginTransaction()
                                            .replace(
                                                R.id.fragment_container,
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
                                    onBackClick = {
                                        currentScreen = Screen.HOME
                                    },
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        isAdmin = false
                                        currentScreen = Screen.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Manejar el botón de back
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}