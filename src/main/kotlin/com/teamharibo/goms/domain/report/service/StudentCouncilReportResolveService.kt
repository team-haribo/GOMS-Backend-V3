package com.teamharibo.goms.domain.report.service

import com.teamharibo.goms.domain.report.dto.request.ReportResolveRequest
import com.teamharibo.goms.domain.report.dto.response.ReportResolveResponse

interface StudentCouncilReportResolveService {
    fun resolve(reportId: Long, request: ReportResolveRequest): ReportResolveResponse
}