package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import TextPrimary
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Lubricentro(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val distancia: Double,
    val precio: Int,
    val calificacion: Float,
    val latitud: Double,
    val longitud: Double,
    val telefono: String,
    val horario: String,
    val servicios: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Lubricentros en Bucaramanga y área metropolitana
    val lubricentros = remember {
        listOf(
            Lubricentro(
                1, "Lubricentro Havoline Centro",
                "Calle 35 #10-45, Bucaramanga",
                1.2, 85000, 4.8f,
                7.1193, -73.1227,
                "6076345678",
                "Lun-Sáb: 8:00AM - 6:00PM",
                listOf("Cambio de aceite", "Filtros", "Lavado de motor")
            ),
            Lubricentro(
                2, "Cambio Express Cabecera",
                "Carrera 36 #48-20, Bucaramanga",
                2.3, 78000, 4.7f,
                7.1305, -73.1217,
                "6076456789",
                "Lun-Vie: 7:30AM - 7:00PM",
                listOf("Cambio de aceite", "Filtros", "Diagnóstico")
            ),
            Lubricentro(
                3, "Taller DLO Provenza",
                "Calle 56 #23-15, Bucaramanga",
                3.5, 92000, 4.9f,
                7.1425, -73.1189,
                "6076567890",
                "Lun-Sáb: 8:00AM - 5:30PM",
                listOf("Cambio de aceite", "Mantenimiento completo", "Lavado")
            ),
            Lubricentro(
                4, "Aceites Total Florida",
                "Carrera 27 #29-102, Floridablanca",
                4.1, 75000, 4.6f,
                7.0622, -73.0875,
                "6076678901",
                "Lun-Sáb: 8:00AM - 6:00PM",
                listOf("Cambio de aceite", "Filtros", "Revisión general")
            ),
            Lubricentro(
                5, "Lubricantes García Rovira",
                "Calle 45 #19-30, Bucaramanga",
                1.8, 88000, 4.5f,
                7.1254, -73.1195,
                "6076789012",
                "Lun-Vie: 8:00AM - 6:00PM, Sáb: 8:00AM - 2:00PM",
                listOf("Cambio de aceite", "Filtros", "Diagnóstico motor")
            ),
            Lubricentro(
                6, "Express Oil Cañaveral",
                "Calle 51 #28-45, Floridablanca",
                5.2, 80000, 4.4f,
                7.0891, -73.0923,
                "6076890123",
                "Lun-Sáb: 7:00AM - 6:00PM",
                listOf("Cambio de aceite", "Filtros", "Lavado de motor")
            )
        ).sortedBy { it.distancia }
    }

    var selectedLubricentro by remember { mutableStateOf<Lubricentro?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lubricentros de Confianza", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            // Banner informativo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = HavolineYellow),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ChevronBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Encuentra tu lubricentro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ChevronBlue
                        )
                        Text(
                            "${lubricentros.size} talleres disponibles",
                            fontSize = 13.sp,
                            color = ChevronBlue
                        )
                    }
                }
            }

            // Lista de lubricentros
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(lubricentros) { lubricentro ->
                    LubricentroCard(
                        lubricentro = lubricentro,
                        onVerDetalles = { selectedLubricentro = lubricentro },
                        onLlamar = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${lubricentro.telefono}")
                            }
                            context.startActivity(intent)
                        },
                        onReservar = {
                            selectedLubricentro = lubricentro
                            showConfirmDialog = true
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Diálogo de detalles
        if (selectedLubricentro != null && !showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { selectedLubricentro = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint = HavolineYellow,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                selectedLubricentro!!.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = HavolineYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    selectedLubricentro!!.calificacion.toString(),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                },
                text = {
                    Column {
                        DetailRow(Icons.Default.LocationOn, selectedLubricentro!!.direccion)
                        Spacer(Modifier.height(8.dp))
                        DetailRow(Icons.Default.Phone, selectedLubricentro!!.telefono)
                        Spacer(Modifier.height(8.dp))
                        DetailRow(Icons.Default.Schedule, selectedLubricentro!!.horario)
                        Spacer(Modifier.height(8.dp))
                        DetailRow(
                            Icons.Default.AttachMoney,
                            "$${selectedLubricentro!!.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}"
                        )
                        Spacer(Modifier.height(8.dp))
                        DetailRow(
                            Icons.Default.Navigation,
                            "${String.format("%.1f", selectedLubricentro!!.distancia)} km de distancia"
                        )

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Servicios disponibles:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ChevronBlue
                        )
                        Spacer(Modifier.height(4.dp))
                        selectedLubricentro!!.servicios.forEach { servicio ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(servicio, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Row {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("geo:${selectedLubricentro!!.latitud},${selectedLubricentro!!.longitud}?q=${selectedLubricentro!!.latitud},${selectedLubricentro!!.longitud}(${selectedLubricentro!!.nombre})")
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("MAPA", fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow)
                        ) {
                            Text("RESERVAR", color = ChevronBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedLubricentro = null }) {
                        Text("CERRAR", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }

        // Diálogo de confirmación
        if (showConfirmDialog && selectedLubricentro != null) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    selectedLubricentro = null
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
                        "¡Reserva Confirmada!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Tu cita en ${selectedLubricentro!!.nombre} ha sido programada.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Recibirás un SMS de confirmación en breve.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            selectedLubricentro = null
                            onBackClick()
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
fun LubricentroCard(
    lubricentro: Lubricentro,
    onVerDetalles: () -> Unit,
    onLlamar: () -> Unit,
    onReservar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lubricentro.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
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
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "• ${String.format("%.1f", lubricentro.distancia)} km",
                            fontSize = 13.sp,
                            color = ChevronBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Precio destacado
                Surface(
                    color = HavolineYellow,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Desde",
                            fontSize = 10.sp,
                            color = ChevronBlue
                        )
                        Text(
                            "$${lubricentro.precio.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")}",
                            fontWeight = FontWeight.Bold,
                            color = ChevronBlue,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    lubricentro.direccion,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    lubricentro.horario.split(",")[0],
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Botones de acción mejorados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Info
                OutlinedButton(
                    onClick = onVerDetalles,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Info",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón Llamar
                OutlinedButton(
                    onClick = onLlamar,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Llamar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón Reservar
                Button(
                    onClick = onReservar,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Reservar",
                        color = ChevronBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = HavolineYellow,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = TextPrimary)
    }
}