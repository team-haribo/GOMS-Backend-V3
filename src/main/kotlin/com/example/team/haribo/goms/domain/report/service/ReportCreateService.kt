package com.example.team.haribo.goms.domain.report.service

import com.example.team.haribo.goms.domain.report.dto.request.ReportCreateRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportCreateResponse

interface ReportCreateService {
    fun create(reviewId: Long, request: ReportCreateRequest): ReportCreateResponse
}