package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.exception.NotFoundReportException
import com.example.team.haribo.goms.domain.report.exception.ReportForbiddenException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.Optional

class ReportDetailServiceImplTest : DescribeSpec({

    val reviewReportRepository = mockk<ReviewReportRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReportDetailServiceImpl(reviewReportRepository, memberUtil)

    val reporter = MemberFixture.student(id = 1L)
    val otherStudent = MemberFixture.student(id = 99L)
    val council = MemberFixture.council(id = 2L)
    val reviewer = MemberFixture.student(id = 3L, profileImageUrl = "profile.png")
    val place = PlaceFixture.place(id = 5L, placeName = "학생식당")
    val review = ReviewFixture.review(id = 10L, member = reviewer, place = place, content = "시끄러움")
    val report = ReviewReport(
        id = 20L,
        review = review,
        memberId = reporter.id!!,
        content = "욕설이 포함됨",
        status = ReportStatus.PENDING
    ).also { it.createdAt = LocalDateTime.now() }

    describe("ReportDetailService") {

        context("Given: 학생회가 상세 조회") {
            every { memberUtil.currentMember() } returns council
            every { reviewReportRepository.findByIdWithReviewAndReviewer(report.id!!) } returns Optional.of(report)

            it("When: 상세 조회 시 Then: placeName과 신고 내용을 반환한다") {
                val response = service.getReportDetail(report.id!!)

                response.reportId shouldBe 20L
                response.reviewId shouldBe 10L
                response.placeName shouldBe "학생식당"
                response.reportContent shouldBe "욕설이 포함됨"
                response.reviewContent shouldBe "시끄러움"
                response.reviewerMemberId shouldBe reviewer.id!!
            }
        }

        context("Given: 신고 본인이 상세 조회") {
            every { memberUtil.currentMember() } returns reporter
            every { reviewReportRepository.findByIdWithReviewAndReviewer(report.id!!) } returns Optional.of(report)

            it("When: 상세 조회 시 Then: 조회에 성공한다") {
                val response = service.getReportDetail(report.id!!)
                response.reportId shouldBe report.id!!
            }
        }

        context("Given: 타 학생이 상세 조회") {
            every { memberUtil.currentMember() } returns otherStudent
            every { reviewReportRepository.findByIdWithReviewAndReviewer(report.id!!) } returns Optional.of(report)

            it("When: 상세 조회 시 Then: ReportForbiddenException이 발생한다") {
                shouldThrow<ReportForbiddenException> {
                    service.getReportDetail(report.id!!)
                }
            }
        }

        context("Given: 존재하지 않는 신고") {
            every { memberUtil.currentMember() } returns council
            every { reviewReportRepository.findByIdWithReviewAndReviewer(999L) } returns Optional.empty()

            it("When: 상세 조회 시 Then: NotFoundReportException이 발생한다") {
                shouldThrow<NotFoundReportException> {
                    service.getReportDetail(999L)
                }
            }
        }
    }
})