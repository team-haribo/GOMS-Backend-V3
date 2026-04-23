package com.example.team.haribo.goms.domain.report.controller

import com.example.team.haribo.goms.domain.report.dto.request.ReportCreateRequest
import com.example.team.haribo.goms.domain.report.dto.response.ReportCreateResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportDetailResponse
import com.example.team.haribo.goms.domain.report.dto.response.ReportListResponse
import com.example.team.haribo.goms.domain.report.service.ReportCreateService
import com.example.team.haribo.goms.domain.report.service.ReportDetailService
import com.example.team.haribo.goms.domain.report.service.ReportMyListService
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Report", description = "리뷰 신고 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/report")
class ReportController(
    private val reportCreateService: ReportCreateService,
    private val reportMyListService: ReportMyListService,
    private val reportDetailService: ReportDetailService
) {

    @Operation(
        summary = "리뷰 신고 생성",
        description = "특정 리뷰에 대한 신고를 생성합니다.",
        parameters = [
            Parameter(
                name = "reviewId",
                description = "신고할 리뷰 ID",
                required = true,
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = ReportCreateRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "신고 생성 성공")
        ]
    )
    @PostMapping("/{reviewId}")
    fun create(
        @PathVariable
        @Positive(message = "reviewId 는 1 이상이어야 합니다.")
        reviewId: Long,
        @Valid @RequestBody request: ReportCreateRequest
    ): ResponseEntity<ReportCreateResponse> {
        return ResponseEntity.status(201).body(reportCreateService.create(reviewId, request))
    }

    @Operation(
        summary = "내 신고 목록 조회",
        description = "현재 로그인한 사용자의 신고 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/my")
    fun myReports(): ResponseEntity<ReportListResponse> {
        return ResponseEntity.ok(reportMyListService.getMyReports())
    }

    @Operation(
        summary = "신고 상세 조회",
        description = "reportId 기준으로 신고 상세를 조회합니다.",
        parameters = [
            Parameter(
                name = "reportId",
                description = "신고 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/{reportId}")
    fun reportDetail(
        @PathVariable
        @Positive(message = "reportId 는 1 이상이어야 합니다.")
        reportId: Long
    ): ResponseEntity<ReportDetailResponse> {
        return ResponseEntity.ok(reportDetailService.getReportDetail(reportId))
    }
}