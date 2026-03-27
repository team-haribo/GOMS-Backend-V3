package com.example.team.haribo.goms.domain.place.dto.response

data class PlaceSummaryResponse(
    val placeId: Long,
    val reviewCount: Long,
    val recommendCount: Long,
    val recommended: Boolean
)