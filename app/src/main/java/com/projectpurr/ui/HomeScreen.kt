package com.projectpurr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.projectpurr.ui.components.AmbientBackdrop
import com.projectpurr.ui.components.GlassCard
import com.projectpurr.ui.theme.ColorOnPrimary
import com.projectpurr.ui.theme.ColorOnSurface
import com.projectpurr.ui.theme.ColorPrimary
import com.projectpurr.ui.theme.ColorTextSecondary
import com.projectpurr.ui.theme.ColorTextTertiary

@Composable
fun HomeScreen(onSelectHouseCat: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AmbientBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(34.dp))

                Text(
                    text = "Project Purr",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ColorOnSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A warm quiet companion for the night.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextSecondary,
                )

                Spacer(Modifier.height(40.dp))

                // Featured cat — House Cat
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    innerPadding = 24.dp,
                ) {
                    Text(
                        "House Cat",
                        style = MaterialTheme.typography.titleLarge,
                        color = ColorOnSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Low, steady purr shaped for chest comfort and calm breathing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTextSecondary,
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = onSelectHouseCat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary,
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Begin Session", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    "More companions",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTextSecondary,
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LockedCatCard(name = "Ragdoll", modifier = Modifier.weight(1f))
                    LockedCatCard(name = "Maine Coon", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LockedCatCard(name: String, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.alpha(0.5f),
        innerPadding = 16.dp,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = ColorOnSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.labelSmall,
            color = ColorTextTertiary,
        )
    }
}
