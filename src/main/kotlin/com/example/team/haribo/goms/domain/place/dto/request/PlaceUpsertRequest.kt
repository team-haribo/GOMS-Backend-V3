package com.example.team.haribo.goms.domain.place.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceUpsertRequest(
    val latitude: Double,
    val longitude: Double
)
