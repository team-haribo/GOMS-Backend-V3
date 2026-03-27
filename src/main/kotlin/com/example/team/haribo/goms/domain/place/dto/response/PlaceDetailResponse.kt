package com.example.team.haribo.goms.domain.place.dto.response

data class PlaceDetailResponse(
    val placeId: Long,
    val latitude: Double,
    val longitude: Double,
    val reviewCount: Long,
    val recommendCount: Long,
    val recommended: Boolean
)