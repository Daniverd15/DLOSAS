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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class HistorialItem(
    val id: String = "",
    val tipo: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val estado: String = "",
    val fecha: Date = Date(),
    val precio: Int = 0,
    val notas: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBackClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var historial by remember { mutableStateOf<List<HistorialItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Taller", "Domicilio")

    // Cargar historial del usuario
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val items = mutableListOf<HistorialItem>()

                android.util.Log.d("HISTORIAL", "Cargando historial para usuario: ${currentUser.uid}")

                // 1. Cargar reservas de taller
                try {
                    val reservas = db.collection("reservas")
                        .whereEqualTo("userId", currentUser.uid)
                        .get()
                        .await()

                    android.util.Log.d("HISTORIAL", "Reservas encontradas: ${reservas.size()}")

                    reservas.documents.forEach { doc ->
                        try {
                            val data = doc.data
                            android.util.Log.d("HISTORIAL", "Reserva ID: ${doc.id}, Data: $data")

                            val fecha = doc.getTimestamp("fechaReserva")?.toDate()
                                ?: doc.getTimestamp("fecha")?.toDate()
                                ?: doc.getTimestamp("timestamp")?.toDate()
                                ?: Date()

                            items.add(
                                HistorialItem(
                                    id = doc.id,
                                    tipo = "Taller",
                                    nombre = doc.getString("lubricentroNombre")
                                        ?: doc.getString("nombreLubricentro")
                                        ?: doc.getString("taller")
                                        ?: "Servicio de Taller",
                                    direccion = doc.getString("lubricentroDireccion")
                                        ?: doc.getString("direccion")
                                        ?: "Sin dirección",
                                    estado = doc.getString("estado") ?: "PENDIENTE",
                                    fecha = fecha,
                                    precio = (doc.getLong("precio") ?: doc.getLong("costo") ?: 0L).toInt(),
                                    notas = doc.getString("notas")
                                        ?: doc.getString("observaciones")
                                        ?: ""
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("HISTORIAL", "Error al parsear reserva ${doc.id}: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HISTORIAL", "Error al cargar reservas: ${e.message}")
                    errorMessage = "Error al cargar reservas de taller"
                }

                // 2. Cargar solicitudes de domicilio
                try {
                    val solicitudes = db.collection("solicitudes_domicilio")
                        .whereEqualTo("userId", currentUser.uid)
                        .get()
                        .await()

                    android.util.Log.d("HISTORIAL", "Solicitudes encontradas: ${solicitudes.size()}")

                    solicitudes.documents.forEach { doc ->
                        try {
                            val data = doc.data
                            android.util.Log.d("HISTORIAL", "Solicitud ID: ${doc.id}, Data: $data")

                            val fecha = doc.getTimestamp("fechaSolicitud")?.toDate()
                                ?: doc.getTimestamp("fecha")?.toDate()
                                ?: doc.getTimestamp("timestamp")?.toDate()
                                ?: Date()

                            items.add(
                                HistorialItem(
                                    id = doc.id,
                                    tipo = "Domicilio",
                                    nombre = "Servicio a Domicilio",
                                    direccion = doc.getString("direccion")
                                        ?: doc.getString("ubicacion")
                                        ?: "Sin dirección",
                                    estado = doc.getString("estado") ?: "PENDIENTE",
                                    fecha = fecha,
                                    precio = (doc.getLong("precio") ?: 0L).toInt(),
                                    notas = doc.getString("notas")
                                        ?: doc.getString("observaciones")
                                        ?: doc.getString("detalles")
                                        ?: ""
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("HISTORIAL", "Error al parsear solicitud ${doc.id}: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HISTORIAL", "Error al cargar solicitudes: ${e.message}")
                    errorMessage = "Error al cargar solicitudes de domicilio"
                }

                // 3. Intentar cargar de una colección genérica "servicios" si existe
                try {
                    val servicios = db.collection("servicios")
                        .whereEqualTo("userId", currentUser.uid)
                        .get()
                        .await()

                    android.util.Log.d("HISTORIAL", "Servicios encontrados: ${servicios.size()}")

                    servicios.documents.forEach { doc ->
                        try {
                            val data = doc.data
                            android.util.Log.d("HISTORIAL", "Servicio ID: ${doc.id}, Data: $data")

                            val fecha = doc.getTimestamp("fecha")?.toDate()
                                ?: doc.getTimestamp("timestamp")?.toDate()
                                ?: Date()

                            items.add(
                                HistorialItem(
                                    id = doc.id,
                                    tipo = doc.getString("tipo") ?: "Servicio",
                                    nombre = doc.getString("nombre") ?: "Servicio",
                                    direccion = doc.getString("direccion") ?: "Sin dirección",
                                    estado = doc.getString("estado") ?: "PENDIENTE",
                                    fecha = fecha,
                                    precio = (doc.getLong("precio") ?: 0L).toInt(),
                                    notas = doc.getString("notas") ?: ""
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("HISTORIAL", "Error al parsear servicio ${doc.id}: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.d("HISTORIAL", "No hay colección 'servicios' o error: ${e.message}")
                }

                historial = items.sortedByDescending { it.fecha }

                android.util.Log.d("HISTORIAL", "Total de items cargados: ${items.size}")

                if (items.isEmpty()) {
                    errorMessage = "No se encontraron servicios"
                }
            } else {
                errorMessage = "Usuario no autenticado"
            }
        } catch (e: Exception) {
            android.util.Log.e("HISTORIAL", "Error general al cargar historial: ${e.message}", e)
            errorMessage = "Error al cargar el historial"
        }
        isLoading = false
    }

    val filteredHistorial = remember(selectedFilter, historial) {
        when (selectedFilter) {
            "Taller" -> historial.filter { it.tipo == "Taller" }
            "Domicilio" -> historial.filter { it.tipo == "Domicilio" }
            else -> historial
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HavolineYellow)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Estadísticas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(
                            label = "Total",
                            value = historial.size.toString(),
                            color = ChevronBlue
                        )
                        StatChip(
                            label = "Talleres",
                            value = historial.count { it.tipo == "Taller" }.toString(),
                            color = Color(0xFF2196F3)
                        )
                        StatChip(
                            label = "Domicilios",
                            value = historial.count { it.tipo == "Domicilio" }.toString(),
                            color = Color(0xFFFF9800)
                        )
                    }

                    // Mensaje de error si existe
                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    errorMessage ?: "",
                                    fontSize = 13.sp,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Filtros
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedFilter == filter) HavolineYellow else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (selectedFilter == filter) 2.dp else 1.dp,
                                    color = if (selectedFilter == filter) HavolineYellow else Color.LightGray
                                ),
                                onClick = { selectedFilter = filter }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        filter,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFilter == filter) ChevronBlue else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Lista de historial
                    if (filteredHistorial.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No hay servicios registrados",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tus reservas aparecerán aquí",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredHistorial) { item ->
                                HistorialCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
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
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
fun HistorialCard(item: HistorialItem) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tipo de servicio
                Surface(
                    color = if (item.tipo == "Taller") ChevronBlue else Color(0xFFFF9800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (item.tipo == "Taller") Icons.Default.Build else Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            item.tipo,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Estado
                Surface(
                    color = when (item.estado) {
                        "PENDIENTE" -> HavolineYellow
                        "EN_PROCESO" -> Color(0xFF2196F3)
                        "COMPLETADO" -> Color(0xFF4CAF50)
                        "CANCELADO" -> Color(0xFFB00020)
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when (item.estado) {
                            "PENDIENTE" -> "Pendiente"
                            "EN_PROCESO" -> "En Proceso"
                            "COMPLETADO" -> "Completado"
                            "CANCELADO" -> "Cancelado"
                            else -> item.estado
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (item.estado == "PENDIENTE") ChevronBlue else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Nombre del servicio
            Text(
                item.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            // Dirección
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = HavolineYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    item.direccion,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(6.dp))

            // Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = HavolineYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    dateFormat.format(item.fecha),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Precio (solo para talleres)
            if (item.precio > 0) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = HavolineYellow.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = ChevronBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "$${item.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}",
                            fontSize = 15.sp,
                            color = ChevronBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Notas
            if (item.notas.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Cream,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ChevronBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.notas,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}