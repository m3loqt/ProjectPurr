package com.projectpurr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Composition rhythm — reference panel pacing. */
object SessionPanelMetrics {
    val slabCorner            = 28.dp
    val slabHorizontalInset   = 20.dp
    val slabBottomInset       = 24.dp
    val contentHorizontal     = 28.dp
    val contentTop            = 16.dp
    val contentBottom         = 16.dp
    val textToControls        = 12.dp
    val ritualRowHeight       = 72.dp
    val ritualSlotWidth       = 76.dp
    val rowToWaveform         = 10.dp
    val waveformHeight        = 26.dp
    val waveformToIcons       = 10.dp
    val iconRowHeight         = 40.dp
}

private val SlabShape = RoundedCornerShape(SessionPanelMetrics.slabCorner)

@Composable
fun SmokedInteractionSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SessionPanelMetrics.slabHorizontalInset,
                vertical   = SessionPanelMetrics.slabBottomInset,
            ),
    ) {
        Box(modifier = Modifier.matchParentSize().clip(SlabShape)) {
            // Same opaque warm charcoal as HomeScreen cards
            Box(Modifier.matchParentSize().background(Color(0xFF191612)))

            // Bottom depth shadow
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.76f to Color.Transparent,
                            1.00f to Color(0x1A060402),
                        ),
                    ),
                ),
            )

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start   = SessionPanelMetrics.contentHorizontal,
                    end     = SessionPanelMetrics.contentHorizontal,
                    top     = SessionPanelMetrics.contentTop,
                    bottom  = SessionPanelMetrics.contentBottom,
                ),
            content = content,
        )
    }
}
