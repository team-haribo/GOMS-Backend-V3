package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.report.dto.response.ReportDetailResponse
import com.example.team.haribo.goms.domain.report.exception.NotFoundReportException
import com.example.team.haribo.goms.domain.report.exception.ReportForbiddenException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.ReportDetailService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportDetailServiceImpl(
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : ReportDetailService {

    @Transactional(readOnly = true)
    override fun getReportDetail(reportId: Long): ReportDetailResponse {
        val current = memberUtil.currentMember()

        val report = reviewReportRepository.findByIdWithReviewAndReviewer(reportId)
            .orElseThrow { NotFoundReportException() }

        if (current.role != Role.ROLE_STUDENT_COUNCIL && report.memberId != current.id) {
            throw ReportForbiddenException()
        }

        val review = report.review
        val reviewer = review.member

        return ReportDetailResponse(
            report_id = report.id!!,
            reason = report.reason,
            review_id = review.id!!,
            review_created_at = review.createdAt!!,
            reviewer_member_id = reviewer.id!!,
            reviewer_name = reviewer.name,
            reviewer_grade = reviewer.grade,
            reviewer_department = reviewer.department,
            review_content = review.content,
            report_content = report.content,
            report_created_at = report.createdAt!!,
            report_status = report.status,
            deleted_at = review.deletedAt,
            deleted_by = review.deletedBy
        )
    }
}