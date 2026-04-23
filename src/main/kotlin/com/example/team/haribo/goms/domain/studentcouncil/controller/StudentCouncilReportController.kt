package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.report.dto.request.ReportResolveRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportResolveResponse
import com.example.team.haribo.goms.domain.report.service.StudentCouncilPendingReportListService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilReportResolveService
import com.example.team.haribo.goms.domain.report.service.StudentCouncilResolvedReportListService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Student Council Report", description = "학생회 신고 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/student-council")
class StudentCouncilReportController(
    private val pendingReportListService: StudentCouncilPendingReportListService,
    private val resolvedReportListService: StudentCouncilResolvedReportListService,
    private val reportResolveService: StudentCouncilReportResolveService
) {

    @Operation(
        summary = "대기 중 신고 목록 조회",
        description = "처리되지 않은 신고 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/report/pending")
    fun pendingReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(pendingReportListService.getPendingReports())
    }

    @Operation(
        summary = "처리 완료 신고 목록 조회",
        description = "처리 완료된 신고 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/report/resolved")
    fun resolvedReportList(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(resolvedReportListService.getResolvedReports())
    }

    @Operation(
        summary = "신고 처리",
        description = "학생회가 신고를 승인 또는 반려 처리합니다.",
        parameters = [
            Parameter(
                name = "reportId",
                description = "신고 ID",
                required = true,
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = ReportResolveRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "신고 처리 성공")
        ]
    )
    @PatchMapping("/report/{reportId}")
    fun resolveReport(
        @PathVariable
        @Positive(message = "reportId 는 1 이상이어야 합니다.")
        reportId: Long,
        @Valid @RequestBody request: ReportResolveRequest
    ): ResponseEntity<ReportResolveResponse> {
        return ResponseEntity.ok(reportResolveService.resolve(reportId, request))
    }
}