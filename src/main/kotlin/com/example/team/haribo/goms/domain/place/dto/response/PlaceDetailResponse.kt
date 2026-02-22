package com.example.team.haribo.goms.domain.place.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceDetailResponse(
    @JsonProperty("place_id")
    val placeId: Long,
    val latitude: Double,
    val longitude: Double,
    @JsonProperty("review_count")
    val reviewCount: Long,
    @JsonProperty("recommend_count")
    val recommendCount: Long,
    val recommended: Boolean
)
