package com.projectpurr.ui.components

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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorOrbAmber
import com.projectpurr.ui.theme.ColorTextTertiary

@Composable
fun SessionInteractionPanel(
    playing: Boolean,
    loopPositionMs: Long,
    sensoryIntensity: Float,
    profileLabel: String,
    whisperPhrase: String,
    timerLabel: String?,
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
    var showAlarmModal by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Typography sits above the glass slab — atmospheric, not encased
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(panelAlpha * textAlpha)
                .padding(horizontal = SessionPanelMetrics.contentHorizontal)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = profileLabel,
                style = MaterialTheme.typography.labelSmall,
                color = ColorMoonlitCream.copy(alpha = 0.80f),
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = whisperPhrase,
                style = MaterialTheme.typography.headlineLarge,
                color = ColorMoonlitCream.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight * 1.02f,
            )

            if (timerLabel != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = timerLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorOrbAmber.copy(alpha = 0.44f),
                )
            }
        }

        SmokedInteractionSurface(modifier = Modifier.alpha(panelAlpha)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SessionPanelMetrics.ritualRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RitualControl(
                    label = "Set alarm",
                    sublabel = sleepTimerSublabel(sleepTimer),
                    icon = Icons.Outlined.Alarm,
                    active = sleepTimer != SleepTimerOption.OFF,
                    modifier = Modifier.width(SessionPanelMetrics.ritualSlotWidth),
                    onClick = { showAlarmModal = true },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SessionPanelMetrics.waveformHeight),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SessionPanelMetrics.iconRowHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconToggle(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    active = forceSpeaker,
                    contentDescription = "Phone speaker",
                    onClick = onToggleSpeaker,
                )
                SoftIconToggle(
                    icon = Icons.Outlined.Bedtime,
                    active = chestMode,
                    contentDescription = "Chest mode",
                    onClick = onToggleChestMode,
                )
                SoftIconToggle(
                    icon = Icons.AutoMirrored.Outlined.VolumeOff,
                    active = silentPurr,
                    contentDescription = "Silent purr",
                    onClick = onToggleSilent,
                )
            }
        }
    }

    if (showSleepTimerModal) {
        SleepTimerModal(
            current = sleepTimer,
            onDismiss = { showSleepTimerModal = false },
            onConfirm = { option -> onSleepTimerChange(option); showSleepTimerModal = false },
        )
    }
    if (showAlarmModal) {
        AlarmModal(onDismiss = { showAlarmModal = false })
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
            tint = ColorMoonlitCream.copy(alpha = if (active) 0.54f else 0.34f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ColorMoonlitCream.copy(alpha = 0.46f),
            textAlign = TextAlign.Center,
        )
        if (sublabel.isNotEmpty()) {
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = ColorOrbAmber.copy(alpha = 0.36f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SoftIconToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(SessionPanelMetrics.iconRowHeight)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = ColorMoonlitCream.copy(alpha = if (active) 0.58f else 0.26f),
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun sleepTimerSublabel(timer: SleepTimerOption): String = when (timer) {
    SleepTimerOption.OFF -> ""
    SleepTimerOption.M10 -> "10m"
    SleepTimerOption.M20 -> "20m"
    SleepTimerOption.M30 -> "30m"
}
