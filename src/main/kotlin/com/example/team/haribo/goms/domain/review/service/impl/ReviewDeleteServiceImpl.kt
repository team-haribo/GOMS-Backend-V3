package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.review.exception.NotFoundReviewException
import com.example.team.haribo.goms.domain.review.exception.ReviewForbiddenException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewDeleteService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewDeleteServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val memberUtil: MemberUtil
) : ReviewDeleteService {

    @Transactional
    override fun delete(reviewId: Long) {
        val memberId = memberUtil.currentMemberId()
        val review = reviewRepository.findAnyById(reviewId).orElseThrow { NotFoundReviewException() }

        if (review.member.id != memberId) throw ReviewForbiddenException()

        reviewRepository.delete(review)
    }
}
