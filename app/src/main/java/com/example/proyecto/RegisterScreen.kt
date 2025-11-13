package com.example.proyecto

import TextMuted
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto.R

// Colores aproximados del diseño
val ChevronRed = Color(0xFFC00030)
val ChevronBlue = Color(0xFF003067)
val ChevronYellow = Color(0xFFFFC000)
val InputBorderColor = Color(0xFFFFC000)

@Composable
fun RegisterScreen(
    email: String,
    password: String,
    username: String,
    confirmPassword: String,
    phone: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    loading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenido principal centrado vertical y horizontalmente
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo de Chevron
                Image(
                    painter = painterResource(id = R.drawable.logo_chevron),
                    contentDescription = "Chevron Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "Crear cuenta",
                    color = ChevronRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // --- CAMPOS DE TEXTO ---

                // 1. Usuario
                AuthInputField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = "Usuario",
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Usuario", tint = ChevronRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    enabled = !loading
                )

                // 2. Email
                AuthInputField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email", tint = ChevronRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !loading
                )

                // 3. Contraseña
                AuthInputField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña", tint = ChevronRed) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !loading
                )

                // 4. Confirma tu contraseña
                AuthInputField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Confirma tu contraseña",
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirmar Contraseña", tint = ChevronRed) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !loading
                )

                // 5. Teléfono
                AuthInputField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = "Teléfono",
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Teléfono", tint = ChevronRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !loading
                )

                Spacer(Modifier.height(24.dp))

                // --- BOTÓN REGISTRARSE ---
                Button(
                    onClick = onRegisterClick,
                    enabled = !loading,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChevronYellow,
                        contentColor = ChevronRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(2.dp, ChevronRed, RoundedCornerShape(25.dp))
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = ChevronRed,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("REGÍSTRATE", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ChevronRed)
                    }
                }
            }

            // --- FOOTER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                runCatching {
                    Image(
                        painter = painterResource(id = R.drawable.logo_losas),
                        contentDescription = "LOSAS",
                        modifier = Modifier.height(22.dp),
                        contentScale = ContentScale.Fit
                    )
                }.onFailure { Text("LOSAS", color = TextMuted, fontSize = 12.sp) }

                runCatching {
                    Image(
                        painter = painterResource(id = R.drawable.logo_sello),
                        contentDescription = "Sello",
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.Fit
                    )
                }.onFailure { Text("●", color = TextMuted, fontSize = 12.sp) }
            }
        }
    }
}

// Composable de Input Personalizado para replicar el estilo de la imagen
@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit),
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        leadingIcon = leadingIcon,
        singleLine = true,
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputBorderColor,
            unfocusedBorderColor = InputBorderColor,
            cursorColor = ChevronBlue,
            focusedLeadingIconColor = ChevronRed,
            unfocusedLeadingIconColor = ChevronRed
        ),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}