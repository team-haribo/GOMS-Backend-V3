package com.example.team.haribo.goms.domain.place.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceSummaryResponse(
    @JsonProperty("place_id")
    val placeId: Long,
    @JsonProperty("review_count")
    val reviewCount: Long,
    @JsonProperty("recommend_count")
    val recommendCount: Long,
    val recommended: Boolean
)
