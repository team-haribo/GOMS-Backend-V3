package com.example.team.haribo.goms.domain.place.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendCountResponse(
    @JsonProperty("recommend_count")
    val recommendCount: Long
)
