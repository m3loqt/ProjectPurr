package com.projectpurr.engine

/**
 * **House Cat** tuning and the haptic waveform used while playing.
 *
 * Haptics use a **rolled‑R‑style burst carrier** (~6 ms tap / ~18 ms gap, 24 ms cycle) driven by RMS
 * from `res/raw/catpur1.mp3` (20 ms analysis grid sampled each cycle). Regenerate via
 * `python3 tools/generate_catpur_haptic_envelope.py` after swapping the MP3.
 *
 * [PURR_LOOP_PERIOD_MS] must always match [Catpur1AudioHapticEnvelope.TOTAL_MS] and the sum of
 * [loopTimingsMs] so the vibrator repeats with [MediaPlayer]’s loop.
 */
object HouseCatProfile {
    const val FADE_OUT_MS: Long = 4_000L

    const val AUDIO_TARGET_VOLUME: Float = 0.78f

    /** One full loop duration (must match MP3 length / generated envelope). */
    val PURR_LOOP_PERIOD_MS: Long get() = Catpur1AudioHapticEnvelope.TOTAL_MS

    /** Burst timings + amplitude envelope (not one flat slab of vibration). See [Catpur1AudioHapticEnvelope]. */
    val loopTimingsMs: LongArray get() = Catpur1AudioHapticEnvelope.timingsMs

    val loopAmplitudesBase: IntArray get() = Catpur1AudioHapticEnvelope.amplitudesBase

    init {
        require(loopTimingsMs.size == loopAmplitudesBase.size)
        require(loopTimingsMs.isNotEmpty())
        require(loopTimingsMs.sum() == Catpur1AudioHapticEnvelope.TOTAL_MS) {
            "Haptic ${loopTimingsMs.sum()} ms != TOTAL_MS=${Catpur1AudioHapticEnvelope.TOTAL_MS}"
        }
        require(loopTimingsMs.sum() == PURR_LOOP_PERIOD_MS)
    }
}
