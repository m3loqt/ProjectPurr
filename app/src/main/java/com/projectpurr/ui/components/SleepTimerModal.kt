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
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.theme.ColorOrbAmber

@Composable
fun SleepTimerModal(
    current: SleepTimerOption,
    onDismiss: () -> Unit,
    onConfirm: (SleepTimerOption) -> Unit,
) {
    val options = SleepTimerOption.entries
    var selectedIndex by remember {
        mutableIntStateOf(options.indexOf(current).coerceAtLeast(0))
    }

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
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                TextButton(
                    onClick = {
                        onConfirm(options[selectedIndex])
                        onDismiss()
                    },
                ) {
                    Text("Set", color = ColorOrbAmber)
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.07f),
                thickness = 0.5.dp,
            )

            WheelPicker(
                items = options.map { it.label },
                initialIndex = selectedIndex,
                onIndexSelected = { selectedIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
