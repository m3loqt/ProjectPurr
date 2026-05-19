package com.projectpurr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorOrbAmber

@Composable
fun FloatingToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    val bg = if (active) ColorOrbAmber.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)
    val textColor = if (active) ColorMoonlitCream.copy(alpha = 0.92f) else ColorMoonlitCream.copy(alpha = 0.42f)

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
fun FloatingTimerRow(
    selected: SleepTimerOption,
    onSelect: (SleepTimerOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        SleepTimerOption.entries.forEach { opt ->
            val active = selected == opt
            val label = when (opt) {
                SleepTimerOption.OFF -> "Off"
                SleepTimerOption.M10 -> "10m"
                SleepTimerOption.M20 -> "20m"
                SleepTimerOption.M30 -> "30m"
            }
            FloatingToggleChip(
                label = label,
                active = active,
                onClick = { onSelect(opt) },
            )
        }
    }
}
