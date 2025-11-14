package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Estados para las estadísticas
    var totalServicios by remember { mutableStateOf(0) }
    var totalReservas by remember { mutableStateOf(0) }
    var puntosAcumulados by remember { mutableStateOf(0) }

    // Cargar datos de Firestore
    LaunchedEffect(Unit) {
        try {
            val uid = currentUser?.uid
            if (uid != null) {
                // Cargar datos del usuario
                val document = db.collection("users").document(uid).get().await()
                userData = document.data

                // Cargar estadísticas de actividad
                // 1. Contar reservas de taller
                val reservasSnapshot = db.collection("reservas")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                val cantidadReservas = reservasSnapshot.size()

                // 2. Contar solicitudes de domicilio
                val solicitudesSnapshot = db.collection("solicitudes_domicilio")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                val cantidadSolicitudes = solicitudesSnapshot.size()

                // 3. Contar servicios de colección genérica (si existe)
                var cantidadServicios = 0
                try {
                    val serviciosSnapshot = db.collection("servicios")
                        .whereEqualTo("userId", uid)
                        .get()
                        .await()
                    cantidadServicios = serviciosSnapshot.size()
                } catch (e: Exception) {
                    // Colección no existe o error
                }

                // Calcular totales
                totalReservas = cantidadReservas
                totalServicios = cantidadReservas + cantidadSolicitudes + cantidadServicios

                // Calcular puntos (10 puntos por cada servicio completado)
                val completados = reservasSnapshot.documents.count {
                    it.getString("estado") == "COMPLETADO"
                } + solicitudesSnapshot.documents.count {
                    it.getString("estado") == "COMPLETADO"
                }
                puntosAcumulados = completados * 10

                android.util.Log.d("PROFILE_STATS", "Reservas: $cantidadReservas, Solicitudes: $cantidadSolicitudes, Total: $totalServicios, Puntos: $puntosAcumulados")
            }
        } catch (e: Exception) {
            android.util.Log.e("PROFILE_ERROR", "Error al cargar datos: ${e.message}")
        } finally {
            loading = false
        }
    }

    // Usar datos de Firestore si están disponibles, sino usar datos de FirebaseAuth
    val displayName = userData?.get("username") as? String ?: currentUser?.displayName ?: "Usuario"
    val email = userData?.get("email") as? String ?: currentUser?.email ?: "correo@ejemplo.com"
    val phoneNumber = userData?.get("phone") as? String ?: "No disponible"
    val isAdmin = userData?.get("isAdmin") as? Boolean ?: false
    val createdAt = userData?.get("createdAt") as? com.google.firebase.Timestamp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
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
        }
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
                // Avatar del usuario con badge de admin
                Box(
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(HavolineYellow)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(80.dp),
                            tint = ChevronBlue
                        )
                    }

                    // Badge de Admin
                    if (isAdmin) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp),
                            color = Color(0xFFB00020),
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Admin",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (isAdmin) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFB00020),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ADMINISTRADOR",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Sección: Información Personal
                SectionHeader("Información Personal")
                Spacer(Modifier.height(12.dp))

                ProfileInfoCard(
                    icon = Icons.Default.Email,
                    label = "Correo electrónico",
                    value = email
                )

                Spacer(Modifier.height(12.dp))

                ProfileInfoCard(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = phoneNumber
                )

                Spacer(Modifier.height(12.dp))

                ProfileInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "ID de Usuario",
                    value = currentUser?.uid?.take(8) ?: "N/A"
                )

                // Sección: Cuenta
                Spacer(Modifier.height(32.dp))
                SectionHeader("Configuración de Cuenta")
                Spacer(Modifier.height(12.dp))

                // Botón Editar Perfil
                Button(
                    onClick = onNavigateToEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = ChevronBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("EDITAR PERFIL", color = ChevronBlue, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                // Botón Cambiar Contraseña
                OutlinedButton(
                    onClick = onNavigateToChangePassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CAMBIAR CONTRASEÑA", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))

                // Sección: Estadísticas (AHORA CON DATOS REALES)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = HavolineYellow,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Actividad",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Servicios", totalServicios.toString())
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            StatItem("Puntos", puntosAcumulados.toString())
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            StatItem("Reservas", totalReservas.toString())
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Botón Cerrar Sesión
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                // Información de cuenta
                if (createdAt != null) {
                    Text(
                        text = "Miembro desde ${formatDate(createdAt)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(HavolineYellow, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ChevronBlue
        )
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(HavolineYellow.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HavolineYellow,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ChevronBlue
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val calendar = java.util.Calendar.getInstance().apply { time = date }
    val months = arrayOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )
    return "${months[calendar.get(java.util.Calendar.MONTH)]} ${calendar.get(java.util.Calendar.YEAR)}"
}