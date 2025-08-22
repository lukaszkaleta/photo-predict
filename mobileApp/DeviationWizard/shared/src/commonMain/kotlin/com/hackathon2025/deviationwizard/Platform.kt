package com.hackathon2025.deviationwizard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform