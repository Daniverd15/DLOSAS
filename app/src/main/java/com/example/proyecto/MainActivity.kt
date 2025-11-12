package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.ui.theme.DomicilioFragment
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.auth.AuthEvent
import com.example.proyecto.auth.AuthViewModel
import com.example.proyecto.ui.theme.AppTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DomicilioFragment())
                .commit()
        }
        setContent {
            AppTheme {
                val vm: AuthViewModel = viewModel()
                val state by vm.state.collectAsState()
                val repo = remember { UserRepository() }
                val snackBar = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    vm.events.collect { e ->
                        when (e) {
                            is AuthEvent.Success -> {
                                // TODO: navegar a Admin o Home usuario
                                scope.launch {
                                    snackBar.showSnackbar(
                                        if (e.isAdmin) "Admin OK" else "Login OK"
                                    )
                                }
                            }
                            is AuthEvent.Error -> scope.launch {
                                snackBar.showSnackbar(e.message)
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackBar) }
                ) { padding ->   // <-- usa el padding que entrega Scaffold
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .padding(padding) // <-- aplicado aquí
                    ) {
                        LoginScreen(
                            email = state.email,
                            password = state.password,
                            onEmailChange = vm::updateEmail,
                            onPasswordChange = vm::updatePassword,
                            onLoginClick = { vm.signIn { uid -> repo.isAdmin(uid) } },
                            onForgotClick = {
                                vm.sendReset {
                                    scope.launch { snackBar.showSnackbar("Correo enviado") }
                                }
                            },
                            onRegisterClick = { /* TODO: ir a registro */ },
                            loading = state.loading
                        )
                    }
                }
            }
        }
    }

}
