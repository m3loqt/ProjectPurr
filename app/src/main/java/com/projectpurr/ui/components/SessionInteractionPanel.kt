package com.projectpurr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorOrbAmber
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextTertiary

@Composable
fun SessionInteractionPanel(
    playing: Boolean,
    loopPositionMs: Long,
    sensoryIntensity: Float,
    profileLabel: String,
    whisperPhrase: String,
    textAlpha: Float,
    silentPurr: Boolean,
    forceSpeaker: Boolean,
    chestMode: Boolean,
    sleepTimer: SleepTimerOption,
    panelAlpha: Float,
    onTogglePlay: () -> Unit,
    onSleepTimerChange: (SleepTimerOption) -> Unit,
    onToggleSilent: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleChestMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playSize = if (playing) 68.dp else 72.dp
    var showSleepTimerModal by remember { mutableStateOf(false) }

    // Live session elapsed time — starts fresh each time playing becomes true
    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(playing) {
        if (playing) {
            val startMs = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                elapsedMs = System.currentTimeMillis() - startMs
            }
        } else {
            elapsedMs = 0L
        }
    }
    val elapsedLabel = if (playing) {
        val s = elapsedMs / 1000
        "%d:%02d".format(s / 60, s % 60)
    } else ""

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(panelAlpha * textAlpha)
                .padding(horizontal = SessionPanelMetrics.contentHorizontal)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text          = profileLabel,
                style         = MaterialTheme.typography.labelSmall,
                color         = ColorMoonlitCream.copy(alpha = 0.58f),
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = whisperPhrase,
                style      = MaterialTheme.typography.headlineLarge,
                color      = ColorMoonlitCream.copy(alpha = 0.80f),
                textAlign  = TextAlign.Center,
                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight * 1.02f,
            )
        }

        SmokedInteractionSurface(modifier = Modifier.alpha(panelAlpha)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SessionPanelMetrics.ritualRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: live session duration counter
                RitualControl(
                    label    = "Duration",
                    sublabel = elapsedLabel,
                    icon     = Icons.Outlined.HourglassEmpty,
                    active   = playing,
                    modifier = Modifier.width(SessionPanelMetrics.ritualSlotWidth),
                    onClick  = {},
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(SessionPanelMetrics.ritualRowHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    BreathingPlayOrb(
                        playing = playing,
                        onClick = onTogglePlay,
                        size = playSize,
                        embedded = true,
                    )
                }

                RitualControl(
                    label = "Sleep timer",
                    sublabel = sleepTimerSublabel(sleepTimer),
                    icon = Icons.Outlined.Timer,
                    active = sleepTimer != SleepTimerOption.OFF,
                    modifier = Modifier.width(SessionPanelMetrics.ritualSlotWidth),
                    onClick = { showSleepTimerModal = true },
                )
            }

            Spacer(Modifier.height(SessionPanelMetrics.rowToWaveform))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                PurrWaveform(
                    playing = playing,
                    loopPositionMs = loopPositionMs,
                    sensoryIntensity = sensoryIntensity,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(SessionPanelMetrics.waveformToIcons))

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconToggle(
                    icon               = Icons.AutoMirrored.Outlined.VolumeUp,
                    label              = "Speaker",
                    active             = forceSpeaker,
                    contentDescription = "Phone speaker",
                    onClick            = onToggleSpeaker,
                    modifier           = Modifier.width(SessionPanelMetrics.ritualSlotWidth),
                )
                Box(
                    modifier         = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    SoftIconToggle(
                        icon               = Icons.Outlined.Bedtime,
                        label              = "Chest mode",
                        active             = chestMode,
                        contentDescription = "Chest mode",
                        onClick            = onToggleChestMode,
                    )
                }
                SoftIconToggle(
                    icon               = Icons.AutoMirrored.Outlined.VolumeOff,
                    label              = "Silent",
                    active             = silentPurr,
                    contentDescription = "Silent purr",
                    onClick            = onToggleSilent,
                    modifier           = Modifier.width(SessionPanelMetrics.ritualSlotWidth),
                )
            }
        }
    }

    if (showSleepTimerModal) {
        SleepTimerModal(
            current   = sleepTimer,
            onDismiss = { showSleepTimerModal = false },
            onConfirm = { option -> onSleepTimerChange(option); showSleepTimerModal = false },
        )
    }
}

@Composable
private fun RitualControl(
    label: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) ColorPrimary else ColorMoonlitCream.copy(alpha = 0.26f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ColorMoonlitCream.copy(alpha = 0.36f),
            textAlign = TextAlign.Center,
        )
        if (sublabel.isNotEmpty()) {
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = ColorOrbAmber.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SoftIconToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (active) ColorPrimary else ColorMoonlitCream.copy(alpha = 0.55f)
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF252118)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = contentDescription,
                tint               = tint,
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = ColorMoonlitCream.copy(alpha = 0.36f),
        )
    }
}

private fun sleepTimerSublabel(timer: SleepTimerOption): String = when (timer) {
    SleepTimerOption.OFF -> ""
    SleepTimerOption.M10 -> "10m"
    SleepTimerOption.M20 -> "20m"
    SleepTimerOption.M30 -> "30m"
}
