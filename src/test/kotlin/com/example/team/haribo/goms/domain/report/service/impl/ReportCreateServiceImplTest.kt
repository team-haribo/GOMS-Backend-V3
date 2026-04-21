package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.request.ReportCreateRequest
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.exception.AlreadyReportedReviewException
import com.example.team.haribo.goms.domain.report.exception.ReportContentEmptyException
import com.example.team.haribo.goms.domain.report.exception.ReportContentTooLongException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.exception.NotFoundReviewException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class ReportCreateServiceImplTest : DescribeSpec({

    val reviewRepository = mockk<ReviewRepository>()
    val reviewReportRepository = mockk<ReviewReportRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReportCreateServiceImpl(reviewRepository, reviewReportRepository, memberUtil)

    val reporter = MemberFixture.student(id = 1L)
    val review = ReviewFixture.review(id = 10L, member = MemberFixture.student(id = 2L))

    describe("ReportCreateService") {

        context("Given: 정상적인 신고 요청") {
            every { memberUtil.currentMemberId() } returns reporter.id!!
            every { reviewReportRepository.existsByReview_IdAndMemberId(review.id!!, reporter.id!!) } returns false
            every { reviewRepository.findById(review.id!!) } returns Optional.of(review)
            every { reviewReportRepository.save(any()) } answers {
                val saved = firstArg<ReviewReport>()
                ReviewReport(
                    id = 100L,
                    review = saved.review,
                    memberId = saved.memberId,
                    content = saved.content,
                    status = saved.status
                )
            }

            it("When: 신고 생성 시 Then: 신고 ID를 반환한다") {
                val response = service.create(review.id!!, ReportCreateRequest(content = "부적절한 리뷰입니다."))

                response.reportId shouldBe 100L
                verify(exactly = 1) { reviewReportRepository.save(any()) }
            }
        }

        context("Given: 신고 내용이 공백") {
            every { memberUtil.currentMemberId() } returns reporter.id!!

            it("When: 신고 생성 시 Then: ReportContentEmptyException이 발생한다") {
                shouldThrow<ReportContentEmptyException> {
                    service.create(review.id!!, ReportCreateRequest(content = "   "))
                }
            }
        }

        context("Given: 신고 내용이 500자 초과") {
            every { memberUtil.currentMemberId() } returns reporter.id!!

            it("When: 신고 생성 시 Then: ReportContentTooLongException이 발생한다") {
                shouldThrow<ReportContentTooLongException> {
                    service.create(review.id!!, ReportCreateRequest(content = "a".repeat(501)))
                }
            }
        }

        context("Given: 이미 신고한 리뷰") {
            every { memberUtil.currentMemberId() } returns reporter.id!!
            every { reviewReportRepository.existsByReview_IdAndMemberId(review.id!!, reporter.id!!) } returns true

            it("When: 신고 생성 시 Then: AlreadyReportedReviewException이 발생한다") {
                shouldThrow<AlreadyReportedReviewException> {
                    service.create(review.id!!, ReportCreateRequest(content = "중복 신고"))
                }
            }
        }

        context("Given: 존재하지 않는 리뷰") {
            every { memberUtil.currentMemberId() } returns reporter.id!!
            every { reviewReportRepository.existsByReview_IdAndMemberId(review.id!!, reporter.id!!) } returns false
            every { reviewRepository.findById(review.id!!) } returns Optional.empty()

            it("When: 신고 생성 시 Then: NotFoundReviewException이 발생한다") {
                shouldThrow<NotFoundReviewException> {
                    service.create(review.id!!, ReportCreateRequest(content = "없는 리뷰 신고"))
                }
            }
        }

        context("Given: 이미 삭제된 리뷰") {
            review.softDelete("학생회장")

            every { memberUtil.currentMemberId() } returns reporter.id!!
            every { reviewReportRepository.existsByReview_IdAndMemberId(review.id!!, reporter.id!!) } returns false
            every { reviewRepository.findById(review.id!!) } returns Optional.of(review)

            it("When: 신고 생성 시 Then: NotFoundReviewException이 발생한다") {
                shouldThrow<NotFoundReviewException> {
                    service.create(review.id!!, ReportCreateRequest(content = "삭제된 리뷰 신고"))
                }
            }
        }
    }
})