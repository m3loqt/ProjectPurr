package com.projectpurr.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.projectpurr.R

/**
 * Looped purr audio: [R.raw.catpur1] (MP3). Duration must match [HouseCatProfile.PURR_LOOP_PERIOD_MS] for haptic sync.
 */
class PurrAudioPlayer(context: Context) {
    private val app = context.applicationContext
    private var player: MediaPlayer? = null

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
                isLooping = true
                prepare()
                setVolume(HouseCatProfile.AUDIO_TARGET_VOLUME, HouseCatProfile.AUDIO_TARGET_VOLUME)
            }
        } finally {
            afd.close()
        }
    }

    /** Reported loop length (ms). Compare to [HouseCatProfile.PURR_LOOP_PERIOD_MS] when tuning a new asset. */
    fun loopDurationMs(): Int = player?.duration?.takeIf { it > 0 } ?: -1

    /** Aligns playback phase with the haptic loop (call before [start] each session). */
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

    fun release() {
        player?.release()
        player = null
    }
}
