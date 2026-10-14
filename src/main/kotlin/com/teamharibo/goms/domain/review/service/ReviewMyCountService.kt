package com.teamharibo.goms.domain.review.service

import com.teamharibo.goms.domain.review.dto.response.MyReviewCountResponse

interface ReviewMyCountService {
    fun getMyReviewCount(): MyReviewCountResponse
}