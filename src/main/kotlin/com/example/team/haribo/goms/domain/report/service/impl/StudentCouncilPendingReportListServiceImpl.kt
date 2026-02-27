package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResponse
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.StudentCouncilPendingReportListService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilPendingReportListServiceImpl(
    private val reviewReportRepository: ReviewReportRepository
) : StudentCouncilPendingReportListService {

    @Transactional(readOnly = true)
    override fun getPendingReports(): ReportListResponse {
        val reports = reviewReportRepository.findAllByStatusWithReviewAndReviewer(ReportStatus.PENDING)

        return ReportListResponse(
            reports = reports.map {
                val review = it.review
                val reviewer = review.member
                ReportResponse(
                    reportId = it.id!!,
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