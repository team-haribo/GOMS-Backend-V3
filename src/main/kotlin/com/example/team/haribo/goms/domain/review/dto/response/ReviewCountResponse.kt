package com.example.team.haribo.goms.domain.review.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewCountResponse(
    @JsonProperty("review_count")
    val reviewCount: Long
)
