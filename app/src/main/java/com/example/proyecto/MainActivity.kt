package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.auth.AuthEvent
import com.example.proyecto.auth.AuthViewModel
import com.example.proyecto.LoginScreen
import com.example.proyecto.RegisterScreen
import com.example.proyecto.ui.theme.AppTheme
import kotlinx.coroutines.launch

// 1. Define las pantallas de autenticación
enum class AuthScreen {
    LOGIN, REGISTER, HOME // Agrega HOME para una navegación futura
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val vm: AuthViewModel = viewModel()
                val state by vm.state.collectAsState()

                // Nota: Reemplaza esto con tu repositorio real que verifica el rol de admin
                val repo = remember { UserRepository() }

                val snackBar = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // 2. Estado de navegación
                var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

                // 3. Manejo de Eventos (SnackBar y Navegación)
                LaunchedEffect(Unit) {
                    vm.events.collect { e ->
                        when (e) {
                            is AuthEvent.Success -> {
                                scope.launch {
                                    // Muestra mensaje de éxito y navega
                                    snackBar.showSnackbar(
                                        if (e.isAdmin) "Admin OK" else "Login OK"
                                    )
                                    // Navega a la pantalla principal
                                    currentScreen = AuthScreen.HOME
                                }
                            }
                            is AuthEvent.Error -> scope.launch {
                                // Muestra el mensaje de error en el SnackBar
                                snackBar.showSnackbar(e.message)
                            }
                        }
                    }
                }

                // 4. Estructura principal con Scaffold
                Scaffold(
                    snackbarHost = { SnackbarHost(snackBar) }
                ) { padding ->
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // 5. Lógica de Navegación Condicional
                        when (currentScreen) {
                            AuthScreen.LOGIN -> {
                                LoginScreen(
                                    email = state.email,
                                    password = state.password,
                                    onEmailChange = vm::updateEmail,
                                    onPasswordChange = vm::updatePassword,
                                    onLoginClick = {
                                        vm.signIn { uid ->
                                            // Lógica para verificar si es admin
                                            repo.isAdmin(uid)
                                        }
                                    },
                                    onForgotClick = {
                                        vm.sendReset {
                                            scope.launch { snackBar.showSnackbar("Correo de recuperación enviado") }
                                        }
                                    },
                                    // Mueve a la pantalla de registro
                                    onRegisterClick = { currentScreen = AuthScreen.REGISTER },
                                    loading = state.loading
                                )
                            }

                            AuthScreen.REGISTER -> {
                                // 🚀 IMPLEMENTACIÓN COMPLETA DE REGISTRO
                                RegisterScreen(
                                    email = state.email,
                                    password = state.password,
                                    // Pasando los nuevos campos del estado
                                    username = state.username,
                                    confirmPassword = state.confirmPassword,
                                    phone = state.phone,

                                    onEmailChange = vm::updateEmail,
                                    onPasswordChange = vm::updatePassword,
                                    // Pasando las nuevas funciones de actualización
                                    onUsernameChange = vm::updateUsername,
                                    onConfirmPasswordChange = vm::updateConfirmPassword,
                                    onPhoneChange = vm::updatePhone,

                                    onRegisterClick = {
                                        vm.signUp { uid ->
                                            // Si el registro es exitoso, vuelve a Login para iniciar sesión
                                            if (uid != null) {
                                                scope.launch { snackBar.showSnackbar("Registro exitoso. Inicia sesión.") }
                                                currentScreen = AuthScreen.LOGIN
                                            }
                                        }
                                    },
                                    // Vuelve a la pantalla de login
                                    onBackToLoginClick = { currentScreen = AuthScreen.LOGIN },
                                    loading = state.loading
                                )
                            }

                            AuthScreen.HOME -> {
                                // Aquí iría tu Composable principal (Ej. HomeScreen)
                                Text(
                                    text = "¡Bienvenido a la App!",
                                    modifier = androidx.compose.ui.Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
