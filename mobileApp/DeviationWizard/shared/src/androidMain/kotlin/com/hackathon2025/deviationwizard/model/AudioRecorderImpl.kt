package com.hackathon2025.deviationwizard.model

import android.content.Context
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream

class AudioRecorderImpl(private val context: Context): AudioRecorder {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return recorder ?: MediaRecorder(context)
    }

    override fun startRecording(output: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.OGG)
            setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(FileOutputStream(output).fd)
            prepare()
            start()

            recorder = this
        }
    }

    override fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}