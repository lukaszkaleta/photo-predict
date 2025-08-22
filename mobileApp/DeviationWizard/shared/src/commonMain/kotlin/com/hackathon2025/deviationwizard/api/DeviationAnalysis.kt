package com.hackathon2025.deviationwizard.api

import kotlinx.serialization.Serializable

@Serializable
data class DeviationAnalysis(
    val transcriptions: Map<String, String> = emptyMap(),
    val images: Map<String, String> = emptyMap(),
    val solution: Solution? = null
)

@Serializable
data class Solution(
    val issueType: String,
    val summary: String,
    val priorityLevel: String,
    val repairEffortHours: String,
    val checkList: List<String> = emptyList()
) 