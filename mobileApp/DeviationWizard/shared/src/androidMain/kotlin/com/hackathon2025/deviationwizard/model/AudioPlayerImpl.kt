package com.hackathon2025.deviationwizard.model

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

class AudioPlayerImpl(private val context: Context) : AudioPlayer {
    private var player: MediaPlayer? = null
    private var onPlaybackComplete: (() -> Unit)? = null

    fun setOnPlaybackCompleteListener(listener: () -> Unit) {
        onPlaybackComplete = listener
    }

    override fun startPlaying(file: File) {
        try {
            player?.release()
            player = MediaPlayer(context).apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    onPlaybackComplete?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio", e)
            player?.release()
            player = null
        }
    }

    override fun stopPlaying() {
        player?.stop()
        player?.release()
        player = null
    }
}