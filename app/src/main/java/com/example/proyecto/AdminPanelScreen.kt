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
import java.text.SimpleDateFormat
import java.util.*

data class Solicitud(
    val id: String,
    val clienteNombre: String,
    val clienteTelefono: String,
    val direccion: String,
    val tipoServicio: String, // "Domicilio" o "Taller"
    val estado: EstadoSolicitud,
    val fecha: Date,
    val precio: Int,
    val notas: String = ""
)

enum class EstadoSolicitud {
    PENDIENTE,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    // Solicitudes de ejemplo
    val solicitudes = remember {
        mutableStateListOf(
            Solicitud(
                "001",
                "Juan Pérez",
                "310-555-0123",
                "Calle 45 #23-10, Bucaramanga",
                "Domicilio",
                EstadoSolicitud.PENDIENTE,
                Date(),
                85000,
                "Cambio de aceite sintético"
            ),
            Solicitud(
                "002",
                "María González",
                "320-555-0456",
                "Carrera 27 #36-15, Floridablanca",
                "Domicilio",
                EstadoSolicitud.EN_PROCESO,
                Date(System.currentTimeMillis() - 3600000),
                78000,
                "Aceite 20W-50"
            ),
            Solicitud(
                "003",
                "Carlos Rodríguez",
                "315-555-0789",
                "Lubricentro Havoline Centro",
                "Taller",
                EstadoSolicitud.COMPLETADO,
                Date(System.currentTimeMillis() - 7200000),
                92000,
                "Cambio completo + filtro"
            )
        )
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Todas", "Pendientes", "En Proceso", "Completadas")

    val filteredSolicitudes = remember(selectedTab, solicitudes.toList()) {
        when (selectedTab) {
            1 -> solicitudes.filter { it.estado == EstadoSolicitud.PENDIENTE }
            2 -> solicitudes.filter { it.estado == EstadoSolicitud.EN_PROCESO }
            3 -> solicitudes.filter { it.estado == EstadoSolicitud.COMPLETADO }
            else -> solicitudes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChevronBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            // Estadísticas rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    "Pendientes",
                    solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE }.toString(),
                    Color(0xFFFF9800)
                )
                StatCard(
                    "En Proceso",
                    solicitudes.count { it.estado == EstadoSolicitud.EN_PROCESO }.toString(),
                    Color(0xFF2196F3)
                )
                StatCard(
                    "Completadas",
                    solicitudes.count { it.estado == EstadoSolicitud.COMPLETADO }.toString(),
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
                            val index = solicitudes.indexOfFirst { it.id == solicitud.id }
                            if (index != -1) {
                                solicitudes[index] = solicitud.copy(estado = nuevoEstado)
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
    onEstadoChange: (EstadoSolicitud) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

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
                        "Solicitud #${solicitud.id}",
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
                        EstadoSolicitud.PENDIENTE -> Color(0xFFFF9800)
                        EstadoSolicitud.EN_PROCESO -> Color(0xFF2196F3)
                        EstadoSolicitud.COMPLETADO -> Color(0xFF4CAF50)
                        EstadoSolicitud.CANCELADO -> Color(0xFFB00020)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when (solicitud.estado) {
                            EstadoSolicitud.PENDIENTE -> "Pendiente"
                            EstadoSolicitud.EN_PROCESO -> "En Proceso"
                            EstadoSolicitud.COMPLETADO -> "Completado"
                            EstadoSolicitud.CANCELADO -> "Cancelado"
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
            InfoRow(Icons.Default.Phone, solicitud.clienteTelefono)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.LocationOn, solicitud.direccion)
            Spacer(Modifier.height(6.dp))
            InfoRow(
                if (solicitud.tipoServicio == "Domicilio") Icons.Default.Home else Icons.Default.Build,
                solicitud.tipoServicio
            )
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.AttachMoney, "$${solicitud.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}")

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
                    EstadoSolicitud.values().forEach { estado ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (estado) {
                                        EstadoSolicitud.PENDIENTE -> "Pendiente"
                                        EstadoSolicitud.EN_PROCESO -> "En Proceso"
                                        EstadoSolicitud.COMPLETADO -> "Completado"
                                        EstadoSolicitud.CANCELADO -> "Cancelado"
                                    }
                                )
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