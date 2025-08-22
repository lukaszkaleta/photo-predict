package com.hackathon2025.deviationwizard.model

import java.io.File

interface AudioRecorder {
    fun startRecording(output: File)
    fun stopRecording()
}