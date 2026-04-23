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

class StudentCouncilPendingReportListServiceImplTest : DescribeSpec({

    val reviewReportRepository = mockk<ReviewReportRepository>()
    val service = StudentCouncilPendingReportListServiceImpl(reviewReportRepository)

    val reviewer = MemberFixture.student(id = 2L, profileImageUrl = "profile.png")
    val place = PlaceFixture.place(id = 1L, placeName = "급식실")
    val review = ReviewFixture.review(id = 10L, member = reviewer, place = place, content = "더러움")

    describe("StudentCouncilPendingReportListService") {

        context("Given: 대기 중 신고 목록이 있음") {
            val reports = listOf(
                ReviewReport(
                    id = 20L,
                    review = review,
                    memberId = 1L,
                    content = "욕설 포함",
                    status = ReportStatus.PENDING
                ).also { it.createdAt = LocalDateTime.now() }
            )

            every { reviewReportRepository.findAllByStatusWithReviewAndReviewer(ReportStatus.PENDING) } returns reports

            it("When: 대기 중 신고 목록 조회 시 Then: 신고 목록을 반환한다") {
                val response = service.getPendingReports()

                response.reports.size shouldBe 1
                response.reports[0].reportId shouldBe 20L
                response.reports[0].reviewId shouldBe 10L
                response.reports[0].reviewerMemberId shouldBe reviewer.id!!
                response.reports[0].reportContent shouldBe "욕설 포함"
                response.reports[0].reviewContent shouldBe "더러움"
                response.reports[0].placeName shouldBe "급식실"
                response.reports[0].reportStatus shouldBe ReportStatus.PENDING
            }
        }

        context("Given: 대기 중 신고 목록이 없음") {
            every { reviewReportRepository.findAllByStatusWithReviewAndReviewer(ReportStatus.PENDING) } returns emptyList()

            it("When: 대기 중 신고 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getPendingReports()
                response.reports.shouldBeEmpty()
            }
        }
    }
})