package com.projectpurr.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectpurr.R
import com.projectpurr.engine.SleepTimerOption
import com.projectpurr.ui.components.CinematicTextureOverlay
import com.projectpurr.ui.components.SleepTimerModal
import com.projectpurr.ui.theme.ColorMoonlitCream
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary

private val SPageBg    = Color(0xFF090704)
private val SCardBg    = Color(0xFF191612)
private val SCardShape = RoundedCornerShape(18.dp)

@Composable
fun SettingsScreen(
    appVersion: String,
    chestMode: Boolean,
    silentPurr: Boolean,
    sleepTimer: SleepTimerOption,
    onChestModeChange: (Boolean) -> Unit = {},
    onSilentPurrChange: (Boolean) -> Unit = {},
    onSleepTimerChange: (SleepTimerOption) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onBack: () -> Unit,
) {
    var showClearConfirm  by remember { mutableStateOf(false) }
    var showTimerPicker   by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SPageBg),
    ) {
        CinematicTextureOverlay()

        LazyColumn(
            modifier       = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            item { SettingsHeader(onBack) }
            item { Spacer(Modifier.height(20.dp)) }
            item { ExperienceCard(chestMode, silentPurr, sleepTimer, onChestModeChange, onSilentPurrChange, onTimerTap = { showTimerPicker = true }) }
            item { Spacer(Modifier.height(10.dp)) }
            item { PrivacyCard(onClearHistoryTap = { showClearConfirm = true }) }
            item { Spacer(Modifier.height(10.dp)) }
            item { AboutCard(appVersion) }
            item { Spacer(Modifier.height(32.dp)) }
            item { SettingsFooter() }
        }

        if (showClearConfirm) {
            ClearHistoryDialog(
                onConfirm = { showClearConfirm = false; onClearHistory() },
                onDismiss = { showClearConfirm = false },
            )
        }

        if (showTimerPicker) {
            SleepTimerModal(
                current   = sleepTimer,
                onDismiss = { showTimerPicker = false },
                onConfirm = { option -> onSleepTimerChange(option) },
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        // Cat image — top right, partial
        Image(
            painter            = painterResource(R.drawable.herocard),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(220.dp),
        )

        // Left gradient — fades image out toward text
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to SPageBg,
                        0.38f to SPageBg.copy(alpha = 0.96f),
                        0.58f to SPageBg.copy(alpha = 0.65f),
                        0.80f to SPageBg.copy(alpha = 0.18f),
                        1.00f to Color.Transparent,
                    ),
                ),
            ),
        )

        // Bottom fade — dissolve into page bg
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.50f to Color.Transparent,
                        1.00f to SPageBg,
                    ),
                ),
            ),
        )

        // Back button — top left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint               = ColorMoonlitCream.copy(alpha = 0.50f),
                modifier           = Modifier.size(20.dp),
            )
        }

        // Title + subtitle — bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Text(
                text  = "Settings",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize   = 42.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 50.sp,
                ),
                color = ColorMoonlitCream,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Keep it quiet. Keep it yours.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = ColorTextSecondary.copy(alpha = 0.58f),
            )
        }
    }
}

// ── Shared card primitives ────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, label: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.70f),
            modifier           = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 10.sp,
                letterSpacing = 1.5.sp,
            ),
            color = ColorPrimary.copy(alpha = 0.70f),
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(ColorMoonlitCream.copy(alpha = 0.07f)),
    )
}

// ── Icon container — matches Home's IconCircle ────────────────────────────────

@Composable
private fun SIconCircle(icon: ImageVector) {
    Box(
        modifier         = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF252118)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.90f),
            modifier           = Modifier.size(17.dp),
        )
    }
}

// ── Settings row types ────────────────────────────────────────────────────────

