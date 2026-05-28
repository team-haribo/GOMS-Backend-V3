package com.example.team.haribo.goms.domain.studentcouncil.dto.response

data class DiscordStudentCouncilApplyResponse(
    val success: Boolean,
    val syncedCount: Int,
    val syncedUsers: List<String>,
    val failedCount: Int,
    val failedUsers: List<String>,
    val appliedAt: String,
    val durationMs: Long
)