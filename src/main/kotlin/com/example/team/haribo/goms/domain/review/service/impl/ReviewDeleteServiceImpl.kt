package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.exception.NotFoundReviewException
import com.example.team.haribo.goms.domain.review.exception.ReviewForbiddenException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.domain.review.service.ReviewDeleteService
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewDeleteServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : ReviewDeleteService {

    private val log = LoggerFactory.getLogger(ReviewDeleteServiceImpl::class.java)

    @Transactional
    override fun delete(reviewId: Long) {
        val memberId = memberUtil.currentMemberId()

        log.info(
            LogFormat.message(
                domain = "REVIEW",
                event = "리뷰 삭제 시도",
                "memberId" to memberId,
                "reviewId" to reviewId
            )
        )

        val review = reviewRepository.findById(reviewId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 삭제 실패",
                    "memberId" to memberId,
                    "reviewId" to reviewId,
                    "reason" to "존재하지 않는 리뷰"
                )
            )
            NotFoundReviewException()
        }

        if (review.member.id != memberId) {
            log.warn(
                LogFormat.message(
                    domain = "REVIEW",
                    event = "리뷰 삭제 실패",
                    "memberId" to memberId,
                    "reviewId" to reviewId,
                    "reason" to "삭제 권한 없음"
                )
            )
            throw ReviewForbiddenException()
        }

        reviewReportRepository.deleteAllByReview_Id(reviewId)
        reviewRepository.delete(review)

        log.info(
            LogFormat.message(
                domain = "REVIEW",
                event = "리뷰 삭제 완료",
                "memberId" to memberId,
                "reviewId" to reviewId
            )
        )
    }
}