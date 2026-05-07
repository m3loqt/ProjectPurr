package com.projectpurr.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.pow

/**
 * Repeating waveform with global [intensity] and optional [queueSessionThermalMultiplier]
 * for long-session taper. Motor output is hard-capped ([MOTOR_ABS_CAP]) for heat safety.
 *
 * Device variance: when [hasAmplitudeControl] is false, falls back to binary on/off timing.
 * The rhythm alone still conveys purr texture on weaker OEM motors.
 * Locations where OEM-specific tuning is most impactful are marked [OEM_TUNING].
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

    /** False on many mid-range and older OEM devices; graceful fallback is binary timing only. */
    val hasAmplitudeControl: Boolean = vibrator.hasAmplitudeControl()

    private var loopTimings: LongArray? = null
    private var loopBaseAmps: IntArray? = null

    @Volatile private var intensity: Float = 1f
    @Volatile private var sessionThermalMultiplier: Float = 1f

    fun startLoop(timings: LongArray, baseAmplitudes: IntArray) {
        require(timings.size == baseAmplitudes.size)
        loopTimings = timings
        loopBaseAmps = baseAmplitudes
        intensity = 1f
        sessionThermalMultiplier = 1f
        restartWaveform()
    }

    /**
     * Scales motor output. Each call cancels and re-posts the vibrator so use sparingly —
     * suitable for fade-in/out steps (~6 calls over 600 ms), not per-frame animation.
     */
    fun setIntensity(scale: Float) {
        intensity = scale.coerceIn(0f, 1f)
        if (loopTimings != null) restartWaveform()
    }

    /**
     * Long-session gentle taper. Queued and applied at the next natural loop restart
     * (via [restartLoop]) so thermal changes don't break audio/haptic sync mid-waveform.
     *
     * [OEM_TUNING]: Devices with very weak motors may need a higher THERMAL_FLOOR in
     * PurrSessionEngine to keep the purr perceptible after the taper kicks in.
     */
    fun queueSessionThermalMultiplier(multiplier: Float) {
        sessionThermalMultiplier = multiplier.coerceIn(0f, 1f)
        // Intentionally NOT calling restartWaveform() here — applied on next natural boundary.
    }

    /** Called at each loop boundary to keep haptic phase aligned with audio. */
    fun restartLoop() {
        if (loopTimings != null) restartWaveform()
    }

    /**
     * Suspends the vibrator WITHOUT clearing waveform state.
     * Use for transient interruptions (audio focus loss) so [restartLoop] can resume cleanly.
     * Contrast with [stop] which also clears all waveform data.
     */
    fun pause() {
        vibrator.cancel()
    }

    /** Fully stops the vibrator and clears all waveform state. */
    fun stop() {
        loopTimings = null
        loopBaseAmps = null
        sessionThermalMultiplier = 1f
        vibrator.cancel()
    }

    private fun effectiveMotorScale(): Float =
        (intensity * sessionThermalMultiplier).coerceIn(0f, 1f)

    /**
     * γ < 1 compresses the low end of the amplitude range so mid-range intensities feel
     * organic rather than harsh. This is the single most impactful constant for perceived
     * purr texture on real devices.
     *
     * [OEM_TUNING]: Most sensitive constant for device-specific tuning.
     * Range: 0.45 (very soft, slightly muffled) → 0.85 (more linear, buzzier).
     * Current value 0.62 tuned on Pixel 6 / Galaxy S22.
     */
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
        // If scale is effectively zero, just cancel — don't post an all-zero waveform.
        if (effectiveMotorScale() <= 0f) return
        val base = loopBaseAmps ?: return
        val effect = if (hasAmplitudeControl) {
            val scaled = IntArray(base.size) { i ->
                val a = base[i]
                if (a == 0) 0 else perceptualMotorAmp(a)
                // perceptualMotorAmp already returns 0 when output is below threshold;
                // do NOT coerce 0→1 here or fade-outs will never reach true silence.
            }
            // -1 = play once; engine re-triggers each loop so audio and haptic stay aligned.
            VibrationEffect.createWaveform(timings, scaled, -1)
        } else {
            // [OEM_TUNING]: Binary on/off fallback. The rhythm still conveys purr texture.
            // For devices with very weak motors, consider reducing duty cycle in the envelope.
            VibrationEffect.createWaveform(timings, -1)
        }
        vibrator.vibrate(effect)
    }

    companion object {
        /**
         * Perceptual gamma for motor amplitude curve.
         * [OEM_TUNING]: Most impactful constant for device-specific purr texture.
         */
        private const val CURVE_GAMMA = 0.62f

        /** Must match AMP_HI in tools/generate_catpur_haptic_envelope.py. */
        private const val MAX_BASE_AMP_EXPECTED = 200f

        /**
         * Hard amplitude ceiling for motor heat safety across extended sessions.
         * [OEM_TUNING]: Lower to 150–175 on devices that run noticeably warm at 200.
         */
        private const val MOTOR_ABS_CAP = 200
    }
}
