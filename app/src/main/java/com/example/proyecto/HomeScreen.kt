package com.example.proyecto

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// NOTA: Estos colores (Cream, HavolineYellow, ChevronBlue, TextPrimary)
// son definidos en el archivo Theme.kt o Colors.kt pues son colores de chevron.
import Cream
import HavolineYellow
import ChevronBlue
import TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDomicilio: () -> Unit,
    onNavigateToTaller: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToVehiculos: () -> Unit = {},
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
        // 1. Inicializar el estado de desplazamiento (Scroll State)
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(horizontal = 24.dp) // Solo padding horizontal aquí
                // 2. Aplicar el modificador de desplazamiento vertical
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            // 3. Eliminado: verticalArrangement = Arrangement.Center
        ) {
            // Un espaciador al principio para el padding superior, ya que no usamos Arrangement.Center
            Spacer(modifier = Modifier.height(24.dp))

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

            // MIS VEHÍCULOS
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

            // Espacio final para que el último botón no quede pegado al borde inferior
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureCard(icon: ImageVector, title: String, description: String) {
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