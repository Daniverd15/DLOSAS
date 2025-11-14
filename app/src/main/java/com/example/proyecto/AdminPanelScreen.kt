package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Modelos de datos
data class Solicitud(
    val id: String = "",
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    val direccion: String = "",
    val tipoServicio: String = "",
    val estado: String = "PENDIENTE",
    val fecha: Date = Date(),
    val precio: Int = 0,
    val notas: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val coleccion: String = ""
)

data class Usuario(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Date? = null,
    val totalServicios: Int = 0
)

data class LubricentroData(
    val id: Int = 0,
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val horario: String = "",
    val disponible: Boolean = true,
    val calificacion: Float = 0f,
    val precio: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var selectedMainTab by remember { mutableStateOf(0) }
    val mainTabs = listOf("Pedidos", "Usuarios", "Lubricentros")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador", fontWeight = FontWeight.Bold) },
                actions = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            // Tabs principales
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = Color.White,
                contentColor = ChevronBlue
            ) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedMainTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Contenido según tab seleccionado
            when (selectedMainTab) {
                0 -> PedidosTab(db, scope)
                1 -> UsuariosTab(db, scope)
                2 -> LubricentrosTab()
            }
        }
    }
}

// ==================== TAB DE PEDIDOS ====================
@Composable
fun PedidosTab(db: FirebaseFirestore, scope: kotlinx.coroutines.CoroutineScope) {
    var solicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val tabs = listOf("Todas", "Pendientes", "En Proceso", "Completadas")

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val todasSolicitudes = mutableListOf<Solicitud>()

            val reservasTaller = db.collection("reservas")
                .orderBy("fechaReserva", Query.Direction.DESCENDING)
                .get()
                .await()

            reservasTaller.documents.forEach { doc ->
                try {
                    todasSolicitudes.add(
                        Solicitud(
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
                            userId = doc.getString("userId") ?: "",
                            coleccion = "reservas"
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ADMIN_ERROR", "Error: ${e.message}")
                }
            }

            val solicitudesDomicilio = db.collection("solicitudes_domicilio")
                .orderBy("fechaSolicitud", Query.Direction.DESCENDING)
                .get()
                .await()

            solicitudesDomicilio.documents.forEach { doc ->
                try {
                    todasSolicitudes.add(
                        Solicitud(
                            id = doc.getString("solicitudId") ?: doc.id,
                            clienteNombre = doc.getString("userName") ?: "Sin nombre",
                            clienteTelefono = doc.getString("telefonoContacto") ?: doc.getString("userPhone") ?: "Sin teléfono",
                            direccion = doc.getString("direccion") ?: "Sin dirección",
                            tipoServicio = "Domicilio",
                            estado = doc.getString("estado") ?: "PENDIENTE",
                            fecha = doc.getTimestamp("fechaSolicitud")?.toDate() ?: Date(),
                            precio = 0,
                            notas = doc.getString("notas") ?: "",
                            userEmail = doc.getString("userEmail") ?: "",
                            userId = doc.getString("userId") ?: "",
                            coleccion = "solicitudes_domicilio"
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ADMIN_ERROR", "Error: ${e.message}")
                }
            }

            solicitudes = todasSolicitudes.sortedByDescending { it.fecha }

        } catch (e: Exception) {
            android.util.Log.e("ADMIN_ERROR", "Error: ${e.message}")
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

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HavolineYellow)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard("Pendientes", solicitudes.count { it.estado == "PENDIENTE" }.toString(), Color(0xFFFF9800))
                StatCard("En Proceso", solicitudes.count { it.estado == "EN_PROCESO" }.toString(), Color(0xFF2196F3))
                StatCard("Completadas", solicitudes.count { it.estado == "COMPLETADO" }.toString(), Color(0xFF4CAF50))
            }

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
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

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
                                    db.collection(solicitud.coleccion)
                                        .document(solicitud.id)
                                        .update("estado", nuevoEstado)
                                        .await()
                                    refreshTrigger++
                                } catch (e: Exception) {
                                    android.util.Log.e("ADMIN_ERROR", "Error: ${e.message}")
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (filteredSolicitudes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay solicitudes en esta categoría", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB DE USUARIOS ====================
@Composable
fun UsuariosTab(db: FirebaseFirestore, scope: kotlinx.coroutines.CoroutineScope) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val usuariosSnapshot = db.collection("users").get().await()
            val listaUsuarios = mutableListOf<Usuario>()

            usuariosSnapshot.documents.forEach { doc ->
                try {
                    val userId = doc.id

                    // Contar servicios del usuario
                    val reservas = db.collection("reservas")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                        .size()

                    val solicitudes = db.collection("solicitudes_domicilio")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                        .size()

                    listaUsuarios.add(
                        Usuario(
                            id = userId,
                            username = doc.getString("username") ?: "Sin nombre",
                            email = doc.getString("email") ?: "Sin email",
                            phone = doc.getString("phone") ?: "Sin teléfono",
                            isAdmin = doc.getBoolean("isAdmin") ?: false,
                            isBanned = doc.getBoolean("isBanned") ?: false,
                            createdAt = doc.getTimestamp("createdAt")?.toDate(),
                            totalServicios = reservas + solicitudes
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ADMIN_ERROR", "Error al cargar usuario: ${e.message}")
                }
            }

            usuarios = listaUsuarios.sortedByDescending { it.createdAt }

        } catch (e: Exception) {
            android.util.Log.e("ADMIN_ERROR", "Error al cargar usuarios: ${e.message}")
        }
        isLoading = false
    }

    val filteredUsuarios = remember(searchQuery, usuarios) {
        if (searchQuery.isBlank()) {
            usuarios
        } else {
            usuarios.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HavolineYellow)
            }
        } else {
            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard("Total", usuarios.size.toString(), ChevronBlue)
                StatCard("Activos", usuarios.count { !it.isBanned }.toString(), Color(0xFF4CAF50))
                StatCard("Baneados", usuarios.count { it.isBanned }.toString(), Color(0xFFB00020))
            }

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar usuario...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HavolineYellow,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Lista de usuarios
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsuarios) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        onBanToggle = {
                            scope.launch {
                                try {
                                    db.collection("users")
                                        .document(usuario.id)
                                        .update("isBanned", !usuario.isBanned)
                                        .await()
                                    refreshTrigger++
                                } catch (e: Exception) {
                                    android.util.Log.e("ADMIN_ERROR", "Error: ${e.message}")
                                }
                            }
                        }
                    )
                }

                if (filteredUsuarios.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron usuarios", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB DE LUBRICENTROS ====================
@Composable
fun LubricentrosTab() {
    val lubricentros = remember {
        listOf(
            LubricentroData(1, "Lubricentro Havoline Centro", "Calle 35 #10-45, Bucaramanga", "6076345678", "Lun-Sáb: 8:00AM - 6:00PM", true, 4.8f, 85000),
            LubricentroData(2, "Cambio Express Cabecera", "Carrera 36 #48-20, Bucaramanga", "6076456789", "Lun-Vie: 7:30AM - 7:00PM", true, 4.7f, 78000),
            LubricentroData(3, "Taller DLO Provenza", "Calle 56 #23-15, Bucaramanga", "6076567890", "Lun-Sáb: 8:00AM - 5:30PM", true, 4.9f, 92000),
            LubricentroData(4, "Aceites Total Florida", "Carrera 27 #29-102, Floridablanca", "6076678901", "Lun-Sáb: 8:00AM - 6:00PM", false, 4.6f, 75000),
            LubricentroData(5, "Lubricantes García Rovira", "Calle 45 #19-30, Bucaramanga", "6076789012", "Lun-Vie: 8:00AM - 6:00PM", true, 4.5f, 88000),
            LubricentroData(6, "Express Oil Cañaveral", "Calle 51 #28-45, Floridablanca", "6076890123", "Lun-Sáb: 7:00AM - 6:00PM", false, 4.4f, 80000)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Estadísticas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard("Total", lubricentros.size.toString(), ChevronBlue)
            StatCard("Abiertos", lubricentros.count { it.disponible }.toString(), Color(0xFF4CAF50))
            StatCard("Cerrados", lubricentros.count { !it.disponible }.toString(), Color(0xFFB00020))
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lubricentros) { lubricentro ->
                LubricentroCard(lubricentro)
            }
        }
    }
}

