package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextMuted
import TextPrimary
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto.ui.theme.*

@Composable
fun LoginScreen(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotClick: () -> Unit,
    onRegisterClick: () -> Unit,
    loading: Boolean
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
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
                    /* ---------- HEADER (logo) ---------- */
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        runCatching {
                            Image(
                                painter = painterResource(id = R.drawable.logo_chevron),
                                contentDescription = "Chevron",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        }.onFailure {
                            Text("Chevron", color = ChevronBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    /* ---------- TÍTULO ---------- */
                    Text(
                        text = "Inicia sesión",
                        color = Color(0xFFB00020),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(32.dp))

                    /* ---------- FORM ---------- */
                    // Usuario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Person, contentDescription = null,
                            tint = Color(0xFF8A0E0E), modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        YellowPillField(
                            value = email,
                            onValueChange = onEmailChange,
                            placeholder = "Usuario",
                            isPassword = false,
                            height = 46.dp,
                            stroke = 3.dp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Contraseña
                    var hidden by remember { mutableStateOf(true) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Lock, contentDescription = null,
                            tint = Color(0xFF8A0E0E), modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        YellowPillField(
                            value = password,
                            onValueChange = onPasswordChange,
                            placeholder = "Contraseña",
                            isPassword = true,
                            hidden = hidden,
                            onToggleHidden = { hidden = !hidden },
                            height = 46.dp,
                            stroke = 3.dp
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // Botón + subrayado rojo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onLoginClick,
                            enabled = !loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(6.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HavolineYellow,
                                contentColor = Color(0xFF202020)
                            )
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF202020),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text("INICIA SESIÓN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        // línea roja solo del botón
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .width(165.dp)
                                .height(4.dp)
                                .background(Color(0xFFB00020), RoundedCornerShape(50))
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onForgotClick() }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Registrate",
                        color = Color(0xFFB00020),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onRegisterClick() }
                    )
                }

                /* ---------- FOOTER ---------- */
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
}

/* ---------- Auxiliares ---------- */
@Composable
private fun YellowPillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean,
    hidden: Boolean = true,
    onToggleHidden: (() -> Unit)? = null,
    height: Dp = 46.dp,
    stroke: Dp = 3.dp
) {
    val shape = CircleShape
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
        cursorBrush = SolidColor(ChevronBlue),
        visualTransformation = if (isPassword && hidden) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(Color.White, shape)
                    .border(stroke, HavolineYellow, shape)
                    .padding(horizontal = 14.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 15.sp)
                    inner()
                }
                if (isPassword && onToggleHidden != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (hidden) "• • •" else "ABC",
                        color = TextMuted, fontSize = 13.sp,
                        modifier = Modifier.clickable { onToggleHidden() }
                    )
                }
            }
        }
    )
}