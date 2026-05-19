package com.projectpurr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.projectpurr.R
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SessionPhase
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.components.AtmosphericScene
import com.projectpurr.ui.components.CinematicTextureOverlay
import com.projectpurr.ui.components.SessionInteractionPanel
import com.projectpurr.ui.theme.ColorMoonlitCream

@Composable
fun SessionScreen(
    state: PurrUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSilentChange: (Boolean) -> Unit,
    onChestModeChange: (Boolean) -> Unit,
    onForceSpeakerChange: (Boolean) -> Unit,
    onSleepTimerChange: (SleepTimerOption) -> Unit,
) {
    val playing = state.phase == SessionPhase.PLAYING || state.phase == SessionPhase.FADING
    var showBackConfirm by remember { mutableStateOf(false) }

    val chestDim by animateFloatAsState(
        targetValue = when {
            state.chestMode && playing -> 0.18f
            playing -> 0.48f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 1200),
        label = "sessionUiDim",
    )
    val panelAlpha by animateFloatAsState(
        targetValue = when {
            state.chestMode && playing -> 0.38f
            playing -> 0.68f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 1000),
        label = "panelFade",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (playing) 0.52f else 0.94f,
        animationSpec = tween(durationMillis = 900),
        label = "textFade",
    )

    val timerLabel = state.timerRemainingMs
        ?.takeIf { it > 0 }
        ?.let { ms -> ms.toTimerLabel() }

    BackHandler {
        if (state.isSessionActive) showBackConfirm = true else onBack()
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text("Leave the purr?") },
            text = { Text("The warmth will fade away.") },
            confirmButton = {
                TextButton(onClick = { showBackConfirm = false; onBack() }) {
                    Text("End session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) { Text("Stay") }
            },
        )
    }

    AtmosphericScene(
        heroImageRes = R.drawable.onboarding_hero_3,
        heroAlpha = if (playing) 0.88f else 1f,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(chestDim),
        ) {
            CinematicTextureOverlay(modifier = Modifier.fillMaxSize())

            // Back — floats on atmosphere, outside the glass slab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .alpha(panelAlpha),
            ) {
                IconButton(
                    onClick = {
                        if (state.isSessionActive) showBackConfirm = true else onBack()
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ColorMoonlitCream.copy(alpha = 0.34f),
                    )
                }
            }

            // Glass panel — typography + controls on one slab
            SessionInteractionPanel(
                playing = playing,
                loopPositionMs = state.loopPositionMs,
                sensoryIntensity = state.sensoryIntensity,
                profileLabel = "House Cat",
                whisperPhrase = sessionWhisper(state.phase),
                timerLabel = timerLabel,
                textAlpha = textAlpha,
                silentPurr = state.silentPurr,
                forceSpeaker = state.forceSpeaker,
                chestMode = state.chestMode,
                sleepTimer = state.sleepTimer,
                panelAlpha = panelAlpha,
                onTogglePlay = onTogglePlay,
                onSleepTimerChange = onSleepTimerChange,
                onToggleSilent = { onSilentChange(!state.silentPurr) },
                onToggleSpeaker = { onForceSpeakerChange(!state.forceSpeaker) },
                onToggleChestMode = { onChestModeChange(!state.chestMode) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

private fun sessionWhisper(phase: SessionPhase): String = when (phase) {
    SessionPhase.STOPPED -> "Begin when ready"
    SessionPhase.PLAYING -> "Rest with me"
    SessionPhase.FADING  -> "Drifting away"
}

private fun Long.toTimerLabel(): String {
    val totalSec = (this / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
