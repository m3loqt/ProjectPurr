package com.projectpurr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectpurr.R
import com.projectpurr.ui.components.AtmosphericScene
import com.projectpurr.ui.components.BreathingPlayOrb
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorTextSecondary
import com.projectpurr.ui.theme.ColorTextTertiary

@Composable
fun HomeScreen(
    sessionCount: Int,
    onSelectHouseCat: () -> Unit,
    onPreviewHaptic: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val entryAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "homeFadeIn",
    )

    AtmosphericScene(
        heroImageRes = R.drawable.herobg,
        heroAlpha = 0.42f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .alpha(entryAlpha),
        ) {
            // Quiet header — logo only, settings recedes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.project_purr),
                    contentDescription = "Project Purr",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentWidth()
                        .alpha(0.88f),
                )
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.alpha(0.38f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = ColorMoonlitCream,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Atmospheric breathing room (~70% feel)
            Spacer(Modifier.weight(0.72f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Settle in.",
                    style = MaterialTheme.typography.displayMedium,
                    color = ColorMoonlitCream.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "House Cat",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTextTertiary.copy(alpha = 0.85f),
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "A warm purr, resting beside you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextSecondary.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )

                if (sessionCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (sessionCount == 1) "1 quiet night" else "$sessionCount quiet nights",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorTextTertiary.copy(alpha = 0.55f),
                    )
                }
            }

            Spacer(Modifier.weight(0.28f))

            // Emotional entry — orb invites touch, not a settings card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BreathingPlayOrb(
                        playing = false,
                        onClick = onSelectHouseCat,
                        size = 80.dp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Begin when ready",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorMoonlitCream.copy(alpha = 0.38f),
                        modifier = Modifier.clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = onSelectHouseCat,
                        ),
                    )
                }
            }

            // Ghost companions — barely present
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GhostCompanionLabel("Ragdoll", onLongPress = onPreviewHaptic)
                GhostCompanionLabel("Maine Coon", onLongPress = onPreviewHaptic)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GhostCompanionLabel(
    name: String,
    onLongPress: () -> Unit,
) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelSmall,
        color = ColorTextTertiary.copy(alpha = 0.32f),
        modifier = Modifier
            .alpha(0.7f)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            },
    )
}
