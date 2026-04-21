package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.exception.InvalidReportStatusException
import com.example.team.haribo.goms.domain.report.exception.NotFoundReportException
import com.example.team.haribo.goms.domain.report.exception.ReportAlreadyResolvedException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.Optional

class StudentCouncilReportResolveServiceImplTest : DescribeSpec({

    val reviewReportRepository = mockk<ReviewReportRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = StudentCouncilReportResolveServiceImpl(reviewReportRepository, memberUtil)

    val resolver = MemberFixture.council(id = 7L)
    val review = ReviewFixture.review(id = 10L, content = "문제있는 리뷰")

    describe("StudentCouncilReportResolveService") {

        context("Given: 승인 처리") {
            val report = ReviewReport(
                id = 20L,
                review = review,
                memberId = 1L,
                content = "부적절함",
                status = ReportStatus.PENDING
            ).also { it.createdAt = LocalDateTime.now() }

            every { memberUtil.currentMember() } returns resolver
            every { reviewReportRepository.findByIdWithReviewAndReviewer(20L) } returns Optional.of(report)

            it("When: APPROVED로 처리 시 Then: 리뷰가 soft delete 된다") {
                val response = service.resolve(20L, ReportResolveRequest(reportStatus = ReportStatus.APPROVED))

                response.reportId shouldBe 20L
                response.reviewId shouldBe 10L
                response.reportStatus shouldBe ReportStatus.APPROVED
                response.resolvedBy shouldBe resolver.id!!
                review.deletedBy shouldBe resolver.name
            }
        }

        context("Given: 반려 처리") {
            val report = ReviewReport(
                id = 21L,
                review = ReviewFixture.review(id = 11L, content = "정상 리뷰"),
                memberId = 1L,
                content = "오신고",
                status = ReportStatus.PENDING
            ).also { it.createdAt = LocalDateTime.now() }

            every { memberUtil.currentMember() } returns resolver
            every { reviewReportRepository.findByIdWithReviewAndReviewer(21L) } returns Optional.of(report)

            it("When: REJECTED로 처리 시 Then: 리뷰는 삭제되지 않는다") {
                val response = service.resolve(21L, ReportResolveRequest(reportStatus = ReportStatus.REJECTED))

                response.reportStatus shouldBe ReportStatus.REJECTED
                report.review.deletedAt shouldBe null
                report.review.deletedBy shouldBe null
            }
        }

        context("Given: 존재하지 않는 신고") {
            every { memberUtil.currentMember() } returns resolver
            every { reviewReportRepository.findByIdWithReviewAndReviewer(999L) } returns Optional.empty()

            it("When: 신고 처리 시 Then: NotFoundReportException이 발생한다") {
                shouldThrow<NotFoundReportException> {
                    service.resolve(999L, ReportResolveRequest(reportStatus = ReportStatus.APPROVED))
                }
            }
        }

        context("Given: 이미 처리된 신고") {
            val report = ReviewReport(
                id = 30L,
                review = review,
                memberId = 1L,
                content = "이미 처리됨",
                status = ReportStatus.REJECTED
            ).also {
                it.createdAt = LocalDateTime.now()
                it.resolvedAt = LocalDateTime.now()
                it.resolvedBy = resolver.id
            }

            every { memberUtil.currentMember() } returns resolver
            every { reviewReportRepository.findByIdWithReviewAndReviewer(30L) } returns Optional.of(report)

            it("When: 신고 처리 시 Then: ReportAlreadyResolvedException이 발생한다") {
                shouldThrow<ReportAlreadyResolvedException> {
                    service.resolve(30L, ReportResolveRequest(reportStatus = ReportStatus.APPROVED))
                }
            }
        }

        context("Given: PENDING 상태로 처리 요청") {
            val report = ReviewReport(
                id = 40L,
                review = review,
                memberId = 1L,
                content = "잘못된 요청",
                status = ReportStatus.PENDING
            ).also { it.createdAt = LocalDateTime.now() }

            every { memberUtil.currentMember() } returns resolver
            every { reviewReportRepository.findByIdWithReviewAndReviewer(40L) } returns Optional.of(report)

            it("When: PENDING으로 처리 시 Then: InvalidReportStatusException이 발생한다") {
                shouldThrow<InvalidReportStatusException> {
                    service.resolve(40L, ReportResolveRequest(reportStatus = ReportStatus.PENDING))
                }
            }
        }
    }
})