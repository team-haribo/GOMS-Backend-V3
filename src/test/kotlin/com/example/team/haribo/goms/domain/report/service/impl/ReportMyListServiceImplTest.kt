package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class ReportMyListServiceImplTest : DescribeSpec({

    val reviewReportRepository = mockk<ReviewReportRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReportMyListServiceImpl(reviewReportRepository, memberUtil)

    val current = MemberFixture.student(id = 1L)
    val reviewer = MemberFixture.student(id = 3L)
    val place = PlaceFixture.place(id = 7L, placeName = "매점")
    val review = ReviewFixture.review(id = 5L, place = place, member = reviewer, content = "불친절함")

    describe("ReportMyListService") {

        context("Given: 내 신고 목록이 있음") {
            val reports = listOf(
                ReviewReport(
                    id = 11L,
                    review = review,
                    memberId = current.id!!,
                    content = "욕설 포함",
                    status = ReportStatus.PENDING
                ).also { it.createdAt = LocalDateTime.now() }
            )

            every { memberUtil.currentMember() } returns current
            every { reviewReportRepository.findAllByMemberIdWithReviewAndReviewer(current.id!!) } returns reports

            it("When: 내 신고 목록 조회 시 Then: 신고 목록을 반환한다") {
                val response = service.getMyReports()

                response.reports.size shouldBe 1
                response.reports[0].reportId shouldBe 11L
                response.reports[0].reviewId shouldBe 5L
                response.reports[0].reportContent shouldBe "욕설 포함"
                response.reports[0].reviewContent shouldBe "불친절함"
                response.reports[0].placeName shouldBe "매점"
            }
        }

        context("Given: 내 신고 목록이 없음") {
            every { memberUtil.currentMember() } returns current
            every { reviewReportRepository.findAllByMemberIdWithReviewAndReviewer(current.id!!) } returns emptyList()

            it("When: 내 신고 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getMyReports()
                response.reports.shouldBeEmpty()
            }
        }
    }
})