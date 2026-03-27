package com.example.team.haribo.goms.domain.place.dto.response

data class PlaceSearchResponse(
    val latitude: Double,
    val longitude: Double,
    val placeId: Long,
    val reviewCount: Long,
    val recommendCount: Long,
    val recommended: Boolean
)