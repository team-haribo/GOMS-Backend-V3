package com.example.team.haribo.goms.domain.report.service.impl

import com.example.team.haribo.goms.domain.common.enums.ReportStatus
import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse
import com.example.team.haribo.goms.domain.report.exception.InvalidReportStatusException
import com.example.team.haribo.goms.domain.report.exception.NotFoundReportException
import com.example.team.haribo.goms.domain.report.exception.ReportAlreadyResolvedException
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.report.service.StudentCouncilReportResolveService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentCouncilReportResolveServiceImpl(
    private val reviewReportRepository: ReviewReportRepository,
    private val memberUtil: MemberUtil
) : StudentCouncilReportResolveService {

    @Transactional
    override fun resolve(reportId: Long, request: ReportResolveRequest): ReportResolveResponse {
        val resolver = memberUtil.currentMember()
        val resolverId = resolver.id!!
        val resolverName = resolver.name

        val report = reviewReportRepository.findByIdWithReviewAndReviewer(reportId)
            .orElseThrow { NotFoundReportException() }

        if (report.isResolved()) throw ReportAlreadyResolvedException()

        val status = request.reportStatus
        if (status == ReportStatus.PENDING) throw InvalidReportStatusException()

        report.resolve(status, resolverId)

        if (status == ReportStatus.APPROVED) {
            report.review.softDelete(resolverName)
        }

        return ReportResolveResponse(
            reportId = report.id!!,
            reviewId = report.review.id!!,
            reportStatus = report.status,
            resolvedAt = report.resolvedAt!!,
            resolvedBy = report.resolvedBy!!
        )
    }
}