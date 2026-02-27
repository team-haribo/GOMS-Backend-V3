package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.request.ReportCreateRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportCreateResponse
import com.example.team.haribo.goms.domain.report.entity.ReviewReport
import com.example.team.haribo.goms.domain.report.exception.AlreadyReportedReviewException
import com.example.team.haribo.goms.domain.report.exception.ReportContentEmptyException
import com.example.team.haribo.goms.domain.report.exception.ReportContentTooLongException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.ReportCreateService
import com.example.team.haribo.goms.domain.review.exception.NotFoundReviewException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportCreateServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : ReportCreateService {

    @Transactional
    override fun create(reviewId: Long, request: ReportCreateRequest): ReportCreateResponse {
        val reporterId = memberUtil.currentMemberId()

        val content = request.content.trim()
        if (content.isBlank()) throw ReportContentEmptyException()
        if (content.length > 500) throw ReportContentTooLongException()

        if (reviewReportRepository.existsByReview_IdAndMemberId(reviewId, reporterId)) {
            throw AlreadyReportedReviewException()
        }

        val review = reviewRepository.findById(reviewId).orElseThrow { NotFoundReviewException() }
        if (review.isDeleted()) throw NotFoundReviewException()

        val report = reviewReportRepository.save(
            ReviewReport(
                review = review,
                memberId = reporterId,
                content = content,
                status = ReportStatus.PENDING
            )
        )

        return ReportCreateResponse(reportId = report.id!!)
    }
}