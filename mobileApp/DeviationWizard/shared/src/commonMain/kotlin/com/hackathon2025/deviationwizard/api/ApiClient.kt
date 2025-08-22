package com.hackathon2025.deviationwizard.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DeviationRequest(
    val images: List<String>, // Base64 encoded images
    val recordings: List<String>, // Base64 encoded recordings
    val comment: String
)

@Serializable
data class Deviation(
    val id: String,
    val timestamp: String,
    val images: List<String>, // Internal IDs of images
    val recordings: List<String>, // Internal IDs of recordings
    val comment: String,
)

class ApiClient(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    suspend fun uploadDeviation(request: DeviationRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "$baseUrl/api/deviations"
            println("Attempting to upload deviation to endpoint: $endpoint")
            println("Request body: ${Json.encodeToString(DeviationRequest.serializer(), request)}")
            
            val response = client.post(endpoint) {
                setBody(request)
                contentType(ContentType.Application.Json)
                header("X-API-Key", ApiConfig.apiKey)
            }
            
            when (response.status.value) {
                in 200..299 -> {
                    println("Deviation upload successful")
                    Result.success(response.bodyAsText())
                }
                401 -> {
                    println("Deviation upload failed: Unauthorized - Invalid or missing API key")
                    Result.failure(Exception("Authentication failed. Please check your API key in the settings."))
                }
                403 -> {
                    println("Deviation upload failed: Forbidden - Insufficient permissions")
                    Result.failure(Exception("Access denied. You don't have permission to perform this action."))
                }
                404 -> {
                    println("Deviation upload failed: Endpoint not found - $endpoint")
                    Result.failure(Exception("Server endpoint not found. Please check your server configuration."))
                }
                in 500..599 -> {
                    println("Deviation upload failed: Server error (${response.status.value})")
                    Result.failure(Exception("Server error occurred. Please try again later."))
                }
                else -> {
                    println("Deviation upload failed with status: ${response.status}")
                    Result.failure(Exception("Failed to upload deviation: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("Deviation upload error: ${e.message}")
            when (e) {
                is io.ktor.client.plugins.ClientRequestException -> {
                    when (e.response.status.value) {
                        401 -> Result.failure(Exception("Authentication failed. Please check your API key in the settings."))
                        403 -> Result.failure(Exception("Access denied. You don't have permission to perform this action."))
                        404 -> Result.failure(Exception("Server endpoint not found. Please check your server configuration."))
                        in 500..599 -> Result.failure(Exception("Server error occurred. Please try again later."))
                        else -> Result.failure(Exception("Network error: ${e.message}"))
                    }
                }
                is io.ktor.client.plugins.ServerResponseException -> {
                    Result.failure(Exception("Server error occurred. Please try again later."))
                }
                else -> Result.failure(Exception("Failed to upload deviation: ${e.message}"))
            }
        }
    }

    suspend fun getDeviations(): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$baseUrl/api/deviations") {
                header("X-API-Key", ApiConfig.apiKey)
            }
            
            if (response.status.isSuccess()) {
                val deviations = Json.decodeFromString<List<Deviation>>(response.bodyAsText())
                Result.success(deviations)
            } else {
                Result.failure(Exception("Failed to fetch deviations: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviation(id: String): Result<Deviation> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$baseUrl/api/deviations/$id") {
                header("X-API-Key", ApiConfig.apiKey)
            }
            
            if (response.status.isSuccess()) {
                val deviation = Json.decodeFromString<Deviation>(response.bodyAsText())
                Result.success(deviation)
            } else {
                Result.failure(Exception("Failed to fetch deviation: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDeviation(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("$baseUrl/api/deviations/$id") {
                header("X-API-Key", ApiConfig.apiKey)
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete deviation: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviationAnalysis(id: String): Result<DeviationAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$baseUrl/api/deviations/$id/analysis") {
                header("X-API-Key", ApiConfig.apiKey)
            }
            
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                // If response is empty or null, return empty DeviationAnalysis
                if (responseText.isBlank()) {
                    Result.success(DeviationAnalysis())
                } else {
                    val analysis = Json.decodeFromString<DeviationAnalysis>(responseText)
                    Result.success(analysis)
                }
            } else {
                Result.failure(Exception("Failed to fetch deviation analysis: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}