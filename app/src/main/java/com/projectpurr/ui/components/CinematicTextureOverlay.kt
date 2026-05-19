package com.projectpurr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Barely-visible film grain + soft haze. Sits above atmosphere, below interaction.
 */
@Composable
fun CinematicTextureOverlay(
    modifier: Modifier = Modifier,
    grainAlpha: Float = 0.045f,
) {
    val grainSeed = remember { Random(42) }
    val specks = remember {
        List(420) {
            Offset(
                x = grainSeed.nextFloat(),
                y = grainSeed.nextFloat(),
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Soft moonlit haze — lower third lift
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.55f to Color(0x06F0E6D8),
                            0.85f to Color(0x0C1A1612),
                            1.0f to Color(0x140E0C0A),
                        ),
                    ),
                ),
        )

        // Diffused fog band behind interaction zone
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x0A908070),
                            Color.Transparent,
                        ),
                        center = Offset(540f, 1680f),
                        radius = 820f,
                    ),
                ),
        )

        // Film grain — procedural, no asset
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            specks.forEachIndexed { i, norm ->
                val flicker = 0.7f + 0.3f * ((i * 17 % 10) / 10f)
                drawCircle(
                    color = Color.White.copy(alpha = grainAlpha * flicker),
                    radius = 0.6f + (i % 3) * 0.25f,
                    center = Offset(norm.x * w, norm.y * h),
                )
            }
        }
    }
}
