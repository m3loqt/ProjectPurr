package com.projectpurr.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.projectpurr.R

/**
 * Looped purr audio: [R.raw.catpur1] (MP3). Duration must match
 * [HouseCatProfile.PURR_LOOP_PERIOD_MS] for haptic sync.
 *
 * AudioFocus: call [requestAudioFocus] when the session starts and [abandonAudioFocus] when it
 * stops. The caller-supplied callbacks fire on AudioManager's handler thread — dispatch to the
 * engine's coroutine scope before touching state.
 */
class PurrAudioPlayer(context: Context) {
    private val app = context.applicationContext
    private var player: MediaPlayer? = null

    private val audioManager = app.getSystemService(AudioManager::class.java)
    private var focusRequest: AudioFocusRequest? = null

    fun prepareDefaultHouseCat() {
        if (player != null) return
        val afd = app.resources.openRawResourceFd(R.raw.catpur1) ?: return
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = false   // Engine drives restarts so audio + haptic stay in sync.
                prepare()
                setVolume(HouseCatProfile.AUDIO_TARGET_VOLUME, HouseCatProfile.AUDIO_TARGET_VOLUME)
            }
        } finally {
            afd.close()
        }
    }

    /**
     * Registers a callback fired each time the audio reaches its natural end.
     * The engine uses this to restart both audio and haptic together, keeping them in phase.
     */
    fun setOnLoopComplete(callback: () -> Unit) {
        player?.setOnCompletionListener { callback() }
    }

    /** Aligns playback to position 0 so haptic and audio loops share the same phase. */
    fun seekToLoopStart() {
        player?.seekTo(0)
    }

    fun start() {
        player?.start()
    }

    fun pause() {
        player?.pause()
    }

    fun setLinearVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        player?.setVolume(v, v)
    }

    fun resetVolumeToTarget() {
        setLinearVolume(HouseCatProfile.AUDIO_TARGET_VOLUME)
    }

    /**
     * Routes playback to the built-in speaker, bypassing Bluetooth or wired headphones.
     * Useful when the user wants the vibration felt on the chest but headphones are connected.
     * API 28+: uses preferred device. API 26–27: best-effort via speakerphone flag.
     */
    fun setForceSpeaker(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (enabled) {
                val speaker = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                player?.setPreferredDevice(speaker)
            } else {
                player?.setPreferredDevice(null)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = enabled
        }
    }

    // ── AudioFocus ─────────────────────────────────────────────────────────────

    /**
     * Requests audio focus for media playback.
     * [onFocusLost] fires when a phone call or other app takes focus.
     * [onFocusGained] fires when focus returns after a transient interruption.
     * Both callbacks arrive on AudioManager's handler thread — dispatch before touching UI state.
     */
    fun requestAudioFocus(onFocusLost: () -> Unit, onFocusGained: () -> Unit) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val listener = AudioManager.OnAudioFocusChangeListener { change ->
            Log.d("PurrAudio", "AudioFocus change: $change")
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onFocusLost()
                AudioManager.AUDIOFOCUS_GAIN                     -> onFocusGained()
            }
        }

        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener(listener)
            .build()

        focusRequest = req
        audioManager.requestAudioFocus(req)
    }

    fun abandonAudioFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    /** Reported loop length (ms). Compare to [HouseCatProfile.PURR_LOOP_PERIOD_MS] when tuning. */
    fun loopDurationMs(): Int = player?.duration?.takeIf { it > 0 } ?: -1

    /** Current position in the active loop (ms). Used to sync UI waveform with audio. */
    fun currentPositionMs(): Long = player?.currentPosition?.toLong()?.coerceAtLeast(0L) ?: 0L

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun release() {
        abandonAudioFocus()
        // Clear listener before release so stale loop-complete callbacks can't fire after cleanup.
        player?.setOnCompletionListener(null)
        player?.release()
        player = null
    }
}
