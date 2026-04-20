package com.example.team.haribo.goms.domain.review.service

import com.example.team.haribo.goms.domain.review.dto.response.MyReviewListResponse

interface ReviewMyListService {
    fun getMyReviews(): MyReviewListResponse
}