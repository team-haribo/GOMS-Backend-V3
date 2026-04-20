package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.exception.NotFoundReviewException
import com.example.team.haribo.goms.domain.review.exception.ReviewForbiddenException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class ReviewDeleteServiceImplTest : DescribeSpec({

    val reviewRepository = mockk<ReviewRepository>()
    val reviewReportRepository = mockk<ReviewReportRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReviewDeleteServiceImpl(reviewRepository, reviewReportRepository, memberUtil)

    val owner = MemberFixture.student(id = 1L)
    val other = MemberFixture.student(id = 99L)

    describe("ReviewDeleteService") {

        context("Given: 본인 리뷰 삭제") {
            val review = ReviewFixture.review(id = 1L, member = owner)
            every { memberUtil.currentMemberId() } returns owner.id!!
            every { reviewRepository.findById(1L) } returns Optional.of(review)
            every { reviewReportRepository.deleteAllByReview_Id(1L) } returns 0L
            justRun { reviewRepository.delete(review) }

            it("When: 삭제 요청 시 Then: 리뷰 신고가 먼저 삭제되고 리뷰가 삭제된다") {
                service.delete(1L)

                verify(exactly = 1) { reviewReportRepository.deleteAllByReview_Id(1L) }
                verify(exactly = 1) { reviewRepository.delete(review) }
            }
        }

        context("Given: 존재하지 않는 reviewId") {
            every { memberUtil.currentMemberId() } returns owner.id!!
            every { reviewRepository.findById(999L) } returns Optional.empty()

            it("When: 삭제 요청 시 Then: NotFoundReviewException이 발생한다") {
                shouldThrow<NotFoundReviewException> {
                    service.delete(999L)
                }
            }
        }

        context("Given: 타인의 리뷰 삭제 시도") {
            val review = ReviewFixture.review(id = 2L, member = owner)
            every { memberUtil.currentMemberId() } returns other.id!!
            every { reviewRepository.findById(2L) } returns Optional.of(review)

            it("When: 삭제 요청 시 Then: ReviewForbiddenException이 발생한다") {
                shouldThrow<ReviewForbiddenException> {
                    service.delete(2L)
                }
            }
        }
    }
})