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