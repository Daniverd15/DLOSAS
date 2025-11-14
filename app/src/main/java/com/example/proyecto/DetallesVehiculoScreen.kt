package com.example.proyecto

import Cream
import HavolineYellow
import ChevronBlue
import TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleVehiculoScreen(
    vehiculo: Vehiculo,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle del Vehículo", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Imagen del vehículo (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (vehiculo.tipo == "auto") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = ChevronBlue.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Título
            Text(
                "${vehiculo.marca} ${vehiculo.modelo}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ChevronBlue
            )

            Spacer(Modifier.height(24.dp))

            // Información del vehículo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    DetalleRow(label = "Marca", valor = vehiculo.marca)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DetalleRow(label = "Modelo", valor = vehiculo.modelo)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DetalleRow(
                        label = "Placa",
                        valor = vehiculo.placa,
                        destacado = true
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DetalleRow(
                        label = "Kilometraje",
                        valor = "${vehiculo.kilometraje.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1.")} km"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DetalleRow(label = "Color", valor = vehiculo.color)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Información de mantenimiento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = HavolineYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Historial de Mantenimiento",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChevronBlue
                        )
                    }

                    if (vehiculo.tipoAceite.isNotEmpty()) {
                        DetalleRow(
                            label = "Tipo de Aceite",
                            valor = vehiculo.tipoAceite
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    if (vehiculo.fechaUltimoCambio.isNotEmpty()) {
                        DetalleRow(
                            label = "Fecha del Último Cambio",
                            valor = vehiculo.fechaUltimoCambio
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón volver
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Volver a Mis Vehículos",
                    color = ChevronBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DetalleRow(
    label: String,
    valor: String,
    destacado: Boolean = false
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            valor,
            fontSize = if (destacado) 20.sp else 18.sp,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
            color = if (destacado) ChevronBlue else TextPrimary
        )
    }
}