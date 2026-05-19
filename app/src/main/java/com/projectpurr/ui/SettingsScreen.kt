package com.projectpurr.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.projectpurr.ui.components.AmbientBackdrop
import com.projectpurr.ui.components.GlassCard
import com.projectpurr.ui.theme.ColorGlassBorder
import com.projectpurr.ui.theme.ColorOnBackground
import com.projectpurr.ui.theme.ColorOnSurface
import com.projectpurr.ui.theme.ColorTextSecondary
import com.projectpurr.ui.theme.ColorTextTertiary

private const val PRIVACY_POLICY_URL = "https://purr.app/privacy"

@Composable
fun SettingsScreen(
    appVersion: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AmbientBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = ColorTextSecondary,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = ColorOnBackground.copy(alpha = 0.92f),
                    )
                }

                Spacer(Modifier.height(24.dp))

                // How it works
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "How it works",
                        style = MaterialTheme.typography.labelLarge,
                        color = ColorOnSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Place your phone face-up on your chest. Press play and breathe slowly. " +
                            "The vibration follows the rhythm of a house cat's purr — felt through " +
                            "your body, not just heard. Use chest mode to dim the screen and block " +
                            "distractions. Enable sleep timer to fade out while you drift off.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTextSecondary,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Links + version
                GlassCard(modifier = Modifier.fillMaxWidth(), innerPadding = 0.dp) {
                    SettingsRow(
                        label   = "Privacy Policy",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)),
                            )
                        },
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color     = ColorGlassBorder,
                    )
                    SettingsRow(label = "Version $appVersion")
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Purr — a quiet companion for restless nights.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextTertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ColorOnSurface)
        if (onClick != null) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = ColorTextSecondary,
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}
