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

    /**
     * 작성자 본인이 직접 삭제하는 경우 물리적으로 제거합니다.
     *
     * 신고를 통한 학생회 처리 삭제는 감사 추적(audit trail)을 위해 소프트 딜리트로 처리하며,
     * 해당 로직은 신고/학생회 도메인의 별도 서비스에서 담당합니다.
     *
     * @param reviewId 삭제할 리뷰의 ID
     * @throws NotFoundReviewException 리뷰가 존재하지 않는 경우
     * @throws ReviewForbiddenException 본인이 작성한 리뷰가 아닌 경우
     */
    @Transactional
    override fun delete(reviewId: Long) {
        val memberId = memberUtil.currentMemberId()
        val review = reviewRepository.findById(reviewId).orElseThrow { NotFoundReviewException() }

        if (review.member.id != memberId) throw ReviewForbiddenException()

        reviewRepository.delete(review)
    }
}
