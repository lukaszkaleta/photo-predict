package com.hackathon2025.deviationwizard

import android.os.Build

class AndroidDeviceInfo : DeviceInfo {
    override val isEmulator: Boolean
        get() {
            return (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || "google_sdk" == Build.PRODUCT
                    || Build.HARDWARE.contains("goldfish")
                    || Build.HARDWARE.contains("ranchu")
                    || Build.HARDWARE.contains("vbox86")
                    || Build.HARDWARE.contains("vbox")
                    || Build.HARDWARE.contains("qemu")
                    || Build.HARDWARE.contains("emulator")
                    || Build.PRODUCT.contains("sdk_gphone")
                    || Build.PRODUCT.contains("google_sdk")
                    || Build.PRODUCT.contains("sdk")
                    || Build.PRODUCT.contains("sdk_x86")
                    || Build.PRODUCT.contains("vbox86p")
                    || Build.PRODUCT.contains("emulator")
                    || Build.PRODUCT.contains("simulator"))
        }
}

actual fun getDeviceInfo(): DeviceInfo = AndroidDeviceInfo() 