package com.example.team.haribo.goms.domain.review.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReviewCreateRequest(
    @field:NotBlank(message = "리뷰 내용은 비어 있을 수 없습니다.")
    @field:Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다.")
    val content: String
)