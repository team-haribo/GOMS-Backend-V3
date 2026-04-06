package com.example.team.haribo.goms.domain.review.dto.response

import com.example.team.haribo.goms.domain.common.enums.Department
import java.time.LocalDateTime

data class ReviewResponse(
    val review_id: Long,
    val name: String,
    val grade: Int,
    val department: Department,
    val profileImageUrl: String?,
    val content: String,
    val reviewed_at: LocalDateTime
)
