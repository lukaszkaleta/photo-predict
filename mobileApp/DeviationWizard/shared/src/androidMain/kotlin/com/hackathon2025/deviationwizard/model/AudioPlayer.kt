package com.hackathon2025.deviationwizard.model

import java.io.File

interface AudioPlayer {
    fun startPlaying(file: File)
    fun stopPlaying()
}