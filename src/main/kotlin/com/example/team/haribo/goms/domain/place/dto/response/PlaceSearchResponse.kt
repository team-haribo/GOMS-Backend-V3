package com.example.team.haribo.goms.domain.place.dto.response

data class PlaceSearchResponse(
    val placeId: Long,
    val placeName: String,
    val address: String,
    val roadAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val categoryGroupName: String?,
    val categoryName: String?,
    val reviewCount: Long,
    val recommendCount: Long,
    val recommended: Boolean
)