package com.projectpurr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.projectpurr.ui.theme.ColorBackground

/**
 * Full-screen cinematic atmosphere: optional hero imagery, moonlight, vignette.
 * Content floats above as interaction layers — not inside cards.
 */
@Composable
fun AtmosphericScene(
    modifier: Modifier = Modifier,
    @DrawableRes heroImageRes: Int? = null,
    heroAlpha: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF12100E),
                            ColorBackground,
                            Color(0xFF080706),
                        ),
                    ),
                ),
        )

        if (heroImageRes != null) {
            Image(
                painter = painterResource(heroImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                alpha = heroAlpha,
            )
        }

        // Moonlight wash — upper right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x14F5E8D4),
                            Color.Transparent,
                        ),
                        center = Offset(900f, 120f),
                        radius = 700f,
                    ),
                ),
        )

        // Warm floor glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x10C87830),
                            Color.Transparent,
                        ),
                        center = Offset(280f, 1400f),
                        radius = 900f,
                    ),
                ),
        )

        // Cinematic scrim — deepens edges, lifts center text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0x88080806),
                            0.22f to Color(0x33080806),
                            0.55f to Color(0x1A080806),
                            0.78f to Color(0xCC0A0908),
                            1.00f to Color(0xF0080706),
                        ),
                    ),
                ),
        )

        // Deep shadow plane — extra depth before vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x66060504),
                            0.45f to Color.Transparent,
                            1.0f to Color(0xE0050403),
                        ),
                    ),
                ),
        )

        // Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xAA050403),
                        ),
                        radius = 1100f,
                    ),
                ),
        )

        content()
    }
}
