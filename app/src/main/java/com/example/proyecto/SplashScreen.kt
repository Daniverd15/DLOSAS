package com.example.proyecto

import ChevronBlue
import Cream
import HavolineYellow
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(3000) // 3 segundos de splash
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo animado
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(180.dp)
                    .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                runCatching {
                    Image(
                        painter = painterResource(id = R.drawable.logo_chevron),
                        contentDescription = "Chevron Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }.onFailure {
                    Text(
                        "DLO",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChevronBlue
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "DLO SAS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ChevronBlue
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Cambio de aceite profesional",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(Modifier.height(40.dp))

            CircularProgressIndicator(
                color = HavolineYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.height(80.dp))

            // Powered by
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.6f)
            ) {
                Text(
                    text = "Powered by",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    runCatching {
                        Image(
                            painter = painterResource(id = R.drawable.logo_losas),
                            contentDescription = "LOSAS",
                            modifier = Modifier.height(18.dp)
                        )
                    }.onFailure {
                        Text("LOSAS", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}