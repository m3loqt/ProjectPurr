package com.projectpurr.engine

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
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
 * Session lifecycle:
 *   STOPPED → [startSession] → PLAYING → [userStop] → STOPPED  (360 ms release ramp)
 *   PLAYING → [beginFadeOutFromTimer] → FADING → [finishAfterFade] → STOPPED
 *
 * Power: long PLAYING sessions apply a mild thermal multiplier, refreshed on [THERMAL_TICK_MS]
 * boundaries so waveform restarts batch with that cadence — not on every frame.
 */
class PurrSessionEngine(
    application: Application,
    private val scope: CoroutineScope,
) {
    private val wakeLock: PowerManager.WakeLock =
        (application.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProjectPurr:PurrSession")
            .apply { setReferenceCounted(false) }

    private val audio = PurrAudioPlayer(application)
    private val haptics = PurrHapticPlayer(application)

    private val _state = MutableStateFlow(PurrUiState())
    val state: StateFlow<PurrUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var fadeJob: Job? = null
    private var thermalJob: Job? = null
    private var loopSyncJob: Job? = null
    private var startupRampJob: Job? = null

    private var playStartElapsed: Long = 0L
    private var sessionAnchorElapsed: Long = 0L

    /** True when audio focus was lost mid-session so we can auto-resume on GAIN. */
    private var focusInterrupted = false

    /** Wall-clock deadline while playing; [Long.MAX_VALUE] when timer is off. */
    private var timerDeadlineElapsed: Long = Long.MAX_VALUE

    /** Remaining timer budget captured when the user pauses mid-session. */
    private var timerRemainingMs: Long? = null

    init {
        audio.prepareDefaultHouseCat()
        log("Engine initialized — hasAmplitudeControl=${haptics.hasAmplitudeControl}")
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
        log("Silent purr set to $enabled")
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
        startupRampJob?.cancel()
        loopSyncJob?.cancel()
        timerJob?.cancel()
        fadeJob?.cancel()
        thermalJob?.cancel()
        releaseWakeLock()
        haptics.stop()
        audio.release()
    }

    // ── Session lifecycle ──────────────────────────────────────────────────────

    private fun startSession() {
        // Cancel any jobs left over from a previous or interrupted session.
        startupRampJob?.cancel(); startupRampJob = null
        loopSyncJob?.cancel(); loopSyncJob = null
        fadeJob?.cancel(); fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel(); thermalJob = null

        _state.update { it.copy(phase = SessionPhase.PLAYING) }
        log("Session started — silentPurr=${_state.value.silentPurr} sleepTimer=${_state.value.sleepTimer}")
        acquireWakeLock()

        // Anchor both clocks at the same instant so haptic and audio stay phase-locked.
        sessionAnchorElapsed = SystemClock.elapsedRealtime()

        // Prepare audio at 0 volume; fade-in ramp below brings it up.
        audio.seekToLoopStart()
        audio.setLinearVolume(0f)
        if (!_state.value.silentPurr) audio.start()

        // Start haptics at 0 intensity; fade-in ramp will bring it up.
        haptics.startLoop(HouseCatProfile.loopTimingsMs, HouseCatProfile.loopAmplitudesBase)
        haptics.setIntensity(0f)

        // Audio loop completion drives haptic restart in non-silent mode.
        audio.setOnLoopComplete {
            scope.launch { onAudioLoopComplete() }
        }

        if (_state.value.silentPurr) startSilentPurrLoopSync()

        focusInterrupted = false
        audio.requestAudioFocus(
            onFocusLost  = { scope.launch { handleFocusLost() } },
            onFocusGained = { scope.launch { handleFocusGained() } },
        )

        armSleepTimer()
        startThermalWatcher()

        // Gentle fade-in: ramp from silent to full over STARTUP_FADE_IN_MS.
        // Haptics restart the waveform each step — acceptable at this cadence (~100 ms/step).
        startupRampJob = scope.launch {
            val steps = 6
            val stepMs = STARTUP_FADE_IN_MS / steps
            for (i in 1..steps) {
                if (!isActive || _state.value.phase != SessionPhase.PLAYING) return@launch
                val t = i.toFloat() / steps
                haptics.setIntensity(t)
                if (!_state.value.silentPurr) audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                delay(stepMs)
            }
            haptics.setIntensity(1f)
            if (!_state.value.silentPurr) audio.resetVolumeToTarget()
            startupRampJob = null
        }
    }

    /**
     * Fires at the natural end of each audio loop. Restarts audio and haptic together
     * so the fade encoded at the end of the waveform always lines up with the audio fade.
     *
     * In Silent Purr mode the audio is paused so this callback never fires —
     * [startSilentPurrLoopSync] drives haptic restarts instead.
     */
    private fun onAudioLoopComplete() {
        if (_state.value.phase != SessionPhase.PLAYING) return
        log("Audio loop complete — restarting both clocks")
        audio.seekToLoopStart()
        if (!_state.value.silentPurr) audio.start()
        haptics.restartLoop()
        sessionAnchorElapsed = SystemClock.elapsedRealtime()
    }

    /** Fallback loop sync for Silent Purr mode (audio is paused so no completion callbacks fire). */
    private fun startSilentPurrLoopSync() {
        loopSyncJob?.cancel()
        sessionAnchorElapsed = SystemClock.elapsedRealtime()
        log("Silent purr loop sync started")
        loopSyncJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                val elapsed = SystemClock.elapsedRealtime() - sessionAnchorElapsed
                val nextBoundary = (elapsed / HouseCatProfile.PURR_LOOP_PERIOD_MS + 1) * HouseCatProfile.PURR_LOOP_PERIOD_MS
                // coerceAtLeast(0): defensive against scheduling slippage past the boundary
                delay((nextBoundary - elapsed).coerceAtLeast(0L))
                if (isActive && _state.value.phase == SessionPhase.PLAYING && _state.value.silentPurr) {
                    log("Silent loop boundary — restarting haptic")
                    haptics.restartLoop()
                }
            }
        }
    }

    // ── Audio focus ────────────────────────────────────────────────────────────

    private fun handleFocusLost() {
        if (_state.value.phase != SessionPhase.PLAYING) return
        log("Audio focus lost — pausing session")
        focusInterrupted = true
        // Cancel startup ramp so it doesn't restart the vibrator while we're paused.
        startupRampJob?.cancel(); startupRampJob = null
        audio.pause()
        // Use pause() (not stop()) so loopTimings are preserved and restartLoop() works on regain.
        haptics.pause()
    }

    private fun handleFocusGained() {
        if (!focusInterrupted || _state.value.phase != SessionPhase.PLAYING) return
        log("Audio focus gained — resuming session")
        focusInterrupted = false
        // Cancel any stale startup ramp; session resumes at full intensity.
        startupRampJob?.cancel(); startupRampJob = null
        haptics.restartLoop()
        sessionAnchorElapsed = SystemClock.elapsedRealtime()
        if (!_state.value.silentPurr) {
            audio.seekToLoopStart()
            audio.start()
        }
    }

    // ── Stop / fade ────────────────────────────────────────────────────────────

    private fun userStop() {
        log("User stopped session (phase=${_state.value.phase})")
        focusInterrupted = false
        releaseWakeLock()
        audio.abandonAudioFocus()
        startupRampJob?.cancel(); startupRampJob = null
        loopSyncJob?.cancel(); loopSyncJob = null
        fadeJob?.cancel(); fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel(); thermalJob = null

        captureTimerRemainingIfPlaying()

        if (_state.value.phase == SessionPhase.FADING) {
            // Interrupting a timer-triggered fade: stop immediately.
            haptics.stop()
            audio.pause()
            audio.resetVolumeToTarget()
            haptics.setIntensity(1f)
            _state.update { it.copy(phase = SessionPhase.STOPPED) }
            return
        }

        // Short release ramp so vibration and audio don't "snap" off against the body.
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
        // Startup ramp may have set volume/intensity mid-ramp; cancel it so the mode
        // switch logic below takes clean ownership of audio/haptic state.
        startupRampJob?.cancel(); startupRampJob = null

        if (_state.value.silentPurr) {
            audio.pause()
            audio.setLinearVolume(0f)
            // Audio completion callbacks won't fire while paused; use timer-based haptic sync.
            if (_state.value.phase == SessionPhase.PLAYING) startSilentPurrLoopSync()
        } else {
            loopSyncJob?.cancel(); loopSyncJob = null
            audio.resetVolumeToTarget()
            if (_state.value.phase == SessionPhase.PLAYING) {
                // Re-align both clocks: audio was paused at an unknown position, haptic was
                // looping on the silent-purr timer. Restart both from position 0 for immediate sync.
                haptics.restartLoop()
                audio.seekToLoopStart()
                audio.start()
                sessionAnchorElapsed = SystemClock.elapsedRealtime()
            }
        }
    }

    // ── Sleep timer ────────────────────────────────────────────────────────────

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
        log("Sleep timer armed — ${remaining / 1000}s remaining")

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
        log("Sleep timer expired — beginning fade-out")
        startupRampJob?.cancel(); startupRampJob = null
        loopSyncJob?.cancel(); loopSyncJob = null
        timerJob?.cancel()
        fadeJob?.cancel()
        thermalJob?.cancel(); thermalJob = null

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
        log("Fade-out complete — session stopped")
        thermalJob?.cancel(); thermalJob = null
        releaseWakeLock()
        haptics.stop()
        audio.pause()
        audio.resetVolumeToTarget()
        haptics.setIntensity(1f)
        timerRemainingMs = null
        timerDeadlineElapsed = Long.MAX_VALUE
        _state.update { it.copy(phase = SessionPhase.STOPPED) }
    }

    // ── Thermal management ─────────────────────────────────────────────────────

    private fun startThermalWatcher() {
        thermalJob?.cancel()
        playStartElapsed = SystemClock.elapsedRealtime()
        thermalJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                delay(THERMAL_TICK_MS)
                if (_state.value.phase != SessionPhase.PLAYING) break
                val mins = (SystemClock.elapsedRealtime() - playStartElapsed) / 60_000f
                val multiplier = thermalMultiplierForPlayMinutes(mins)
                if (multiplier < 1f) log("Thermal taper active — ${mins.toInt()} min played, multiplier=${"%.3f".format(multiplier)}")
                // Queued (not mid-waveform) — applied on the next natural loop boundary.
                haptics.queueSessionThermalMultiplier(multiplier)
            }
        }
    }

    private fun thermalMultiplierForPlayMinutes(minutes: Float): Float {
        if (minutes <= THERMAL_FULL_MINUTES) return 1f
        val over = minutes - THERMAL_FULL_MINUTES
        val t = (over / THERMAL_RAMP_SPAN_MINUTES).coerceIn(0f, 1f)
        return 1f - t * (1f - THERMAL_FLOOR)
    }

    // ── Logging ────────────────────────────────────────────────────────────────

    private fun log(msg: String) = Log.d(TAG, msg)

    private fun acquireWakeLock() {
        if (wakeLock.isHeld) return
        wakeLock.acquire(WAKELOCK_TIMEOUT_MS)
        log("Wake lock acquired")
    }

    private fun releaseWakeLock() {
        if (!wakeLock.isHeld) return
        wakeLock.release()
        log("Wake lock released")
    }

    companion object {
        private const val TAG = "PurrEngine"
        private const val WAKELOCK_TIMEOUT_MS = 35L * 60L * 1000L

        private const val STARTUP_FADE_IN_MS = 600L

        private const val THERMAL_TICK_MS = 45_000L
        private const val THERMAL_FULL_MINUTES = 5f
        private const val THERMAL_RAMP_SPAN_MINUTES = 18f
        private const val THERMAL_FLOOR = 0.88f
    }
}
