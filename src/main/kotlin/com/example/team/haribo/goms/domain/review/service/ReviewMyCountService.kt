package com.example.team.haribo.goms.domain.review.service

import com.example.team.haribo.goms.domain.review.dto.response.MyReviewCountResponse

interface ReviewMyCountService {
    fun getMyReviewCount(): MyReviewCountResponse
}