package com.example.team.haribo.goms.domain.review.service

import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCreateResponse

interface ReviewCreateService {
    fun create(placeId: Long, request: ReviewCreateRequest): ReviewCreateResponse
}
