package com.projectpurr.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorOrbAmber

/**
 * Matte tactile play orb — softly alive, not a light source.
 *
 * @param embedded When true, glow stays inside the orb bounds (panel-integrated).
 */
@Composable
fun BreathingPlayOrb(
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    enabled: Boolean = true,
    embedded: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "orbBreath")
    val breathScale by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (playing) 1.02f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (playing) 4000 else 5400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )
    val warmthAlpha by transition.animateFloat(
        initialValue = 0.03f,
        targetValue = if (playing) 0.07f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (playing) 4400 else 5800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "warmthAlpha",
    )

    val outerSize = if (embedded) size else size + 16.dp

    Box(
        modifier = modifier
            .size(outerSize)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .drawBehind {
                    if (!embedded) {
                        drawCircle(
                            color = ColorOrbAmber.copy(alpha = warmthAlpha * breathScale),
                            radius = this.size.minDimension * 0.52f,
                        )
                    }
                    drawCircle(
                        color = ColorOrbAmber.copy(alpha = warmthAlpha * 0.6f * breathScale),
                        radius = this.size.minDimension * 0.46f,
                    )
                    // Solid amber fill — matches Begin Session button
                    drawCircle(
                        color  = Color(0xFFBB7C34),
                        radius = this.size.minDimension * 0.5f,
                    )
                    // Subtle dark edge ring for definition
                    drawCircle(
                        color  = Color(0xFF6B3E18).copy(alpha = 0.55f),
                        radius = this.size.minDimension * 0.48f,
                        style  = Stroke(width = 1.5f),
                    )
                }
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Filled.Pets,
                contentDescription = if (playing) "Pause" else "Play",
                tint               = Color(0xFF1A0E05),
                modifier           = Modifier.size(size * 0.38f),
            )
        }
    }
}
