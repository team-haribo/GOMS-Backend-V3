package com.example.team.haribo.goms.domain.review.service

import com.example.team.haribo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.example.team.haribo.goms.domain.review.dto.response.ReviewCountResponse

interface ReviewGetService {
    fun getPlaceReviews(placeId: Long): PlaceReviewListResponse
    fun countPlaceReviews(placeId: Long): ReviewCountResponse
}
