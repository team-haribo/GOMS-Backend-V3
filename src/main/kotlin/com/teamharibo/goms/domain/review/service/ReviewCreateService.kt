package com.teamharibo.goms.domain.review.service

import com.teamharibo.goms.domain.review.dto.request.ReviewCreateRequest
import com.teamharibo.goms.domain.review.dto.response.ReviewCreateResponse

interface ReviewCreateService {
    fun create(placeId: Long, request: ReviewCreateRequest): ReviewCreateResponse
}
