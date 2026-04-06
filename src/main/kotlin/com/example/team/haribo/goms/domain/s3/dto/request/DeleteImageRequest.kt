package com.example.team.haribo.goms.domain.s3.dto.request

import jakarta.validation.constraints.NotBlank

data class DeleteImageRequest(
    @field:NotBlank(message = "imageUrl은 비어 있을 수 없습니다.")
    val imageUrl: String
)