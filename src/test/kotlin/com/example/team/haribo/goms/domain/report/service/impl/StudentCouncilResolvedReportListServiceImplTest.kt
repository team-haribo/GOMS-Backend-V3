package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class StudentCouncilResolvedReportListServiceImplTest : DescribeSpec({

    val reviewReportRepository = mockk<ReviewReportRepository>()
    val service = StudentCouncilResolvedReportListServiceImpl(reviewReportRepository)

    val reviewer = MemberFixture.student(id = 2L)
    val place = PlaceFixture.place(id = 1L, placeName = "카페")
    val review = ReviewFixture.review(id = 10L, member = reviewer, place = place, content = "시끄러움")

    describe("StudentCouncilResolvedReportListService") {

        context("Given: 처리 완료 신고 목록이 있음") {
            val reports = listOf(
                ReviewReport(
                    id = 20L,
                    review = review,
                    memberId = 1L,
                    content = "스팸성 리뷰",
                    status = ReportStatus.APPROVED
                ).also { it.createdAt = LocalDateTime.now() }
            )

            every {
                reviewReportRepository.findAllByStatusInWithReviewAndReviewer(
                    listOf(ReportStatus.APPROVED, ReportStatus.REJECTED)
                )
            } returns reports

            it("When: 처리 완료 신고 목록 조회 시 Then: 신고 목록을 반환한다") {
                val response = service.getResolvedReports()

                response.reports.size shouldBe 1
                response.reports[0].reportId shouldBe 20L
                response.reports[0].reviewId shouldBe 10L
                response.reports[0].reportContent shouldBe "스팸성 리뷰"
                response.reports[0].reviewContent shouldBe "시끄러움"
                response.reports[0].placeName shouldBe "카페"
                response.reports[0].reportStatus shouldBe ReportStatus.APPROVED
            }
        }

        context("Given: 처리 완료 신고 목록이 없음") {
            every {
                reviewReportRepository.findAllByStatusInWithReviewAndReviewer(
                    listOf(ReportStatus.APPROVED, ReportStatus.REJECTED)
                )
            } returns emptyList()

            it("When: 처리 완료 신고 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getResolvedReports()
                response.reports.shouldBeEmpty()
            }
        }
    }
})