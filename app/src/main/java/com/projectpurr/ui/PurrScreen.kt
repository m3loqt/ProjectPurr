package com.projectpurr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectpurr.R
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SessionPhase
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.components.AtmosphericScene
import com.projectpurr.ui.components.CinematicTextureOverlay
import com.projectpurr.ui.components.SessionInteractionPanel
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorPrimary

@Composable
fun SessionScreen(
    state: PurrUiState,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
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

    val timerBadgeAlpha by animateFloatAsState(
        targetValue = when {
            timerLabel == null || !playing -> 0f
            state.chestMode -> 0.55f
            else -> 1f
        },
        animationSpec = tween(600),
        label = "timerBadgeAlpha",
    )

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

            // Panel + nav bar anchored at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SessionInteractionPanel(
                    playing          = playing,
                    loopPositionMs   = state.loopPositionMs,
                    sensoryIntensity = state.sensoryIntensity,
                    profileLabel     = "House Cat",
                    whisperPhrase    = sessionWhisper(state.phase),
                    textAlpha        = textAlpha,
                    silentPurr       = state.silentPurr,
                    forceSpeaker     = state.forceSpeaker,
                    chestMode        = state.chestMode,
                    sleepTimer       = state.sleepTimer,
                    panelAlpha       = panelAlpha,
                    onTogglePlay     = onTogglePlay,
                    onSleepTimerChange  = onSleepTimerChange,
                    onToggleSilent      = { onSilentChange(!state.silentPurr) },
                    onToggleSpeaker     = { onForceSpeakerChange(!state.forceSpeaker) },
                    onToggleChestMode   = { onChestModeChange(!state.chestMode) },
                )
                SessionNavBar(
                    panelAlpha = panelAlpha,
                    onHome     = { if (state.isSessionActive) showBackConfirm = true else onBack() },
                    onSessions = {},
                    // Profile and History: keep session playing, just navigate away
                    onFavorites = onNavigateToHistory,
                    onProfile   = onNavigateToProfile,
                )
            }

            // Floating timer countdown — drawn above the dimmed layer, always readable
            if (timerLabel != null) {
                TimerBadge(
                    label    = timerLabel,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 14.dp)
                        .alpha(timerBadgeAlpha),
                )
            }
        }
    }
}

@Composable
private fun SessionNavBar(
    panelAlpha: Float,
    onHome: () -> Unit,
    onSessions: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .alpha(panelAlpha),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xF0161310))
                .border(0.5.dp, ColorMoonlitCream.copy(alpha = 0.09f), RoundedCornerShape(28.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SessionNavItem(icon = Icons.Filled.Home,             label = "Home",    active = false, onClick = onHome)
            SessionNavItem(icon = Icons.Filled.Pets,        label = "Purr",   active = true,  onClick = onSessions)
            SessionNavItem(icon = Icons.Filled.NightsStay,  label = "Nights", active = false, onClick = onFavorites)
            SessionNavItem(icon = Icons.Filled.Person,           label = "Profile", active = false, onClick = onProfile)
        }
    }
}

@Composable
private fun SessionNavItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) ColorPrimary else ColorMoonlitCream.copy(alpha = 0.28f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tint,
        )
    }
}

@Composable
private fun TimerBadge(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xCC191612))
            .border(0.5.dp, ColorPrimary.copy(alpha = 0.28f), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = Icons.Outlined.Timer,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.80f),
            modifier           = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
            color = ColorMoonlitCream,
        )
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
