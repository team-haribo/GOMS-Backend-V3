package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse
import com.example.team.haribo.goms.domain.report.exception.InvalidReportStatusException
import com.example.team.haribo.goms.domain.report.exception.NotFoundReportException
import com.example.team.haribo.goms.domain.report.exception.ReportAlreadyResolvedException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.StudentCouncilReportResolveService
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilReportResolveServiceImpl(
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : StudentCouncilReportResolveService {

    private val log = LoggerFactory.getLogger(StudentCouncilReportResolveServiceImpl::class.java)

    @Transactional
    override fun resolve(reportId: Long, request: ReportResolveRequest): ReportResolveResponse {
        val resolver = memberUtil.currentMember()
        val resolverId = resolver.id!!
        val resolverName = resolver.name

        log.info(
            LogFormat.message(
                domain = "REPORT",
                event = "신고 처리 시도",
                "reportId" to reportId,
                "requestStatus" to request.reportStatus
            )
        )

        val report = reviewReportRepository.findByIdWithReviewAndReviewer(reportId)
            .orElseThrow {
                log.warn(
                    LogFormat.message(
                        domain = "REPORT",
                        event = "신고 처리 실패",
                        "reportId" to reportId,
                        "reason" to "존재하지 않는 신고"
                    )
                )
                NotFoundReportException()
            }

        if (report.isResolved()) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 처리 실패",
                    "reportId" to reportId,
                    "reason" to "이미 처리된 신고"
                )
            )
            throw ReportAlreadyResolvedException()
        }

        val status = request.reportStatus
        if (status == ReportStatus.PENDING) {
            log.warn(
                LogFormat.message(
                    domain = "REPORT",
                    event = "신고 처리 실패",
                    "reportId" to reportId,
                    "reason" to "유효하지 않은 처리 상태"
                )
            )
            throw InvalidReportStatusException()
        }

        report.resolve(status, resolverId)

        if (status == ReportStatus.APPROVED) {
            report.review.softDelete(resolverName)
        }

        log.info(
            LogFormat.message(
                domain = "REPORT",
                event = "신고 처리 완료",
                "reportId" to report.id,
                "reviewId" to report.review.id,
                "status" to report.status
            )
        )

        return ReportResolveResponse(
            reportId = report.id!!,
            reviewId = report.review.id!!,
            reportStatus = report.status,
            resolvedAt = report.resolvedAt!!,
            resolvedBy = report.resolvedBy!!
        )
    }
}