package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextMuted
import TextPrimary
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar datos actuales
    LaunchedEffect(Unit) {
        try {
            val uid = currentUser?.uid
            if (uid != null) {
                val document = db.collection("users").document(uid).get().await()
                username = document.getString("username") ?: ""
                phone = document.getString("phone") ?: ""
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar datos"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChevronBlue,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HavolineYellow)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
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
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = ChevronBlue,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Actualiza tu información",
                    color = Color(0xFFB00020),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(32.dp))

                // Campo Nombre de usuario
                Text(
                    text = "Nombre de usuario",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF8A0E0E),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    YellowPillField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "Nombre de usuario",
                        height = 46.dp,
                        stroke = 3.dp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Campo Teléfono
                Text(
                    text = "Número de teléfono",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF8A0E0E),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    YellowPillField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Teléfono",
                        height = 46.dp,
                        stroke = 3.dp,
                        keyboardType = KeyboardType.Phone
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Botón Guardar Cambios
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (username.isBlank() || phone.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Completa todos los campos")
                                }
                                return@Button
                            }

                            saving = true
                            val uid = currentUser?.uid

                            if (uid != null) {
                                db.collection("users").document(uid)
                                    .update(
                                        mapOf(
                                            "username" to username,
                                            "phone" to phone
                                        )
                                    )
                                    .addOnSuccessListener {
                                        // Actualizar displayName en Auth
                                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build()

                                        currentUser.updateProfile(profileUpdates)
                                            .addOnSuccessListener {
                                                saving = false
                                                showSuccessDialog = true
                                            }
                                            .addOnFailureListener {
                                                saving = false
                                                showSuccessDialog = true
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        saving = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                e.localizedMessage ?: "Error al guardar cambios"
                                            )
                                        }
                                    }
                            }
                        },
                        enabled = !saving,
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
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF202020),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (saving) "Guardando..." else "GUARDAR CAMBIOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .width(165.dp)
                            .height(4.dp)
                            .background(Color(0xFFB00020), RoundedCornerShape(50))
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ChevronBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Tu correo electrónico no puede ser modificado desde aquí",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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

        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onProfileUpdated()
                },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        "¡Perfil Actualizado!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        "Tus cambios han sido guardados exitosamente.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onProfileUpdated()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow)
                    ) {
                        Text("ENTENDIDO", color = ChevronBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
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