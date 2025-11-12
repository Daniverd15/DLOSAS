package com.example.proyecto

import TextMuted
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Reemplaza con el recurso de tu logo
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
    onBackToLoginClick: () -> Unit, // Aunque no está en la imagen, es buena práctica
    loading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo de Chevron
        // Reemplaza R.drawable.chevron_logo con el ID de tu recurso de logo
        Image(
            painter = painterResource(id = R.drawable.logo_chevron),
            contentDescription = "Chevron Logo",
            modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
        )

        Text(
            text = "Crear cuenta",
            color = ChevronRed,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 24.dp)
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
            label = "Telefono",
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Teléfono", tint = ChevronRed) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !loading
        )

        Spacer(Modifier.height(32.dp))

        // --- BOTÓN REGISTRARSE ---
        Button(
            onClick = onRegisterClick,
            enabled = !loading,
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChevronYellow,
                contentColor = ChevronRed // Color del texto
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(2.dp, ChevronRed, RoundedCornerShape(25.dp)) // Borde rojo
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

        Spacer(Modifier.height(16.dp))

        // Pie de página (Opcional, basado en la imagen)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            /* ---------- FOOTER ---------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.width(2.dp))
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
            .padding(vertical = 8.dp)
    )
}
