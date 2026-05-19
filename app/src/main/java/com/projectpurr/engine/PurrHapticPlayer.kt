package com.projectpurr.engine

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * Repeating waveform with global [intensity] and optional [queueSessionThermalMultiplier]
 * for long-session taper. Motor output is hard-capped ([MOTOR_ABS_CAP]) for heat safety.
 *
 * Device variance: when [hasAmplitudeControl] is false, falls back to binary on/off timing.
 * The rhythm alone still conveys purr texture on weaker OEM motors.
 * Locations where OEM-specific tuning is most impactful are marked [OEM_TUNING].
 */
class PurrHapticPlayer(context: Context, private val scope: CoroutineScope) {
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

    /** True while a vibration should be active — used by the interrupt-recovery watchdog. */
    @Volatile private var shouldBeVibrating = false

    /** True while we are in the middle of our own cancel+vibrate cycle — prevents re-entry. */
    @Volatile private var isRestarting = false

    /**
     * Monotonic timestamp of the last time we posted a VibrationEffect.
     * Used by the watchdog to know when we last kicked the vibrator, so we can detect
     * a notification hijack: if too much wall-clock time passes without a natural loop
     * restart (via [restartLoop]) while [shouldBeVibrating] is true, the waveform was
     * likely stolen and we re-post immediately rather than waiting up to ~15 s.
     */
    @Volatile private var lastVibrateMs: Long = 0L

    init {
        // Interrupt-recovery watchdog: polls at INTERRUPT_POLL_MS intervals. If the engine
        // says we should be vibrating but [INTERRUPT_POLL_MS * 2] has elapsed since we last
        // posted the waveform (far less than the 14.76 s loop period), we restart immediately.
        // restartWaveform() is cancel-then-post, so calling it while still vibrating from an
        // earlier post is safe — it just resets the waveform phase, which is acceptable here.
        scope.launch {
            while (isActive) {
                delay(INTERRUPT_POLL_MS)
                val sinceLastVibrate = System.currentTimeMillis() - lastVibrateMs
                if (shouldBeVibrating && !isRestarting && sinceLastVibrate > INTERRUPT_POLL_MS * 2) {
                    restartWaveform()
                }
            }
        }
    }

    fun startLoop(timings: LongArray, baseAmplitudes: IntArray) {
        require(timings.size == baseAmplitudes.size)
        loopTimings = timings
        loopBaseAmps = baseAmplitudes
        intensity = 0f
        sessionThermalMultiplier = 1f
        vibrator.cancel()
        // First motor output happens when intensity rises (startup ramp) — avoids
        // full-blast-then-cancel which can leave some OEM motors silent until reboot.
    }

    /**
     * Scales motor output. Each call cancels and re-posts the vibrator so use sparingly —
     * suitable for fade-in/out steps (~6 calls over 600 ms), not per-frame animation.
     */
    fun setIntensity(scale: Float) {
        intensity = scale.coerceIn(0f, 1f)
        if (loopTimings == null) return
        if (intensity <= 0f) {
            vibrator.cancel()
            return
        }
        restartWaveform()
    }

    /**
     * Long-session gentle taper. Queued and applied at the next natural loop restart
     * (via [restartLoop]) so thermal changes don't break audio/haptic sync mid-waveform.
     *
     * [OEM_TUNING]: Devices with very weak motors may need a higher THERMAL_FLOOR in
     * PurrSessionEngine to keep the purr perceptible after the taper kicks in.
     */
    fun queueSessionThermalMultiplier(multiplier: Float) {
        val next = multiplier.coerceIn(0f, 1f)
        if (next == sessionThermalMultiplier) return
        sessionThermalMultiplier = next
        if (loopTimings != null && intensity > 0f) restartWaveform()
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
        shouldBeVibrating = false
        vibrator.cancel()
    }

    /** Fully stops the vibrator and clears all waveform state. */
    fun stop() {
        shouldBeVibrating = false
        loopTimings = null
        loopBaseAmps = null
        sessionThermalMultiplier = 1f
        vibrator.cancel()
    }

    /**
     * Plays a short (~1.5 s) sample of the house-cat envelope so users can feel a locked
     * profile before unlocking it. No-ops while a session is active to avoid interference.
     */
    fun playPreview() {
        if (loopTimings != null) return
        val timings = Catpur1AudioHapticEnvelope.timingsMs
        val base    = Catpur1AudioHapticEnvelope.amplitudesBase
        var acc = 0L
        val pt = mutableListOf<Long>()
        val pa = mutableListOf<Int>()
        for (i in timings.indices) {
            val t = timings[i]
            if (acc + t > PREVIEW_MS) {
                val rem = PREVIEW_MS - acc
                if (rem > 0) { pt.add(rem); pa.add(base[i]) }
                break
            }
            pt.add(t); pa.add(base[i])
            acc += t
            if (acc >= PREVIEW_MS) break
        }
        if (pt.isEmpty()) return
        val effect = if (hasAmplitudeControl) {
            VibrationEffect.createWaveform(
                pt.toLongArray(),
                pa.map { a ->
                    if (a == 0) 0 else {
                        val norm   = (a.toFloat() / 200f).coerceIn(0f, 1f)
                        val curved = norm.toDouble().pow(CURVE_GAMMA.toDouble()).toFloat()
                        (curved * 200f).toInt().coerceIn(1, MOTOR_ABS_CAP)
                    }
                }.toIntArray(),
                -1,
            )
        } else {
            VibrationEffect.createWaveform(pt.toLongArray(), -1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attrs = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_MEDIA)
                .build()
            vibrator.vibrate(effect, attrs)
        } else {
            @Suppress("DEPRECATION")
            vibrateWithAudioAttrs(effect)
        }
    }

    /** Combined ramp × thermal scale — same energy profile the waveform should reflect. */
    fun outputScale(): Float = effectiveMotorScale()

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
        shouldBeVibrating = true
        val timings = loopTimings ?: return
        isRestarting = true
        vibrator.cancel()
        // If scale is effectively zero, just cancel — don't post an all-zero waveform.
        if (effectiveMotorScale() <= 0f) {
            isRestarting = false
            return
        }
        val base = loopBaseAmps ?: run { isRestarting = false; return }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attrs = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_MEDIA)
                .build()
            vibrator.vibrate(effect, attrs)
        } else {
            @Suppress("DEPRECATION")
            vibrateWithAudioAttrs(effect)
        }
        lastVibrateMs = System.currentTimeMillis()
        isRestarting = false
    }

    @Suppress("DEPRECATION")
    private fun vibrateWithAudioAttrs(effect: VibrationEffect) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        vibrator.vibrate(effect, attrs)
    }

    companion object {
        private const val CURVE_GAMMA = 0.62f
        private const val MAX_BASE_AMP_EXPECTED = 200f
        private const val MOTOR_ABS_CAP = 200
        private const val PREVIEW_MS = 1_500L

        /**
         * Watchdog poll interval. A notification typically returns the vibrator within ~1–2 s.
         * Polling at 2 s means worst-case ~4 s of silence before we recover — far better than
         * waiting for the next 14.76 s loop boundary (up to ~15 s silence).
         */
        private const val INTERRUPT_POLL_MS = 2_000L
    }
}
