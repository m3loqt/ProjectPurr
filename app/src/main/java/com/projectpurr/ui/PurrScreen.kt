package com.projectpurr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SessionPhase
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.components.AmbientBackdrop
import com.projectpurr.ui.components.GlassCard
import com.projectpurr.ui.theme.ColorOnBackground
import com.projectpurr.ui.theme.ColorOnPrimary
import com.projectpurr.ui.theme.ColorOnSurface
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorPrimaryGlow
import com.projectpurr.ui.theme.ColorTextSecondary

@Composable
fun SessionScreen(
    state: PurrUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSilentChange: (Boolean) -> Unit,
    onChestModeChange: (Boolean) -> Unit,
    onSleepTimerChange: (SleepTimerOption) -> Unit,
) {
    val playing = state.phase == SessionPhase.PLAYING || state.phase == SessionPhase.FADING

    // Dim deeper and slower so the session UI feels like it falls into the background.
    val chestDim by animateFloatAsState(
        targetValue = if (state.chestMode && playing) 0.34f else 1f,
        animationSpec = tween(durationMillis = 950),
        label = "sessionDim",
    )
    val glowTransition = rememberInfiniteTransition(label = "sessionGlow")
    val glowScale by glowTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (playing) 1.06f else 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (playing) 3600 else 4600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowScale",
    )
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = if (playing) 0.32f else 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (playing) 4200 else 5200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    // Intercept system back so we stop the session before popping the back stack.
    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AmbientBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .alpha(chestDim),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ColorTextSecondary,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "House Cat",
                        style = MaterialTheme.typography.titleLarge,
                        color = ColorOnBackground.copy(alpha = 0.92f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Session phase label
                Text(
                    text = when (state.phase) {
                        SessionPhase.STOPPED -> "Settle the phone on your chest, then press play."
                        SessionPhase.PLAYING -> "Resting with you."
                        SessionPhase.FADING  -> "Softly drifting to silence..."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )

                // Play button with slow breathing glow
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(154.dp * glowScale)
                            .drawBehind {
                                drawCircle(
                                    color = ColorPrimaryGlow.copy(alpha = glowAlpha),
                                    radius = size.minDimension * 0.72f,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingActionButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(112.dp),
                            shape = CircleShape,
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                        ) {
                            Icon(
                                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                modifier = Modifier.size(52.dp),
                            )
                        }
                    }
                }

                // Glass controls card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    innerPadding = 20.dp,
                ) {
                    ControlToggleRow(
                        label = "Silent purr",
                        checked = state.silentPurr,
                        onCheckedChange = onSilentChange,
                    )
                    Spacer(Modifier.height(16.dp))
                    ControlToggleRow(
                        label = "Chest mode",
                        checked = state.chestMode,
                        onCheckedChange = onChestModeChange,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Sleep timer",
                        style = MaterialTheme.typography.labelLarge,
                        color = ColorOnSurface.copy(alpha = 0.92f),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SleepTimerOption.entries.forEach { opt ->
                            FilterChip(
                                selected = state.sleepTimer == opt,
                                onClick = { onSleepTimerChange(opt) },
                                label = {
                                    Text(opt.label, style = MaterialTheme.typography.labelMedium)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = ColorOnSurface)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
