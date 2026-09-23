package com.teamharibo.goms.domain.report.service

import com.teamharibo.goms.domain.report.dto.request.ReportCreateRequest
import com.teamharibo.goms.domain.report.dto.response.ReportCreateResponse

interface ReportCreateService {
    fun create(reviewId: Long, request: ReportCreateRequest): ReportCreateResponse
}