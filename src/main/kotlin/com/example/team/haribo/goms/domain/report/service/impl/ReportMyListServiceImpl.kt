package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResponse
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.ReportMyListService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportMyListServiceImpl(
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : ReportMyListService {

    @Transactional(readOnly = true)
    override fun getMyReports(): ReportListResponse {
        val memberId = memberUtil.currentMemberId()
        val reports = reviewReportRepository.findAllByMemberIdWithReviewAndReviewer(memberId)

        return ReportListResponse(
            reports = reports.map {
                val review = it.review
                val reviewer = review.member
                ReportResponse(
                    reportId = it.id!!,
                    reason = it.reason,
                    reviewId = review.id!!,
                    reviewerMemberId = reviewer.id!!,
                    reviewerName = reviewer.name,
                    reviewerGrade = reviewer.grade,
                    reviewerDepartment = reviewer.department,
                    reportCreatedAt = it.createdAt!!,
                    reportStatus = it.status,
                    deletedAt = review.deletedAt,
                    deletedBy = review.deletedBy
                )
            }
        )
    }
}