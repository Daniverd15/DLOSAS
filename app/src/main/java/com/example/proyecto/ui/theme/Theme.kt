package com.example.proyecto.ui.theme



import CardWhite
import ChevronBlue
import Cream
import HavolineYellow
import TextPrimary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colorScheme = lightColorScheme(
    primary = ChevronBlue,
    secondary = HavolineYellow,
    background = Cream,
    surface = CardWhite,
    onPrimary = Color.White,
    onSecondary = Color(0xFF202020),
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}