// ==================== CARDS COMPONENTS ====================
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
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SolicitudCard(solicitud: Solicitud, onEstadoChange: (String) -> Unit) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Solicitud #${solicitud.id.take(8)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(dateFormat.format(solicitud.fecha), fontSize = 12.sp, color = Color.Gray)
                }

                Surface(
                    color = when (solicitud.estado) {
                        "PENDIENTE" -> HavolineYellow
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
                        color = if (solicitud.estado == "PENDIENTE") ChevronBlue else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

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
                InfoRow(Icons.Default.AttachMoney, "$${solicitud.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}")
            }

            if (solicitud.notas.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                InfoRow(Icons.Default.Info, solicitud.notas)
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showMenu = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CAMBIAR ESTADO", color = ChevronBlue, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null, tint = ChevronBlue)
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    estados.forEach { estado ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(12.dp).background(
                                            when (estado) {
                                                "PENDIENTE" -> Color(0xFFFF9800)
                                                "EN_PROCESO" -> Color(0xFF2196F3)
                                                "COMPLETADO" -> Color(0xFF4CAF50)
                                                else -> Color(0xFFB00020)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        when (estado) {
                                            "PENDIENTE" -> "Pendiente"
                                            "EN_PROCESO" -> "En Proceso"
                                            "COMPLETADO" -> "Completado"
                                            else -> "Cancelado"
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
fun UsuarioCard(usuario: Usuario, onBanToggle: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.isBanned) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (usuario.isBanned) Color.LightGray else HavolineYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (usuario.isBanned) Color.Gray else ChevronBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(usuario.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (usuario.isAdmin) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFB00020),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "ADMIN",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (usuario.isBanned) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFB00020),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "BANEADO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(usuario.email, fontSize = 13.sp, color = Color.Gray)
                }

                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Teléfono", fontSize = 11.sp, color = Color.Gray)
                    Text(usuario.phone, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Servicios", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        usuario.totalServicios.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChevronBlue
                    )
                }
            }

            if (usuario.createdAt != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Miembro desde ${dateFormat.format(usuario.createdAt)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    if (usuario.isBanned) Icons.Default.Check else Icons.Default.Block,
                    contentDescription = null,
                    tint = if (usuario.isBanned) Color(0xFF4CAF50) else Color(0xFFB00020),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (usuario.isBanned) "¿Desbanear usuario?" else "¿Banear usuario?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    if (usuario.isBanned)
                        "${usuario.username} podrá volver a realizar pedidos y usar la aplicación."
                    else
                        "${usuario.username} no podrá realizar más pedidos ni usar servicios de la aplicación.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBanToggle()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (usuario.isBanned) Color(0xFF4CAF50) else Color(0xFFB00020)
                    )
                ) {
                    Text(if (usuario.isBanned) "DESBANEAR" else "BANEAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun LubricentroCard(lubricentro: LubricentroData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (lubricentro.disponible) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            lubricentro.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (lubricentro.disponible) TextPrimary else Color.Gray
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = HavolineYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            lubricentro.calificacion.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Estado badge
                Surface(
                    color = if (lubricentro.disponible) Color(0xFF4CAF50) else Color(0xFFB00020),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (lubricentro.disponible) "ABIERTO" else "CERRADO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            InfoRow(Icons.Default.LocationOn, lubricentro.direccion)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.Phone, lubricentro.telefono)
            Spacer(Modifier.height(6.dp))
            InfoRow(Icons.Default.Schedule, lubricentro.horario)
            Spacer(Modifier.height(6.dp))

            // Precio destacado
            Surface(
                color = HavolineYellow.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = ChevronBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Precio desde", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(
                        "${lubricentro.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChevronBlue
                    )
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