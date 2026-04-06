package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResponse
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.StudentCouncilResolvedReportListService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilResolvedReportListServiceImpl(
    private val reviewReportRepository: ReviewReportRepository
) : StudentCouncilResolvedReportListService {

    @Transactional(readOnly = true)
    override fun getResolvedReports(): ReportListResponse {
        val reports = reviewReportRepository.findAllByStatusInWithReviewAndReviewer(
            listOf(ReportStatus.APPROVED, ReportStatus.REJECTED)
        )

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
                    reviewerProfileImageUrl = reviewer.profileImageUrl,
                    reportCreatedAt = it.createdAt!!,
                    reportStatus = it.status,
                    deletedAt = review.deletedAt,
                    deletedBy = review.deletedBy,
                )
            }
        )
    }
}