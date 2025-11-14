package com.example.proyecto

import Cream
import HavolineYellow
import ChevronBlue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class para vehículos
data class Vehiculo(
    val id: String = "",
    val userId: String = "",
    val marca: String = "",
    val modelo: String = "",
    val año: Int = 0,
    val placa: String = "",
    val kilometraje: Int = 0,
    val color: String = "",
    val tipo: String = "auto", // auto o moto
    val tipoAceite: String = "",
    val fechaUltimoCambio: String = "",
    val imagenUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    onBackClick: () -> Unit,
    onNavigateToAgregarVehiculo: () -> Unit,
    onVehiculoClick: (Vehiculo) -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para cargar vehículos desde Firestore
    suspend fun cargarVehiculos() {
        try {
            val currentUser = auth.currentUser ?: return

            val snapshot = db.collection("vehiculos")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            vehiculos = snapshot.documents.mapNotNull { doc ->
                try {
                    Vehiculo(
                        id = doc.getString("id") ?: "",
                        userId = doc.getString("userId") ?: "",
                        marca = doc.getString("marca") ?: "",
                        modelo = doc.getString("modelo") ?: "",
                        año = doc.getLong("año")?.toInt() ?: 0,
                        placa = doc.getString("placa") ?: "",
                        kilometraje = doc.getLong("kilometraje")?.toInt() ?: 0,
                        color = doc.getString("color") ?: "",
                        tipo = doc.getString("tipo") ?: "auto",
                        tipoAceite = doc.getString("tipoAceite") ?: "",
                        fechaUltimoCambio = doc.getString("fechaUltimoCambio") ?: "",
                        imagenUrl = doc.getString("imagenUrl") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar vehículos: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Cargar vehículos al iniciar
    LaunchedEffect(Unit) {
        cargarVehiculos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mis Vehículos", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                cargarVehiculos()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChevronBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding)
            ) {
                // Botón agregar vehículo
                Button(
                    onClick = onNavigateToAgregarVehiculo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = ChevronBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Agregar Vehículo",
                        color = ChevronBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                when {
                    isLoading -> {
                        // Estado de carga
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ChevronBlue)
                        }
                    }

                    vehiculos.isEmpty() -> {
                        // Estado vacío
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = Color.Gray.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No tienes vehículos registrados",
                                    fontSize = 18.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Agrega tu primer vehículo para comenzar",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    else -> {
                        // Lista de vehículos
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(vehiculos) { vehiculo ->
                                VehiculoCard(
                                    vehiculo = vehiculo,
                                    onClick = { onVehiculoClick(vehiculo) }
                                )
                            }
                        }
                    }
                }
            }

            // Snackbar de error
            errorMessage?.let { message ->
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    errorMessage = null
                }

                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFFB00020),
                    contentColor = Color.White
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
fun VehiculoCard(
    vehiculo: Vehiculo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del vehículo (placeholder)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Cream),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (vehiculo.tipo == "auto") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = ChevronBlue.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Información del vehículo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "${vehiculo.marca} ${vehiculo.modelo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ChevronBlue
                )

                if (vehiculo.año > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Año: ${vehiculo.año}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Pin,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        vehiculo.placa,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (vehiculo.kilometraje > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${vehiculo.kilometraje.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")} km",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}