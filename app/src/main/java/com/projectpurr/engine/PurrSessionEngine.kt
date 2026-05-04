package com.projectpurr.engine

import android.app.Application
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Owns session clock, audio, haptics, sleep timer, and fade-out. UI observes [state] only.
 *
 * Power: long PLAYING sessions apply a mild thermal multiplier ([thermalMultiplierForPlayMinutes]),
 * refreshed on [THERMAL_TICK_MS] ticks so waveform restarts batch with that cadence—not on every frame.
 */
class PurrSessionEngine(
    application: Application,
    private val scope: CoroutineScope,
) {
    private val audio = PurrAudioPlayer(application)
    private val haptics = PurrHapticPlayer(application)

    private val _state = MutableStateFlow(PurrUiState())
    val state: StateFlow<PurrUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var fadeJob: Job? = null
    private var thermalJob: Job? = null

    private var playStartElapsed: Long = 0L

    /** Wall-clock deadline while playing; [Long.MAX_VALUE] when timer off. */
    private var timerDeadlineElapsed: Long = Long.MAX_VALUE

    /** Remaining timer budget when user pauses mid-session. */
    private var timerRemainingMs: Long? = null

    init {
        audio.prepareDefaultHouseCat()
    }

    fun togglePlay() {
        when (_state.value.phase) {
            SessionPhase.STOPPED -> startSession()
            SessionPhase.PLAYING, SessionPhase.FADING -> userStop()
        }
    }

    fun setSilentPurr(enabled: Boolean) {
        _state.update { it.copy(silentPurr = enabled) }
        if (!_state.value.isSessionActive) return
        applyAudioForSilentFlag()
    }

    fun setChestMode(enabled: Boolean) {
        _state.update { it.copy(chestMode = enabled) }
    }

    fun setSleepTimer(option: SleepTimerOption) {
        _state.update { it.copy(sleepTimer = option) }
        if (_state.value.phase == SessionPhase.PLAYING) {
            timerRemainingMs = null
            armSleepTimer()
        }
    }

    fun dispose() {
        timerJob?.cancel()
        fadeJob?.cancel()
        thermalJob?.cancel()
        haptics.stop()
        audio.release()
    }

    private fun startSession() {
        fadeJob?.cancel()
        fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel()
        thermalJob = null

        _state.update { it.copy(phase = SessionPhase.PLAYING) }

        // Same loop period as [HouseCatProfile.PURR_LOOP_PERIOD_MS] / WAV length so haptics and audio re-align each cycle.
        audio.seekToLoopStart()
        applyAudioForSilentFlag()
        haptics.startLoop(HouseCatProfile.loopTimingsMs, HouseCatProfile.loopAmplitudesBase)

        armSleepTimer()
        startThermalWatcher()
    }

    private fun userStop() {
        fadeJob?.cancel()
        fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel()
        thermalJob = null

        captureTimerRemainingIfPlaying()

        if (_state.value.phase == SessionPhase.FADING) {
            haptics.stop()
            audio.pause()
            audio.resetVolumeToTarget()
            haptics.setIntensity(1f)
            _state.update { it.copy(phase = SessionPhase.STOPPED) }
            return
        }

        // Short release curve so vibration and audio don’t “snap” off vs the body.
        _state.update { it.copy(phase = SessionPhase.STOPPED) }
        fadeJob = scope.launch {
            val steps = 10
            val stepMs = 36L
            repeat(steps) { i ->
                if (!isActive) return@launch
                val t = 1f - (i + 1f) / steps
                haptics.setIntensity(t)
                if (!_state.value.silentPurr) {
                    audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                }
                delay(stepMs)
            }
            audio.pause()
            audio.resetVolumeToTarget()
            haptics.stop()
            haptics.setIntensity(1f)
        }
    }

    private fun captureTimerRemainingIfPlaying() {
        if (_state.value.phase != SessionPhase.PLAYING) {
            timerRemainingMs = null
            return
        }
        if (timerDeadlineElapsed == Long.MAX_VALUE) {
            timerRemainingMs = null
            return
        }
        timerRemainingMs = (timerDeadlineElapsed - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
    }

    private fun applyAudioForSilentFlag() {
        if (_state.value.silentPurr) {
            audio.pause()
            audio.setLinearVolume(0f)
        } else {
            audio.resetVolumeToTarget()
            if (_state.value.phase == SessionPhase.PLAYING) {
                audio.start()
            }
        }
    }

    private fun armSleepTimer() {
        timerJob?.cancel()
        val full = _state.value.sleepTimer.durationMillis()
        if (full == null || _state.value.phase != SessionPhase.PLAYING) {
            timerDeadlineElapsed = Long.MAX_VALUE
            timerRemainingMs = null
            return
        }
        val remaining = timerRemainingMs ?: full
        timerRemainingMs = null
        timerDeadlineElapsed = SystemClock.elapsedRealtime() + remaining

        timerJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                if (SystemClock.elapsedRealtime() >= timerDeadlineElapsed) {
                    beginFadeOutFromTimer()
                    break
                }
                delay(200)
            }
        }
    }

    private fun beginFadeOutFromTimer() {
        if (_state.value.phase != SessionPhase.PLAYING) return
        timerJob?.cancel()
        fadeJob?.cancel()
        thermalJob?.cancel()
        thermalJob = null

        _state.update { it.copy(phase = SessionPhase.FADING) }

        fadeJob = scope.launch {
            val steps = 12
            val stepMs = HouseCatProfile.FADE_OUT_MS / steps
            repeat(steps) { i ->
                if (!isActive) return@launch
                val t = 1f - (i + 1f) / steps
                if (!_state.value.silentPurr) {
                    audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                }
                haptics.setIntensity(t)
                delay(stepMs)
            }
            finishAfterFade()
        }
    }

    private fun finishAfterFade() {
        thermalJob?.cancel()
        thermalJob = null
        haptics.stop()
        audio.pause()
        audio.resetVolumeToTarget()
        haptics.setIntensity(1f)
        timerRemainingMs = null
        timerDeadlineElapsed = Long.MAX_VALUE
        _state.update { it.copy(phase = SessionPhase.STOPPED) }
    }

    private fun startThermalWatcher() {
        thermalJob?.cancel()
        playStartElapsed = SystemClock.elapsedRealtime()
        thermalJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                delay(THERMAL_TICK_MS)
                if (_state.value.phase != SessionPhase.PLAYING) break
                val mins = (SystemClock.elapsedRealtime() - playStartElapsed) / 60_000f
                haptics.setSessionThermalMultiplier(thermalMultiplierForPlayMinutes(mins))
            }
        }
    }

    private fun thermalMultiplierForPlayMinutes(minutes: Float): Float {
        if (minutes <= THERMAL_FULL_MINUTES) return 1f
        val over = minutes - THERMAL_FULL_MINUTES
        val t = (over / THERMAL_RAMP_SPAN_MINUTES).coerceIn(0f, 1f)
        return 1f - t * (1f - THERMAL_FLOOR)
    }

    companion object {
        private const val THERMAL_TICK_MS = 45_000L
        private const val THERMAL_FULL_MINUTES = 5f
        private const val THERMAL_RAMP_SPAN_MINUTES = 18f
        private const val THERMAL_FLOOR = 0.88f
    }
}
