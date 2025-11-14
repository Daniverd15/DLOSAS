package com.example.proyecto

import Cream
import HavolineYellow
import ChevronBlue
import TextPrimary
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDomicilio: () -> Unit,
    onNavigateToTaller: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToVehiculos: () -> Unit = {},  // ✅ NUEVO PARÁMETRO
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DLO SAS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChevronBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToHistorial) {
                        Icon(Icons.Default.History, contentDescription = "Historial", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_chevron),
                contentDescription = "Havoline",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "¡Cambia tu aceite de manera rápida y eficiente con Havoline!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Características
            FeatureCard(
                icon = Icons.Default.CheckCircle,
                title = "FÁCIL DE USAR",
                description = "Interfaz intuitiva y simple"
            )

            Spacer(Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.People,
                title = "FAMILIAR",
                description = "Para toda tu familia"
            )

            Spacer(Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Star,
                title = "ATENCIÓN ESPECIALIZADA",
                description = "Servicio profesional garantizado"
            )

            Spacer(Modifier.height(32.dp))

            // Botones principales
            Button(
                onClick = onNavigateToDomicilio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HavolineYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = ChevronBlue)
                Spacer(Modifier.width(8.dp))
                Text("SERVICIO A DOMICILIO", color = ChevronBlue, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToTaller,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("IR AL TALLER", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // ✅ NUEVO BOTÓN: MIS VEHÍCULOS
            OutlinedButton(
                onClick = onNavigateToVehiculos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChevronBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("MIS VEHÍCULOS", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = HavolineYellow,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ChevronBlue)
                Text(description, fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}