package com.example.team.haribo.goms.domain.report.controller

import com.example.team.haribo.goms.domain.report.dto.request.ReportCreateRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportCreateResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportDetailResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.service.ReportCreateService
import com.example.team.haribo.goms.domain.report.service.ReportDetailService
import com.example.team.haribo.goms.domain.report.service.ReportMyListService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/report")
class ReportController(
    private val reportCreateService: ReportCreateService,
    private val reportMyListService: ReportMyListService,
    private val reportDetailService: ReportDetailService
) {

    @PostMapping("/{reviewId}")
    fun create(
        @PathVariable reviewId: Long,
        @RequestBody request: ReportCreateRequest
    ): ResponseEntity<ReportCreateResponse> {
        return ResponseEntity.status(201).body(reportCreateService.create(reviewId, request))
    }

    @GetMapping("/my")
    fun myReports(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(reportMyListService.getMyReports())
    }

    @GetMapping("/{reportId}")
    fun reportDetail(@PathVariable reportId: Long): ResponseEntity<ReportDetailResponse> {
        return ResponseEntity.ok(reportDetailService.getReportDetail(reportId))
    }
}