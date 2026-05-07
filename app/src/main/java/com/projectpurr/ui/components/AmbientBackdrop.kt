package com.projectpurr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.projectpurr.ui.theme.ColorBackground

@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val base = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF17120E),
            ColorBackground,
            Color(0xFF0B0908),
        ),
    )
    val moonlight = Brush.radialGradient(
        colors = listOf(
            Color(0x18FFE9CA),
            Color.Transparent,
        ),
        center = Offset(0.85f, 0.12f),
        radius = 900f,
    )
    val warmth = Brush.radialGradient(
        colors = listOf(
            Color(0x12E09040),
            Color.Transparent,
        ),
        center = Offset(0.2f, 1.0f),
        radius = 1200f,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .background(moonlight)
            .background(warmth),
        content = content,
    )
}
