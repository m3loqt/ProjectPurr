package com.projectpurr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PurrDark = darkColorScheme(
    primary = Color(0xFFD4A574),
    onPrimary = Color(0xFF1B1612),
    primaryContainer = Color(0xFF3D2E22),
    onPrimaryContainer = Color(0xFFF2E6D8),
    secondary = Color(0xFFC9B8A4),
    onSecondary = Color(0xFF1B1612),
    background = Color(0xFF1B1612),
    onBackground = Color(0xFFF2E6D8),
    surface = Color(0xFF262019),
    onSurface = Color(0xFFF2E6D8),
    surfaceVariant = Color(0xFF332A22),
    onSurfaceVariant = Color(0xFFD7C8B8),
    outline = Color(0x66D4A574),
)

@Composable
fun ProjectPurrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PurrDark,
        typography = PurrTypography,
        content = content,
    )
}
