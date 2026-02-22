package com.example.team.haribo.goms.domain.place.dto.request

data class PlaceUpsertRequest(
    val placeName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