@Composable
private fun ValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SIconCircle(icon)
        Spacer(Modifier.width(14.dp))
        Text(
            text     = title,
            style    = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color    = ColorMoonlitCream.copy(alpha = 0.88f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = ColorTextSecondary.copy(alpha = 0.55f),
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = ColorTextSecondary.copy(alpha = 0.35f),
            modifier           = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SIconCircle(icon)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = ColorMoonlitCream.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.48f),
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = ColorMoonlitCream,
                checkedTrackColor   = Color(0xFF4A3018),
                checkedBorderColor  = Color.Transparent,
                uncheckedThumbColor = ColorMoonlitCream.copy(alpha = 0.30f),
                uncheckedTrackColor = Color(0xFF1E1A14),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SIconCircle(icon)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = ColorMoonlitCream.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = ColorTextSecondary.copy(alpha = 0.48f),
            )
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = ColorTextSecondary.copy(alpha = 0.35f),
            modifier           = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SIconCircle(icon)
        Spacer(Modifier.width(14.dp))
        Text(
            text     = title,
            style    = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color    = ColorMoonlitCream.copy(alpha = 0.88f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = ColorTextSecondary.copy(alpha = 0.50f),
        )
    }
}

// ── EXPERIENCE card ───────────────────────────────────────────────────────────

@Composable
private fun ExperienceCard(
    chestMode: Boolean,
    silentPurr: Boolean,
    sleepTimer: SleepTimerOption,
    onChestModeChange: (Boolean) -> Unit,
    onSilentPurrChange: (Boolean) -> Unit,
    onTimerTap: () -> Unit = {},
) {
    val sleepLabel = sleepTimer.label

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(SCardShape)
            .background(SCardBg),
    ) {
        SectionHeader(Icons.Outlined.AutoAwesome, "EXPERIENCE")
        RowDivider()
        ValueRow(
            icon    = Icons.Filled.AccessTime,
            title   = "Sleep timer",
            value   = sleepLabel,
            onClick = onTimerTap,
        )
        RowDivider()
        ToggleRow(
            icon            = Icons.Filled.Bedtime,
            title           = "Chest mode",
            subtitle        = "Open with immersive chest experience",
            checked         = chestMode,
            onCheckedChange = onChestModeChange,
        )
        RowDivider()
        ToggleRow(
            icon            = Icons.Filled.Vibration,
            title           = "Silent purr",
            subtitle        = "Begin sessions with haptics only",
            checked         = silentPurr,
            onCheckedChange = onSilentPurrChange,
        )
    }
}

// ── PRIVACY card ──────────────────────────────────────────────────────────────

@Composable
private fun PrivacyCard(onClearHistoryTap: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(SCardShape)
            .background(SCardBg),
    ) {
        SectionHeader(Icons.Outlined.Lock, "PRIVACY")
        RowDivider()
        ActionRow(
            icon     = Icons.Filled.Delete,
            title    = "Clear history",
            subtitle = "Delete all nights and local records",
            onClick  = onClearHistoryTap,
        )
    }
}

@Composable
private fun ClearHistoryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SCardShape)
                .background(Color(0xFF1C1915))
                .border(0.5.dp, ColorMoonlitCream.copy(alpha = 0.10f), SCardShape)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier         = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF252118)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = null,
                    tint               = ColorPrimary.copy(alpha = 0.85f),
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text      = "Clear all history?",
                style     = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color     = ColorMoonlitCream,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "This will permanently delete all your rest sessions and reset your count.",
                style     = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color     = ColorTextSecondary.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF252118))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onDismiss,
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        color = ColorMoonlitCream.copy(alpha = 0.70f),
                    )
                }
                // Confirm
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF3A1A10))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onConfirm,
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Clear",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        color = Color(0xFFE07050),
                    )
                }
            }
        }
    }
}

// ── ABOUT card ────────────────────────────────────────────────────────────────

@Composable
private fun AboutCard(appVersion: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(SCardShape)
            .background(SCardBg),
    ) {
        SectionHeader(Icons.Outlined.Eco, "ABOUT")
        RowDivider()
        InfoRow(
            icon  = Icons.Filled.Info,
            title = "Version",
            value = appVersion,
        )
        RowDivider()
        InfoRow(
            icon  = Icons.Filled.Favorite,
            title = "Credits",
            value = "Built for quiet nights.",
        )
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsFooter() {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Thank you for being here.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = ColorTextSecondary.copy(alpha = 0.38f),
        )
        Spacer(Modifier.height(8.dp))
        Icon(
            imageVector        = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint               = ColorPrimary.copy(alpha = 0.35f),
            modifier           = Modifier.size(12.dp),
        )
    }
}
