package com.hackathon2025.deviationwizard.api

object ApiConfig {
    private var _baseUrl = GOOGLE_CLOUD_URL
    val baseUrl: String get() = _baseUrl

    private var _apiKey = DEFAULT_API_KEY
    val apiKey: String get() = _apiKey

    fun setBaseUrl(url: String) {
        _baseUrl = url
    }

    fun setApiKey(key: String) {
        _apiKey = key
    }

    const val GOOGLE_CLOUD_URL = "https://hackathon2025api-506638310413.europe-west1.run.app"
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080"
    const val DEFAULT_API_KEY = "550e8400-e29b-41d4-a716-446655440000"
}