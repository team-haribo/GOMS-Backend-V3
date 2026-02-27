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
            reportId = report.id!!,
            reviewId = review.id!!,
            reviewCreatedAt = review.createdAt!!,
            reviewerMemberId = reviewer.id!!,
            reviewerName = reviewer.name,
            reviewerGrade = reviewer.grade,
            reviewerDepartment = reviewer.department,
            reviewContent = review.content,
            reportContent = report.content,
            reportCreatedAt = report.createdAt!!,
            reportStatus = report.status,
            deletedAt = review.deletedAt,
            deletedBy = review.deletedBy
        )
    }
}