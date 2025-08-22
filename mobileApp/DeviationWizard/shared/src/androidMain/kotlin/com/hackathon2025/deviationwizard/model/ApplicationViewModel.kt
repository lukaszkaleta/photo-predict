package com.hackathon2025.deviationwizard.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hackathon2025.deviationwizard.api.ApiClient
import com.hackathon2025.deviationwizard.api.ApiConfig
import com.hackathon2025.deviationwizard.api.Deviation
import com.hackathon2025.deviationwizard.api.DeviationAnalysis
import com.hackathon2025.deviationwizard.api.DeviationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


class ApplicationViewModel : ViewModel() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    private val _records = MutableStateFlow<List<File>>(emptyList())
    private val _comment = MutableStateFlow("")
    private val _deviations = MutableStateFlow<List<Deviation>>(emptyList())
    private val _selectedDeviation = MutableStateFlow<Deviation?>(null)
    private val _selectedDeviationAnalysis = MutableStateFlow<DeviationAnalysis?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private var _apiClient = ApiClient(ApiConfig.baseUrl)

    init {
        // Clear any initial error state
        _error.value = null
    }

    val bitmaps = _bitmaps.asStateFlow()
    val records = _records.asStateFlow()
    val comment = _comment.asStateFlow()
    val deviations = _deviations.asStateFlow()
    val selectedDeviation = _selectedDeviation.asStateFlow()
    val selectedDeviationAnalysis = _selectedDeviationAnalysis.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val error = _error.asStateFlow()

    fun onPhotoTaken(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }

    fun onImageSelect(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            _bitmaps.value += bitmap
        }
    }

    fun onRemoveBitmap(bitmap: Bitmap) {
        _bitmaps.value -= bitmap
    }

    fun onRecordTaken(file: File) {
        _records.value += file
    }

    fun onRemoveRecord(file: File) {
        _records.value -= file
    }

    fun onCommentChanged(newComment: String) {
        _comment.value = newComment
    }

    fun onSenData() {
        _isLoading.value = true
        _error.value = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Convert bitmaps to Base64 encoded strings
                val imageBytes = _bitmaps.value.map { bitmap ->
                    ByteArrayOutputStream().apply {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    }.toByteArray().let { bytes ->
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                }

                // Convert record files to Base64 encoded strings
                val recordBytes = _records.value.map { file ->
                    Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                }

                // Create and send the deviation request
                val request = DeviationRequest(
                    images = imageBytes,
                    recordings = recordBytes,
                    comment = _comment.value
                )

                _apiClient.uploadDeviation(request).fold(
                    onSuccess = { response ->
                        Log.d("AppView", "Deviation uploaded successfully: $response")
                        // Clear the form after successful upload
                        _bitmaps.value = emptyList()
                        _records.value = emptyList()
                        _comment.value = ""
                        
                        // Refresh the deviations list without changing loading state
                        _apiClient.getDeviations().fold(
                            onSuccess = { deviations ->
                                _deviations.value = deviations
                                // Only set isLoading to false after everything is complete
                                _isLoading.value = false
                            },
                            onFailure = { error ->
                                // Don't show error dialog for GET request
                                Log.e("AppView", "Failed to fetch deviations: ${error.message}")
                                _isLoading.value = false
                            }
                        )
                    },
                    onFailure = { error ->
                        Log.e("AppView", "Failed to upload deviation", error)
                        _error.value = "Failed to upload deviation: ${error.message}"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e("AppView", "Error in onSenData", e)
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchDeviations() {
        _isLoading.value = true
        _error.value = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _apiClient.getDeviations().fold(
                    onSuccess = { deviations ->
                        _deviations.value = deviations
                    },
                    onFailure = { error ->
                        // Don't show error dialog for GET request
                        Log.e("AppView", "Failed to fetch deviations: ${error.message}")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDeviation(id: String) {
        _isLoading.value = true
        _error.value = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _apiClient.getDeviation(id).fold(
                    onSuccess = { deviation ->
                        _selectedDeviation.value = deviation
                        // Fetch analysis after getting deviation
                        fetchDeviationAnalysis(id)
                    },
                    onFailure = { error ->
                        // Don't show error dialog for GET request
                        Log.e("AppView", "Failed to fetch deviation: ${error.message}")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchDeviationAnalysis(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _apiClient.getDeviationAnalysis(id).fold(
                    onSuccess = { analysis ->
                        _selectedDeviationAnalysis.value = analysis
                    },
                    onFailure = { error ->
                        Log.e("ApplicationViewModel", "Failed to fetch deviation analysis: ${error.message}")
                        // Don't set error state here as it's not critical
                    }
                )
            } catch (e: Exception) {
                Log.e("ApplicationViewModel", "Error fetching deviation analysis: ${e.message}")
            }
        }
    }

    fun updateBaseUrl(url: String) {
        ApiConfig.setBaseUrl(url)
        // Recreate the API client with the new URL
        _apiClient = ApiClient(ApiConfig.baseUrl)
        // Refresh the deviations list with the new URL
        fetchDeviations()
    }

    fun deleteDeviation(id: String) {
        _isLoading.value = true
        _error.value = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _apiClient.deleteDeviation(id).fold(
                    onSuccess = {
                        // Remove the deviation from the list
                        _deviations.value = _deviations.value.filter { it.id != id }
                    },
                    onFailure = { error ->
                        // Don't show error dialog for DELETE request
                        Log.e("AppView", "Failed to delete deviation: ${error.message}")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}