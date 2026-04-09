package com.example.team.haribo.goms.domain.place.dto.response

data class PlaceSyncResult(
    val createdCount: Int,
    val updatedCount: Int,
    val deactivatedCount: Int,
    val totalFetchedCount: Int
)