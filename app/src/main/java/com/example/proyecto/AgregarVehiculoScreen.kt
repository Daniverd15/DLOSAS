package com.example.proyecto

import Cream
import HavolineYellow
import ChevronBlue
import TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarVehiculoScreen(
    onBackClick: () -> Unit,
    onVehiculoGuardado: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var tipoVehiculo by remember { mutableStateOf("auto") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var año by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para guardar vehículo en Firestore
    suspend fun guardarVehiculo(): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val vehiculoId = db.collection("vehiculos").document().id

            val vehiculo = hashMapOf(
                "id" to vehiculoId,
                "userId" to currentUser.uid,
                "marca" to marca,
                "modelo" to modelo,
                "año" to (año.toIntOrNull() ?: 0),
                "placa" to placa.uppercase(),
                "kilometraje" to (kilometraje.toIntOrNull() ?: 0),
                "color" to color,
                "tipo" to tipoVehiculo,
                "tipoAceite" to "",
                "fechaUltimoCambio" to "",
                "imagenUrl" to "",
                "fechaCreacion" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            db.collection("vehiculos")
                .document(vehiculoId)
                .set(vehiculo)
                .await()

            Result.success(vehiculoId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Agregar Vehículo", fontWeight = FontWeight.Bold)
                },
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa los datos de tu vehículo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChevronBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Selector de tipo (Auto o Moto)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TipoVehiculoButton(
                        icono = Icons.Default.DirectionsCar,
                        label = "Auto",
                        selected = tipoVehiculo == "auto",
                        onClick = { tipoVehiculo = "auto" },
                        modifier = Modifier.weight(1f)
                    )

                    TipoVehiculoButton(
                        icono = Icons.Default.TwoWheeler,
                        label = "Moto",
                        selected = tipoVehiculo == "moto",
                        onClick = { tipoVehiculo = "moto" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Botón agregar foto (placeholder)
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .clickable { /* TODO: Agregar foto */ },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Agregar foto",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Formulario
                VehicleTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = "Marca *",
                    placeholder = "Ej: Chevrolet, Toyota, Mazda"
                )

                Spacer(Modifier.height(16.dp))

                VehicleTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = "Modelo *",
                    placeholder = "Ej: Sail, Corolla, 3"
                )

                Spacer(Modifier.height(16.dp))

                VehicleTextField(
                    value = año,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 4) año = it },
                    label = "Año",
                    placeholder = "Ej: 2020",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(16.dp))

                VehicleTextField(
                    value = kilometraje,
                    onValueChange = { if (it.all { c -> c.isDigit() }) kilometraje = it },
                    label = "Kilometraje",
                    placeholder = "Ej: 50000",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(16.dp))

                VehicleTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase() },
                    label = "Placa *",
                    placeholder = "Ej: ABC123"
                )

                Spacer(Modifier.height(16.dp))

                VehicleTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = "Color",
                    placeholder = "Ej: Blanco, Negro, Rojo"
                )

                Spacer(Modifier.height(32.dp))

                // Botón guardar
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val result = guardarVehiculo()
                            isLoading = false

                            if (result.isSuccess) {
                                showSuccessDialog = true
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    shape = RoundedCornerShape(12.dp),
                    enabled = marca.isNotBlank() && modelo.isNotBlank() && placa.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = ChevronBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = ChevronBlue
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "GUARDAR",
                            color = ChevronBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Diálogo de éxito
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                    },
                    title = {
                        Text(
                            "¡Vehículo Guardado!",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            "Tu ${if (tipoVehiculo == "auto") "auto" else "moto"} $marca $modelo ha sido registrado exitosamente.",
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onVehiculoGuardado()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow)
                        ) {
                            Text("ENTENDIDO", color = ChevronBlue, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color.White
                )
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
fun TipoVehiculoButton(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) HavolineYellow else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(3.dp, ChevronBlue)
        } else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icono,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (selected) ChevronBlue else Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) ChevronBlue else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HavolineYellow,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            focusedLabelColor = ChevronBlue,
            cursorColor = ChevronBlue
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}