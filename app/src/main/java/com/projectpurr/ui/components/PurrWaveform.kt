package com.projectpurr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projectpurr.engine.PurrEnvelopeSampler
import com.projectpurr.ui.theme.ColorMoonlitCream
import kotlin.math.PI
import kotlin.math.sin

/**
 * Continuous purr line — smoothed envelope when playing, flat line when idle.
 */
@Composable
fun PurrWaveform(
    loopPositionMs: Long,
    sensoryIntensity: Float,
    playing: Boolean,
    modifier: Modifier = Modifier,
    pointCount: Int = 96,
) {
    val ink = ColorMoonlitCream

    Canvas(modifier = modifier.height(SessionPanelMetrics.waveformHeight)) {
        val padX = size.width * 0.04f
        val midY = size.height / 2f
        val usableW = size.width - padX * 2f
        val ampH = size.height * 0.42f
        val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        if (!playing) {
            drawLine(
                color = ink.copy(alpha = 0.14f),
                start = Offset(padX, midY),
                end = Offset(size.width - padX, midY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
            return@Canvas
        }

        val scale = sensoryIntensity.coerceIn(0f, 1f)
        val amps = PurrEnvelopeSampler.sampleSmoothCurve(
            centerLoopMs = loopPositionMs,
            pointCount = pointCount,
            intensityScale = scale,
        )
        if (amps.isEmpty()) return@Canvas

        val path = Path()
        val stepX = usableW / (amps.size - 1).coerceAtLeast(1)

        for (i in amps.indices) {
            val edgeFade = sin((i.toFloat() / (amps.size - 1).coerceAtLeast(1)) * PI).toFloat()
            val amp = amps[i] * edgeFade
            val x = padX + i * stepX
            val y = midY - amp * ampH

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = padX + (i - 1) * stepX
                val prevAmp = amps[i - 1] * sin(((i - 1).toFloat() / (amps.size - 1).coerceAtLeast(1)) * PI).toFloat()
                val prevY = midY - prevAmp * ampH
                val cx = (prevX + x) / 2f
                path.cubicTo(cx, prevY, cx, y, x, y)
            }
        }

        val peak = amps.maxOrNull() ?: 0f
        val alpha = (0.12f + peak * 0.28f).coerceIn(0.10f, 0.38f)
        drawPath(path, color = ink.copy(alpha = alpha), style = stroke)
    }
}
