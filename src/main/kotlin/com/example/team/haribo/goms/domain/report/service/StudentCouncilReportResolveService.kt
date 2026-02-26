package com.example.team.haribo.goms.domain.report.service

import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse

interface StudentCouncilReportResolveService {
    fun resolve(reportId: Long, request: ReportResolveRequest): ReportResolveResponse
}