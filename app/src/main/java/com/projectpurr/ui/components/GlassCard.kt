package com.projectpurr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projectpurr.ui.theme.ColorGlassBorder
import com.projectpurr.ui.theme.ColorGlassFill

val GlassShape = RoundedCornerShape(20.dp)

/**
 * Frosted-glass card used throughout the app.
 * Warm translucent fill + subtle border on a dark background reads as glassmorphism
 * without requiring actual blur (which is expensive and requires API 31+).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = ColorGlassFill),
        border = BorderStroke(0.5.dp, ColorGlassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(innerPadding),
            content = content,
        )
    }
}
