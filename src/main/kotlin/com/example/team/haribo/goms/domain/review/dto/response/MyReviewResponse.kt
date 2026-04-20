package com.example.team.haribo.goms.domain.review.dto.response

import java.time.LocalDateTime

data class MyReviewResponse(
    val reviewId: Long,
    val placeId: Long,
    val placeName: String,
    val categoryName: String?,
    val address: String,
    val content: String,
    val reviewedAt: LocalDateTime
)