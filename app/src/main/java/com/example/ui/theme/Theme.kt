package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QcomDarkColorScheme = darkColorScheme(
    primary = QcomCyan,
    onPrimary = Color(0xFF00324D),
    primaryContainer = Color(0xFF004B71),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = QcomOrange,
    onSecondary = Color(0xFF4C2100),
    secondaryContainer = Color(0xFF6D3100),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = QcomAmber,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF574300),
    onTertiaryContainer = Color(0xFFFFEFA7),
    background = SlateBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = QcomDarkColorScheme,
        typography = Typography,
        content = content
    )
}

