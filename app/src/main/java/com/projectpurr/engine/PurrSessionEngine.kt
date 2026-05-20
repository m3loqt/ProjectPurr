package com.projectpurr.engine

import android.app.Application
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
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
 * Owns session clock, audio, haptics, sleep timer, fade-out, and media session. UI observes [state] only.
 *
 * Session lifecycle:
 *   STOPPED → [startSession] → PLAYING → [userStop] → STOPPED  (360 ms release ramp)
 *   PLAYING → [beginFadeOutFromTimer] → FADING → [finishAfterFade] → STOPPED
 */
class PurrSessionEngine(
    application: Application,
    private val scope: CoroutineScope,
    private val onSessionCompleted: ((
        startedAtMs: Long,
        endedAtMs: Long,
        durationMs: Long,
        usedSilent: Boolean,
        usedChest: Boolean,
        timerOptionMinutes: Int?,
        completedNaturally: Boolean,
    ) -> Unit)? = null,
) {
    private val wakeLock: PowerManager.WakeLock =
        (application.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProjectPurr:PurrSession")
            .apply { setReferenceCounted(false) }

    private val audio   = PurrAudioPlayer(application)
    private val haptics = PurrHapticPlayer(application, scope)

    private val mediaSession = MediaSession(application, "${application.packageName}.PurrSession").apply {
        setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE,  "House Cat")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Purr")
                .build(),
        )
        setCallback(object : MediaSession.Callback() {
            override fun onPlay()  { scope.launch { if (_state.value.phase == SessionPhase.STOPPED) startSession() } }
            override fun onPause() { scope.launch { if (_state.value.isSessionActive) userStop() } }
            override fun onStop()  { scope.launch { if (_state.value.isSessionActive) userStop() } }
        })
    }

    private val _state = MutableStateFlow(PurrUiState())
    val state: StateFlow<PurrUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var fadeJob: Job? = null
    private var thermalJob: Job? = null
    private var loopSyncJob: Job? = null
    private var waveformSyncJob: Job? = null
    private var startupRampJob: Job? = null

    private var playStartElapsed: Long = 0L
    private var sessionAnchorElapsed: Long = 0L

    private var sessionStartWallMs: Long = 0L
    private var sessionSaved: Boolean = false

    private var focusInterrupted = false

    /** Wall-clock deadline while playing; [Long.MAX_VALUE] when timer is off. */
    private var timerDeadlineElapsed: Long = Long.MAX_VALUE

    /** Remaining timer budget captured when the user pauses mid-session. */
    private var timerRemainingMs: Long? = null

    init {
        audio.prepareDefaultHouseCat()
        updateMediaSession()
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

    fun setForceSpeaker(enabled: Boolean) {
        _state.update { it.copy(forceSpeaker = enabled) }
        if (_state.value.isSessionActive) audio.setForceSpeaker(enabled)
    }

    fun setSleepTimer(option: SleepTimerOption) {
        _state.update { it.copy(sleepTimer = option) }
        if (_state.value.phase == SessionPhase.PLAYING) {
            timerRemainingMs = null
            armSleepTimer()
        }
    }

    fun previewHaptic() = haptics.playPreview()

    fun dispose() {
        startupRampJob?.cancel()
        loopSyncJob?.cancel()
        waveformSyncJob?.cancel()
        timerJob?.cancel()
        fadeJob?.cancel()
        thermalJob?.cancel()
        releaseWakeLock()
        haptics.stop()
        audio.release()
        mediaSession.release()
    }

    // ── Session lifecycle ──────────────────────────────────────────────────────

    private fun startSession() {
        startupRampJob?.cancel(); startupRampJob = null
        loopSyncJob?.cancel(); loopSyncJob = null
        waveformSyncJob?.cancel(); waveformSyncJob = null
        fadeJob?.cancel(); fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel(); thermalJob = null

        _state.update { it.copy(phase = SessionPhase.PLAYING, loopPositionMs = 0L, sensoryIntensity = 0f) }
        updateMediaSession()
        log("Session started — silentPurr=${_state.value.silentPurr} sleepTimer=${_state.value.sleepTimer}")
        acquireWakeLock()
        sessionStartWallMs = System.currentTimeMillis()
        sessionSaved = false

        sessionAnchorElapsed = SystemClock.elapsedRealtime()

        audio.seekToLoopStart()
        audio.setLinearVolume(0f)
        if (!_state.value.silentPurr) audio.start()
        audio.setForceSpeaker(_state.value.forceSpeaker)

        haptics.startLoop(HouseCatProfile.loopTimingsMs, HouseCatProfile.loopAmplitudesBase)

        audio.setOnLoopComplete {
            scope.launch { onAudioLoopComplete() }
        }

        if (_state.value.silentPurr) startSilentPurrLoopSync()

        focusInterrupted = false
        audio.requestAudioFocus(
            onFocusLost   = { scope.launch { handleFocusLost() } },
            onFocusGained = { scope.launch { handleFocusGained() } },
        )

        armSleepTimer()
        startThermalWatcher()
        startWaveformSync()

        startupRampJob = scope.launch {
            val steps  = 6
            val stepMs = STARTUP_FADE_IN_MS / steps
            for (i in 1..steps) {
                if (!isActive || _state.value.phase != SessionPhase.PLAYING) return@launch
                val t = i.toFloat() / steps
                applySensoryIntensity(t)
                if (!_state.value.silentPurr) audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                delay(stepMs)
            }
            applySensoryIntensity(1f)
            if (!_state.value.silentPurr) audio.resetVolumeToTarget()
            haptics.restartLoop()
            startupRampJob = null
        }
    }

    private fun startWaveformSync() {
        waveformSyncJob?.cancel()
        waveformSyncJob = scope.launch {
            while (isActive && _state.value.isSessionActive) {
                val pos = currentLoopPositionMs()
                _state.update {
                    it.copy(
                        loopPositionMs = pos,
                        sensoryIntensity = haptics.outputScale(),
                    )
                }
                delay(WAVEFORM_TICK_MS)
            }
        }
    }

    private fun currentLoopPositionMs(): Long {
        val period = PurrEnvelopeSampler.loopPeriodMs
        return if (_state.value.silentPurr || !audio.isPlaying()) {
            val elapsed = SystemClock.elapsedRealtime() - sessionAnchorElapsed
            ((elapsed % period) + period) % period
        } else {
            audio.currentPositionMs().let { if (period > 0) it % period else it }
        }
    }

    private fun onAudioLoopComplete() {
        if (_state.value.phase != SessionPhase.PLAYING) return
        log("Audio loop complete — restarting both clocks")
        audio.seekToLoopStart()
        if (!_state.value.silentPurr) audio.start()
        haptics.restartLoop()
        sessionAnchorElapsed = SystemClock.elapsedRealtime()
    }

    private fun startSilentPurrLoopSync() {
        loopSyncJob?.cancel()
        sessionAnchorElapsed = SystemClock.elapsedRealtime()
        log("Silent purr loop sync started")
        loopSyncJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                val elapsed     = SystemClock.elapsedRealtime() - sessionAnchorElapsed
                val nextBoundary = (elapsed / HouseCatProfile.PURR_LOOP_PERIOD_MS + 1) * HouseCatProfile.PURR_LOOP_PERIOD_MS
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
        startupRampJob?.cancel(); startupRampJob = null
        audio.pause()
        haptics.pause()
    }

    private fun handleFocusGained() {
        if (!focusInterrupted || _state.value.phase != SessionPhase.PLAYING) return
        log("Audio focus gained — resuming session")
        focusInterrupted = false
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
        waveformSyncJob?.cancel(); waveformSyncJob = null
        fadeJob?.cancel(); fadeJob = null
        timerJob?.cancel()
        thermalJob?.cancel(); thermalJob = null

        captureTimerRemainingIfPlaying()

        if (_state.value.phase == SessionPhase.FADING) {
            haptics.stop()
            audio.pause()
            audio.resetVolumeToTarget()
            applySensoryIntensity(1f)
            _state.update {
                it.copy(phase = SessionPhase.STOPPED, timerRemainingMs = null, loopPositionMs = 0L, sensoryIntensity = 1f)
            }
            updateMediaSession()
            return
        }

        _state.update { it.copy(phase = SessionPhase.FADING, timerRemainingMs = null) }
        updateMediaSession()
        fadeJob = scope.launch {
            val steps  = 10
            val stepMs = 36L
            repeat(steps) { i ->
                if (!isActive) return@launch
                val t = 1f - (i + 1f) / steps
                applySensoryIntensity(t)
                if (!_state.value.silentPurr) {
                    audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                }
                delay(stepMs)
            }
            audio.pause()
            audio.resetVolumeToTarget()
            haptics.stop()
            applySensoryIntensity(1f)
            notifySessionCompleted(completedNaturally = false)
            _state.update {
                it.copy(phase = SessionPhase.STOPPED, loopPositionMs = 0L, sensoryIntensity = 1f)
            }
            updateMediaSession()
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
        startupRampJob?.cancel(); startupRampJob = null

        if (_state.value.silentPurr) {
            audio.pause()
            audio.setLinearVolume(0f)
            if (_state.value.phase == SessionPhase.PLAYING) {
                haptics.restartLoop()
                startSilentPurrLoopSync()
            }
        } else {
            loopSyncJob?.cancel(); loopSyncJob = null
            audio.resetVolumeToTarget()
            if (_state.value.phase == SessionPhase.PLAYING) {
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
            timerRemainingMs     = null
            _state.update { it.copy(timerRemainingMs = null) }
            return
        }
        val remaining = timerRemainingMs ?: full
        timerRemainingMs     = null
        timerDeadlineElapsed = SystemClock.elapsedRealtime() + remaining
        log("Sleep timer armed — ${remaining / 1000}s remaining")

        timerJob = scope.launch {
            var lastDisplayedSec = -1L
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                val now = SystemClock.elapsedRealtime()
                if (now >= timerDeadlineElapsed) {
                    _state.update { it.copy(timerRemainingMs = null) }
                    beginFadeOutFromTimer()
                    break
                }
                val rem = timerDeadlineElapsed - now
                val sec = rem / 1000
                if (sec != lastDisplayedSec) {
                    lastDisplayedSec = sec
                    _state.update { it.copy(timerRemainingMs = rem) }
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

        _state.update { it.copy(phase = SessionPhase.FADING, timerRemainingMs = null) }
        updateMediaSession()
        startWaveformSync()

        fadeJob = scope.launch {
            val steps  = 12
            val stepMs = HouseCatProfile.FADE_OUT_MS / steps
            repeat(steps) { i ->
                if (!isActive) return@launch
                val t = 1f - (i + 1f) / steps
                if (!_state.value.silentPurr) {
                    audio.setLinearVolume(t * HouseCatProfile.AUDIO_TARGET_VOLUME)
                }
                applySensoryIntensity(t)
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
        applySensoryIntensity(1f)
        timerRemainingMs     = null
        timerDeadlineElapsed = Long.MAX_VALUE
        waveformSyncJob?.cancel(); waveformSyncJob = null
        notifySessionCompleted(completedNaturally = true)
        _state.update {
            it.copy(phase = SessionPhase.STOPPED, timerRemainingMs = null, loopPositionMs = 0L, sensoryIntensity = 1f)
        }
        updateMediaSession()
    }

    private fun notifySessionCompleted(completedNaturally: Boolean) {
        if (sessionSaved) return
        val endMs = System.currentTimeMillis()
        val durationMs = endMs - sessionStartWallMs
        if (durationMs < MIN_SAVE_DURATION_MS) return
        sessionSaved = true
        val s = _state.value
        onSessionCompleted?.invoke(
            sessionStartWallMs,
            endMs,
            durationMs,
            s.silentPurr,
            s.chestMode,
            s.sleepTimer.durationMinutes(),
            completedNaturally,
        )
    }

    private fun applySensoryIntensity(scale: Float) {
        haptics.setIntensity(scale)
        _state.update { it.copy(sensoryIntensity = haptics.outputScale()) }
    }

    // ── Thermal management ─────────────────────────────────────────────────────

    private fun startThermalWatcher() {
        thermalJob?.cancel()
        playStartElapsed = SystemClock.elapsedRealtime()
        thermalJob = scope.launch {
            while (isActive && _state.value.phase == SessionPhase.PLAYING) {
                delay(THERMAL_TICK_MS)
                if (_state.value.phase != SessionPhase.PLAYING) break
                val mins       = (SystemClock.elapsedRealtime() - playStartElapsed) / 60_000f
                val multiplier = thermalMultiplierForPlayMinutes(mins)
                if (multiplier < 1f) log("Thermal taper active — ${mins.toInt()} min played, multiplier=${"%.3f".format(multiplier)}")
                haptics.queueSessionThermalMultiplier(multiplier)
            }
        }
    }

    private fun thermalMultiplierForPlayMinutes(minutes: Float): Float {
        if (minutes <= THERMAL_FULL_MINUTES) return 1f
        val over = minutes - THERMAL_FULL_MINUTES
        val t    = (over / THERMAL_RAMP_SPAN_MINUTES).coerceIn(0f, 1f)
        return 1f - t * (1f - THERMAL_FLOOR)
    }

    // ── Media session ──────────────────────────────────────────────────────────

    private fun updateMediaSession() {
        val phase = _state.value.phase
        val pbState = when (phase) {
            SessionPhase.PLAYING, SessionPhase.FADING -> PlaybackState.STATE_PLAYING
            SessionPhase.STOPPED                      -> PlaybackState.STATE_STOPPED
        }
        val actions = if (phase == SessionPhase.STOPPED) {
            PlaybackState.ACTION_PLAY
        } else {
            PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP
        }
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(pbState, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build(),
        )
        mediaSession.isActive = phase != SessionPhase.STOPPED
    }

    // ── Logging / wakelock ─────────────────────────────────────────────────────

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

        private const val MIN_SAVE_DURATION_MS = 60_000L
        private const val STARTUP_FADE_IN_MS = 600L
        private const val WAVEFORM_TICK_MS   = 33L

        private const val THERMAL_TICK_MS          = 45_000L
        private const val THERMAL_FULL_MINUTES      = 5f
        private const val THERMAL_RAMP_SPAN_MINUTES = 18f
        private const val THERMAL_FLOOR             = 0.88f
    }
}
