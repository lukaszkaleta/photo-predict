package com.hackathon2025.deviationwizard

import platform.UIKit.UIDevice

class IOSDeviceInfo : DeviceInfo {
    override val isEmulator: Boolean
        get() {
            return UIDevice.currentDevice.name.contains("Simulator") ||
                   UIDevice.currentDevice.model.contains("Simulator") ||
                   UIDevice.currentDevice.systemName.contains("Simulator")
        }
}

actual fun getDeviceInfo(): DeviceInfo = IOSDeviceInfo() 