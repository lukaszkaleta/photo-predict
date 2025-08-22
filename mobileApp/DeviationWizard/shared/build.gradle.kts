import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
    kotlin("plugin.serialization") version "2.1.20"
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.precompose)

            // Compose Material dependency (Material 3)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.runtime)

            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.compose)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.accompanist.permissions)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.material.icons.extended)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.compose)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.accompanist.permissions)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.hackathon2025.deviationwizard"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        ndk {
            debugSymbolLevel = "FULL"
        }
    }
    buildTypes {
        release {
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.media3.common.ktx)
}
