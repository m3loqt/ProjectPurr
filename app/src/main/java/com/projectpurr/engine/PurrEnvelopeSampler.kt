package com.projectpurr.engine

import kotlin.math.pow

/**
 * Maps loop playback time to the same perceptual energy curve used for haptics.
 * UI waveform uses smoothed sampling so the line feels organic, not pulsed per motor segment.
 */
object PurrEnvelopeSampler {
    private const val GAMMA = 0.62f
    private const val MAX_BASE = 200f
    private const val SMOOTH_RADIUS_MS = 48L

    private val timings = Catpur1AudioHapticEnvelope.timingsMs
    private val baseAmps = Catpur1AudioHapticEnvelope.amplitudesBase

    private val segmentStartMs: LongArray = run {
        val starts = LongArray(timings.size)
        var acc = 0L
        for (i in timings.indices) {
            starts[i] = acc
            acc += timings[i]
        }
        starts
    }

    val loopPeriodMs: Long = Catpur1AudioHapticEnvelope.TOTAL_MS

    fun perceptualEnergyAt(loopPositionMs: Long): Float {
        val wrapped = ((loopPositionMs % loopPeriodMs) + loopPeriodMs) % loopPeriodMs
        val idx = segmentIndexAt(wrapped)
        val base = baseAmps[idx]
        if (base <= 0) return 0f
        val norm = (base / MAX_BASE).coerceIn(0f, 1f)
        return norm.toDouble().pow(GAMMA.toDouble()).toFloat()
    }

    /** Time-averaged energy — softens harsh on/off haptic segments. */
    fun smoothedEnergyAt(loopPositionMs: Long): Float {
        var sum = 0f
        var n = 0
        var dt = -SMOOTH_RADIUS_MS
        while (dt <= SMOOTH_RADIUS_MS) {
            sum += perceptualEnergyAt(loopPositionMs + dt)
            n++
            dt += 12L
        }
        return sum / n
    }

    /**
     * Smooth amplitude curve for a continuous waveform path (not discrete bars).
     */
    fun sampleSmoothCurve(
        centerLoopMs: Long,
        pointCount: Int,
        windowMs: Long = 3_600L,
        intensityScale: Float = 1f,
    ): FloatArray {
        if (pointCount <= 0) return FloatArray(0)
        val step = windowMs.toFloat() / (pointCount - 1).coerceAtLeast(1)
        val half = windowMs / 2f
        val scale = intensityScale.coerceIn(0f, 1f)

        val raw = FloatArray(pointCount) { i ->
            val t = centerLoopMs - half + step * i
            smoothedEnergyAt(t.toLong()) * scale
        }

        if (pointCount < 3) return raw
        var curve = raw
        repeat(2) {
            val smoothed = curve.copyOf()
            for (i in 1 until pointCount - 1) {
                smoothed[i] = (curve[i - 1] + curve[i] * 2f + curve[i + 1]) / 4f
            }
            curve = smoothed
        }
        return curve
    }

    private fun segmentIndexAt(positionMs: Long): Int {
        var lo = 0
        var hi = segmentStartMs.size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val start = segmentStartMs[mid]
            val end = start + timings[mid]
            when {
                positionMs < start -> hi = mid - 1
                positionMs >= end -> lo = mid + 1
                else -> return mid
            }
        }
        return segmentStartMs.size - 1
    }
}
