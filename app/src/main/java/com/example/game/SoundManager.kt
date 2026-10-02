package com.example.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var mediaPlayer: MediaPlayer? = null
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    var currentSongIndex: Int = 0
        private set
    var currentSongTitle: String = ""
        private set

    var onSongChangedListener: ((index: Int, title: String) -> Unit)? = null

    // 10 famous complete Ghazals by Kazi Nazrul Islam
    val builtInSongs = listOf(
        Pair(R.raw.song_1, "১. ত্রিভুবনের প্রিয় মুহাম্মদ • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_2, "২. তাওহীদেরই মুর্শিদ আমার • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_3, "৩. মসজিদেরই পাশে আমায় কবর দিও ভাই • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_4, "৪. হেরা হতে হেলে দুলে নূর মদিনায় এলো রে • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_5, "৫. তোরা দেখে যা আমিনা মায়ের কোলে • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_6, "৬. আল্লাহকে যে পাইতে চায় • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_7, "৭. আমি যদি আরব হতাম • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_8, "৮. শোন শোন ইয়া ইলাহী আমার মোনাজাত • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_9, "৯. আল্লাহু আল্লাহু তুমি জাল্লা জালালুহু • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)"),
        Pair(R.raw.song_10, "১০. মনে বড় আশা ছিল যাব মদিনায় • কাজী নজরুল ইসলাম (সম্পূর্ণ গজল)")
    )

    private var activeCustomUriString: String? = null
    private var isPlayingCustom: Boolean = false

    /**
     * Starts continuous music playback.
     * If music is ALREADY PLAYING, it continues seamlessly without restarting!
     */
    fun ensureMusicPlaying(
        startIndex: Int = 0,
        customUriString: String? = null,
        preferCustom: Boolean = false,
        forceSwitch: Boolean = false
    ) {
        if (!isMusicEnabled) return

        // If music is already playing and no forced switch is requested, DO NOT RESTART
        if (mediaPlayer?.isPlaying == true && !forceSwitch) {
            return
        }

        if (preferCustom && !customUriString.isNullOrEmpty()) {
            playCustomUri(customUriString)
        } else {
            playBuiltInSong(startIndex)
        }
    }

    fun playBuiltInSong(index: Int) {
        if (!isMusicEnabled) return
        stopInternalPlayer()

        val safeIndex = (index % builtInSongs.size + builtInSongs.size) % builtInSongs.size
        currentSongIndex = safeIndex
        isPlayingCustom = false
        val (resId, title) = builtInSongs[safeIndex]
        currentSongTitle = title

        try {
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                isLooping = false // Serial playback: play next song when completed
                setOnCompletionListener {
                    playNextSerialSong()
                }
                setVolume(0.9f, 0.9f)
                start()
            }
            onSongChangedListener?.invoke(safeIndex, title)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing built-in song", e)
        }
    }

    fun playNextSerialSong() {
        val nextIndex = (currentSongIndex + 1) % builtInSongs.size
        playBuiltInSong(nextIndex)
    }

    fun playPreviousSerialSong() {
        val prevIndex = if (currentSongIndex > 0) currentSongIndex - 1 else builtInSongs.size - 1
        playBuiltInSong(prevIndex)
    }

    fun playCustomUri(uriString: String) {
        if (!isMusicEnabled) return
        stopInternalPlayer()

        try {
            val uri = Uri.parse(uriString)
            activeCustomUriString = uriString
            isPlayingCustom = true
            currentSongTitle = "ডিভাইসের সংরক্ষিত গান"

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = true
                setVolume(0.9f, 0.9f)
                prepare()
                start()
            }
            onSongChangedListener?.invoke(-1, currentSongTitle)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing custom song, fallback to built-in", e)
            playBuiltInSong(0)
        }
    }

    fun stopMusic() {
        stopInternalPlayer()
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeMusic() {
        if (!isMusicEnabled) return
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            } else if (mediaPlayer == null) {
                ensureMusicPlaying(currentSongIndex, activeCustomUriString, isPlayingCustom)
            }
        } catch (_: Exception) {}
    }

    private fun stopInternalPlayer() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    // --- Clean, peaceful haptic & subtle feedback (No annoying beeps) ---

    fun onCorrectArrowLaunched() {
        if (!isSoundEnabled) return
        triggerHaptic(40)
    }

    fun onWrongArrowBlocked() {
        if (!isSoundEnabled) return
        // Stronger vibration alert for wrong tap, music remains uninterrupted
        triggerHaptic(120)
    }

    fun onLevelWin() {
        if (!isSoundEnabled) return
        triggerHaptic(90)
    }

    fun playClick() {
        if (!isSoundEnabled) return
        triggerHaptic(25)
    }

    private fun triggerHaptic(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
