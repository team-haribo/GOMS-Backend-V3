package com.teamharibo.goms.domain.review.service

import com.teamharibo.goms.domain.review.dto.response.MyReviewListResponse

interface ReviewMyListService {
    fun getMyReviews(): MyReviewListResponse
}