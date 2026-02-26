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
        val approved = reviewReportRepository.findAllByStatusWithReviewAndReviewer(ReportStatus.APPROVED)
        val rejected = reviewReportRepository.findAllByStatusWithReviewAndReviewer(ReportStatus.REJECTED)
        val merged = (approved + rejected).sortedByDescending { it.createdAt }

        return ReportListResponse(
            reports = merged.map {
                val review = it.review
                val reviewer = review.member
                ReportResponse(
                    report_id = it.id!!,
                    reason = it.reason,
                    review_id = review.id!!,
                    reviewer_member_id = reviewer.id!!,
                    reviewer_name = reviewer.name,
                    reviewer_grade = reviewer.grade,
                    reviewer_department = reviewer.department,
                    report_created_at = it.createdAt!!,
                    report_status = it.status,
                    deleted_at = review.deletedAt,
                    deleted_by = review.deletedBy
                )
            }
        )
    }
}