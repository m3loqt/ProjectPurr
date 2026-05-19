package com.projectpurr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.projectpurr.ui.theme.ColorOrbAmber

@Composable
fun AlarmModal(onDismiss: () -> Unit) {
    val hours = (1..12).map { it.toString() }
    val minutes = (0..59).map { "%02d".format(it) }
    val periods = listOf("AM", "PM")

    var hourIndex by remember { mutableIntStateOf(6) }     // default "7"
    var minuteIndex by remember { mutableIntStateOf(0) }   // default "00"
    var periodIndex by remember { mutableIntStateOf(0) }   // default "AM"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1B18)),
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.55f))
                }
                Text(
                    text = "Set Alarm",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onDismiss) {
                    Text("Set", color = ColorOrbAmber)
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.07f),
                thickness = 0.5.dp,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hours wheel
                WheelPicker(
                    items = hours,
                    initialIndex = hourIndex,
                    onIndexSelected = { hourIndex = it },
                    modifier = Modifier.weight(2f),
                )

                Text(
                    text = ":",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.headlineMedium,
                )

                // Minutes wheel
                WheelPicker(
                    items = minutes,
                    initialIndex = minuteIndex,
                    onIndexSelected = { minuteIndex = it },
                    modifier = Modifier.weight(2f),
                )

                // AM/PM wheel
                WheelPicker(
                    items = periods,
                    initialIndex = periodIndex,
                    onIndexSelected = { periodIndex = it },
                    modifier = Modifier.weight(1.5f),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
