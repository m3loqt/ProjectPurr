package com.projectpurr.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.pow

/**
 * Repeating waveform with global [intensity] and optional [setSessionThermalMultiplier]
 * for long-session taper. Motor output is hard-capped ([MOTOR_ABS_CAP]) for heat safety.
 */
class PurrHapticPlayer(context: Context) {
    private val app = context.applicationContext

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = app.getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Vibrator::class.java)!!
        }

    private val hasAmplitudeControl: Boolean = vibrator.hasAmplitudeControl()

    private var loopTimings: LongArray? = null
    private var loopBaseAmps: IntArray? = null

    @Volatile
    private var intensity: Float = 1f

    @Volatile
    private var sessionThermalMultiplier: Float = 1f

    fun startLoop(timings: LongArray, baseAmplitudes: IntArray) {
        require(timings.size == baseAmplitudes.size)
        loopTimings = timings
        loopBaseAmps = baseAmplitudes
        intensity = 1f
        sessionThermalMultiplier = 1f
        restartWaveform()
    }

    fun setIntensity(scale: Float) {
        intensity = scale.coerceIn(0f, 1f)
        if (loopTimings != null) {
            restartWaveform()
        }
    }

    /** Long-session gentle taper; only applied while a loop is running. Updates are infrequent (engine-side). */
    fun setSessionThermalMultiplier(multiplier: Float) {
        sessionThermalMultiplier = multiplier.coerceIn(0f, 1f)
        if (loopTimings != null) {
            restartWaveform()
        }
    }

    fun stop() {
        loopTimings = null
        loopBaseAmps = null
        sessionThermalMultiplier = 1f
        vibrator.cancel()
    }

    private fun effectiveMotorScale(): Float =
        (intensity * sessionThermalMultiplier).coerceIn(0f, 1f)

    /** γ < 1 stretches mid-range more than linear (less “plastic ruler”). */
    private fun perceptualMotorAmp(baseAmp: Int): Int {
        val intensityScale = effectiveMotorScale()
        if (baseAmp <= 0 || intensityScale <= 0f) return 0
        val normalized = (baseAmp.toFloat() / MAX_BASE_AMP_EXPECTED).coerceIn(0f, 1f)
        val curved = normalized.toDouble().pow(CURVE_GAMMA.toDouble()).toFloat()
        val motorOut = curved * 255f * intensityScale
        return when {
            motorOut < 1f -> 0
            else -> motorOut.toInt().coerceIn(1, MOTOR_ABS_CAP)
        }
    }

    private fun restartWaveform() {
        val timings = loopTimings ?: return
        vibrator.cancel()
        val base = loopBaseAmps ?: return
        val effect = if (hasAmplitudeControl) {
            val scaled = IntArray(base.size) { i ->
                when (val a = base[i]) {
                    0 -> 0
                    else -> perceptualMotorAmp(a).coerceIn(1, MOTOR_ABS_CAP)
                }
            }
            VibrationEffect.createWaveform(timings, scaled, 0)
        } else {
            VibrationEffect.createWaveform(timings, 0)
        }
        vibrator.vibrate(effect)
    }

    companion object {
        private const val CURVE_GAMMA = 0.62f

        /** Slightly above max generated envelope so curve uses full-ish range below ceiling. */
        private const val MAX_BASE_AMP_EXPECTED = 58f

        /** Hard ceiling on waveform amplitudes (below 255) to reduce motor heat across devices. */
        private const val MOTOR_ABS_CAP = 200
    }
}
