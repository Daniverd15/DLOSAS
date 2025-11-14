package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Solicitud(
    val id: String = "",
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    val direccion: String = "",
    val tipoServicio: String = "", // "Domicilio" o "Taller"
    val estado: String = "PENDIENTE",
    val fecha: Date = Date(),
    val precio: Int = 0,
    val notas: String = "",
    val userEmail: String = "",
    val coleccion: String = "" // Para saber de dónde viene
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var solicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val tabs = listOf("Todas", "Pendientes", "En Proceso", "Completadas")

    // Cargar solicitudes desde Firestore
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val todasSolicitudes = mutableListOf<Solicitud>()

            // 1. Cargar reservas de talleres
            val reservasTaller = db.collection("reservas")
                .orderBy("fechaReserva", Query.Direction.DESCENDING)
                .get()
                .await()

            reservasTaller.documents.forEach { doc ->
                try {
                    val solicitud = Solicitud(
                        id = doc.getString("reservaId") ?: doc.id,
                        clienteNombre = doc.getString("userName") ?: "Sin nombre",
                        clienteTelefono = doc.getString("userPhone") ?: "Sin teléfono",
                        direccion = doc.getString("lubricentroDireccion") ?: "Sin dirección",
                        tipoServicio = "Taller - ${doc.getString("lubricentroNombre") ?: ""}",
                        estado = doc.getString("estado") ?: "PENDIENTE",
                        fecha = doc.getTimestamp("fechaReserva")?.toDate() ?: Date(),
                        precio = doc.getLong("precio")?.toInt() ?: 0,
                        notas = doc.getString("notas") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        coleccion = "reservas"
                    )
                    todasSolicitudes.add(solicitud)
                } catch (e: Exception) {
                    android.util.Log.e("ADMIN_ERROR", "Error al parsear reserva: ${e.message}")
                }
            }

            // 2. Cargar solicitudes de domicilio
            val solicitudesDomicilio = db.collection("solicitudes_domicilio")
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            solicitudesDomicilio.documents.forEach { doc ->
                try {
                    val solicitud = Solicitud(
                        id = doc.getString("solicitudId") ?: doc.id,
                        clienteNombre = doc.getString("userName") ?: "Sin nombre",
                        clienteTelefono = doc.getString("telefonoContacto") ?: doc.getString("userPhone") ?: "Sin teléfono",
                        direccion = doc.getString("direccion") ?: "Sin dirección",
                        tipoServicio = "Domicilio",
                        estado = doc.getString("estado") ?: "PENDIENTE",
                        fecha = doc.getTimestamp("fechaSolicitud")?.toDate() ?: Date(),
                        precio = 0, // Domicilio no tiene precio predefinido
                        notas = doc.getString("notas") ?: "",
                        userEmail = doc.getString("userEmail") ?: "",
                        coleccion = "solicitudes_domicilio"
                    )
                    todasSolicitudes.add(solicitud)
                } catch (e: Exception) {
                    android.util.Log.e("ADMIN_ERROR", "Error al parsear solicitud: ${e.message}")
                }
            }

            // Ordenar por fecha más reciente
            solicitudes = todasSolicitudes.sortedByDescending { it.fecha }

        } catch (e: Exception) {
            android.util.Log.e("ADMIN_ERROR", "Error al cargar solicitudes: ${e.message}")
        }
        isLoading = false
    }

    val filteredSolicitudes = remember(selectedTab, solicitudes) {
        when (selectedTab) {
            1 -> solicitudes.filter { it.estado == "PENDIENTE" }
            2 -> solicitudes.filter { it.estado == "EN_PROCESO" }
            3 -> solicitudes.filter { it.estado == "COMPLETADO" }
            else -> solicitudes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador", fontWeight = FontWeight.Bold) },
                actions = {
                    // Botón refrescar
                    IconButton(onClick = {
                        refreshTrigger++
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                    // Botón cerrar sesión
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChevronBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            if (isLoading) {
                // Indicador de carga
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HavolineYellow)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Estadísticas rápidas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            "Pendientes",
                            solicitudes.count { it.estado == "PENDIENTE" }.toString(),
                            Color(0xFFFF9800)
                        )
                        StatCard(
                            "En Proceso",
                            solicitudes.count { it.estado == "EN_PROCESO" }.toString(),
                            Color(0xFF2196F3)
                        )
                        StatCard(
                            "Completadas",
                            solicitudes.count { it.estado == "COMPLETADO" }.toString(),
                            Color(0xFF4CAF50)
                        )
                    }

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = ChevronBlue,
                        edgePadding = 16.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Lista de solicitudes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(filteredSolicitudes) { solicitud ->
                            SolicitudCard(
                                solicitud = solicitud,
                                onEstadoChange = { nuevoEstado ->
                                    scope.launch {
                                        try {
                                            // Actualizar en Firestore
                                            db.collection(solicitud.coleccion)
                                                .document(solicitud.id)
                                                .update("estado", nuevoEstado)
                                                .await()

                                            // Refrescar la lista
                                            refreshTrigger++
                                        } catch (e: Exception) {
                                            android.util.Log.e("ADMIN_ERROR", "Error al actualizar estado: ${e.message}")
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        if (filteredSolicitudes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "No hay solicitudes en esta categoría",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(105.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SolicitudCard(
    solicitud: Solicitud,
    onEstadoChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val estados = listOf("PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Solicitud #${solicitud.id.take(8)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        dateFormat.format(solicitud.fecha),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Estado badge
                Surface(
                    color = when (solicitud.estado) {
                        "PENDIENTE" -> Color(0xFFFF9800)
                        "EN_PROCESO" -> Color(0xFF2196F3)
                        "COMPLETADO" -> Color(0xFF4CAF50)
                        "CANCELADO" -> Color(0xFFB00020)
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when (solicitud.estado) {
                            "PENDIENTE" -> "Pendiente"
                            "EN_PROCESO" -> "En Proceso"
                            "COMPLETADO" -> "Completado"
                            "CANCELADO" -> "Cancelado"
                            else -> solicitud.estado
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Información del cliente
            InfoRow(Icons.Default.Person, solicitud.clienteNombre)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.Email, solicitud.userEmail)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.Phone, solicitud.clienteTelefono)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.LocationOn, solicitud.direccion)
            Spacer(Modifier.height(6.dp))
            InfoRow(
                if (solicitud.tipoServicio.contains("Domicilio")) Icons.Default.Home else Icons.Default.Build,
                solicitud.tipoServicio
            )

            if (solicitud.precio > 0) {
                Spacer(Modifier.height(6.dp))
                InfoRow(
                    Icons.Default.AttachMoney,
                    "$${solicitud.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}"
                )
            }

            if (solicitud.notas.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                InfoRow(Icons.Default.Info, solicitud.notas)
            }

            // Botón de cambiar estado
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CAMBIAR ESTADO", color = ChevronBlue, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ChevronBlue)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    estados.forEach { estado ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Indicador de color
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = when (estado) {
                                                    "PENDIENTE" -> Color(0xFFFF9800)
                                                    "EN_PROCESO" -> Color(0xFF2196F3)
                                                    "COMPLETADO" -> Color(0xFF4CAF50)
                                                    "CANCELADO" -> Color(0xFFB00020)
                                                    else -> Color.Gray
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        when (estado) {
                                            "PENDIENTE" -> "Pendiente"
                                            "EN_PROCESO" -> "En Proceso"
                                            "COMPLETADO" -> "Completado"
                                            "CANCELADO" -> "Cancelado"
                                            else -> estado
                                        }
                                    )
                                }
                            },
                            onClick = {
                                onEstadoChange(estado)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = HavolineYellow,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextPrimary)
    }
}