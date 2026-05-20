package com.projectpurr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val TrackColor  = Color(0xFF1A1107)   // deep amber-dark surface
private val HandleColor = Color(0xFFBB7C34)   // amber (matches "Begin Session" button)
private val LabelColor  = Color(0xFFE8DCC8)   // moonlit cream

/** Plain amber pill — used on onboarding pages 1 and 2. */
@Composable
fun OnboardingPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(HandleColor)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF1A0E05),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = Color(0xFF1A0E05),
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

/** Swipe handle right to complete — last onboarding screen. */
@Composable
fun OnboardingSwipeButton(
    label: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val handlePx = with(density) { 48.dp.toPx() }
    val padPx = with(density) { 4.dp.toPx() }

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var dragPx by remember { mutableFloatStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    val maxDrag = (trackWidthPx - handlePx - padPx * 2f).coerceAtLeast(0f)
    val threshold = maxDrag * 0.82f

    val displayDrag by animateFloatAsState(
        targetValue = if (completed) maxDrag else dragPx,
        label = "swipeDrag",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(TrackColor)
            .onSizeChanged { trackWidthPx = it.width.toFloat() },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = LabelColor.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 64.dp),
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(displayDrag.roundToInt(), 0) }
                .padding(4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(HandleColor)
                .pointerInput(maxDrag, threshold) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragPx >= threshold) {
                                completed = true
                                dragPx = maxDrag
                                onComplete()
                            } else {
                                dragPx = 0f
                            }
                        },
                    ) { _, delta ->
                        dragPx = (dragPx + delta).coerceIn(0f, maxDrag)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            RowChevrons()
        }
    }
}

@Composable
private fun RowChevrons() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 2.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LabelColor,
            modifier = Modifier.size(20.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LabelColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
    }
}
