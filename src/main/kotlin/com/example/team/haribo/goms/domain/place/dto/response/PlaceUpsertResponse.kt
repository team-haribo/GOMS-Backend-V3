package com.example.team.haribo.goms.domain.place.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceUpsertResponse(
    @JsonProperty("place_id")
    val placeId: Long
)
