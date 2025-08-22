package com.hackathon2025.deviationwizard

interface DeviceInfo {
    val isEmulator: Boolean
}

expect fun getDeviceInfo(): DeviceInfo 