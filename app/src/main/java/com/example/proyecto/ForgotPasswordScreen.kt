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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    loading: Boolean
) {
    var emailSent by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            /* ---------- HEADER (logo) ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.28f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
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
                        Text("Chevron", color = ChevronBlue, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
            }

            /* ---------- TÍTULO ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (emailSent) "¡CORREO ENVIADO!" else "¿OLVIDASTE TU\nCONTRASEÑA?",
                    color = if (emailSent) Color(0xFF4CAF50) else Color(0xFFB00020),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            /* ---------- FORM ---------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                if (!emailSent) {
                    Text(
                        text = "Restáurala aquí",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Correo electrónico
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF8A0E0E),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        YellowPillField(
                            value = email,
                            onValueChange = onEmailChange,
                            placeholder = "Correo electrónico",
                            height = 46.dp,
                            stroke = 3.dp,
                            keyboardType = KeyboardType.Email
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Te enviaremos un enlace para\nrestablecer tu contraseña",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                        lineHeight = 18.sp
                    )

                    // Botón de enviar código
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    onSendCodeClick()
                                    emailSent = true
                                }
                            },
                            enabled = !loading && email.isNotBlank(),
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
                            Text("ENVIAR ENLACE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .width(165.dp)
                                .height(4.dp)
                                .background(Color(0xFFB00020), RoundedCornerShape(50))
                        )
                    }
                } else {
                    // Mensaje de éxito
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Revisa tu correo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Hemos enviado un enlace de recuperación a:",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ChevronBlue,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "El enlace expirará en 1 hora.\nSi no ves el correo, revisa tu carpeta de spam.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = {
                                onSendCodeClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue)
                        ) {
                            Text("REENVIAR CORREO", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Volver al inicio",
                    color = Color(0xFFB00020),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        emailSent = false
                        onBackToLoginClick()
                    }
                )

                if (!emailSent) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "OPCIONAL",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

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

@Composable
private fun YellowPillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    height: Dp = 46.dp,
    stroke: Dp = 3.dp,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val shape = CircleShape
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
        cursorBrush = SolidColor(ChevronBlue),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
            }
        }
    )
}