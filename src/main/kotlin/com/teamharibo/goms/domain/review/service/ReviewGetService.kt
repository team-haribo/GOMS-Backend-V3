package com.teamharibo.goms.domain.review.service

import com.teamharibo.goms.domain.review.dto.response.PlaceReviewListResponse
import com.teamharibo.goms.domain.review.dto.response.ReviewCountResponse

interface ReviewGetService {
    fun getPlaceReviews(placeId: Long): PlaceReviewListResponse
    fun countPlaceReviews(placeId: Long): ReviewCountResponse
}
