package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.review.dto.response.MyReviewCountResponse
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewMyCountService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewMyCountServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : ReviewMyCountService {

    @Transactional(readOnly = true)
    override fun getMyReviewCount(): MyReviewCountResponse {
        val memberId = memberUtil.currentMemberId()
        val count = reviewRepository.countAllByMemberIdAndDeletedAtIsNull(memberId)
        return MyReviewCountResponse(count = count)
    }
}