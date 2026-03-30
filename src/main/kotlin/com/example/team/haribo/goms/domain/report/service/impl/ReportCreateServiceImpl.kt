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
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportCreateServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : ReportCreateService {

    private val log = LoggerFactory.getLogger(ReportCreateServiceImpl::class.java)

    @Transactional
    override fun create(reviewId: Long, request: ReportCreateRequest): ReportCreateResponse {
        val reporterId = memberUtil.currentMemberId()
        val content = request.content.trim()

        log.info(
            LogFormat.message(
                domain = "REPORT",
                event = "신고 작성 시도",
                "reporterId" to reporterId,
                "reviewId" to reviewId,
                "contentLength" to content.length
            )
        )

        if (content.isBlank()) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 작성 실패",
                    "reporterId" to reporterId,
                    "reviewId" to reviewId,
                    "reason" to "내용 없음"
                )
            )
            throw ReportContentEmptyException()
        }

        if (content.length > 500) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 작성 실패",
                    "reporterId" to reporterId,
                    "reviewId" to reviewId,
                    "reason" to "내용 길이 초과"
                )
            )
            throw ReportContentTooLongException()
        }

        if (reviewReportRepository.existsByReview_IdAndMemberId(reviewId, reporterId)) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 작성 실패",
                    "reporterId" to reporterId,
                    "reviewId" to reviewId,
                    "reason" to "이미 신고한 리뷰"
                )
            )
            throw AlreadyReportedReviewException()
        }

        val review = reviewRepository.findById(reviewId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 작성 실패",
                    "reporterId" to reporterId,
                    "reviewId" to reviewId,
                    "reason" to "존재하지 않는 리뷰"
                )
            )
            NotFoundReviewException()
        }

        if (review.isDeleted()) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 작성 실패",
                    "reporterId" to reporterId,
                    "reviewId" to reviewId,
                    "reason" to "이미 삭제된 리뷰"
                )
            )
            throw NotFoundReviewException()
        }

        val report = reviewReportRepository.save(
            ReviewReport(
                review = review,
                memberId = reporterId,
                content = content,
                status = ReportStatus.PENDING
            )
        )

        log.info(
            LogFormat.message(
                domain = "REPORT",
                event = "신고 작성 완료",
                "reporterId" to reporterId,
                "reviewId" to reviewId,
                "reportId" to report.id
            )
        )

        return ReportCreateResponse(reportId = report.id!!)
    }
}