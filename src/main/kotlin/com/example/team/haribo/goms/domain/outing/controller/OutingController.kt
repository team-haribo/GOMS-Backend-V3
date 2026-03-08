package com.example.team.haribo.goms.domain.outing.controller

import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.MyOutingStatusResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingCountResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentListResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.outing.service.MyOutingStatusService
import com.example.team.haribo.goms.domain.outing.service.OutingCountService
import com.example.team.haribo.goms.domain.outing.service.OutingStudentListService
import com.example.team.haribo.goms.domain.outing.service.OutingStudentSearchService
import com.example.team.haribo.goms.domain.outing.service.QrComingService
import com.example.team.haribo.goms.domain.outing.service.QrOutingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Outing", description = "외출 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v3/outing")
class OutingController(
    private val myOutingStatusService: MyOutingStatusService,
    private val qrOutingService: QrOutingService,
    private val qrComingService: QrComingService,
    private val outingStudentListService: OutingStudentListService,
    private val outingCountService: OutingCountService,
    private val outingStudentSearchService: OutingStudentSearchService
) {

    @Operation(
        summary = "내 외출 상태 조회",
        description = "현재 로그인한 사용자의 외출 상태를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<MyOutingStatusResponse> {
        return ResponseEntity.ok(myOutingStatusService.getStatus())
    }

    @Operation(
        summary = "QR 외출 처리",
        description = "QR 토큰을 사용해 외출 처리합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = QrToggleRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiJ9..."
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "외출 처리 성공"),
            ApiResponse(responseCode = "400", description = "QR 토큰 오류")
        ]
    )
    @PostMapping("/in")
    fun outing(@RequestBody request: QrToggleRequest): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(qrOutingService.outing(request))
    }

    @Operation(
        summary = "QR 복귀 처리",
        description = "QR 토큰을 사용해 복귀 처리합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = QrToggleRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiJ9..."
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "복귀 처리 성공"),
            ApiResponse(responseCode = "400", description = "QR 토큰 오류")
        ]
    )
    @PostMapping("/out")
    fun coming(@RequestBody request: QrToggleRequest): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(qrComingService.coming(request))
    }

    @Operation(
        summary = "현재 외출 중인 학생 목록 조회",
        description = "현재 외출 중인 학생 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/list")
    fun getOutingStudents(): ResponseEntity<OutingStudentListResponse> {
        return ResponseEntity.ok(outingStudentListService.getList())
    }

    @Operation(
        summary = "현재 외출 인원 수 조회",
        description = "현재 외출 중인 인원 수를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/count")
    fun getOutingCount(): ResponseEntity<OutingCountResponse> {
        return ResponseEntity.ok(outingCountService.getCount())
    }

    @Operation(
        summary = "외출 학생 이름 검색",
        description = "현재 외출 중인 학생을 이름으로 검색합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "검색 성공")
        ]
    )
    @GetMapping("/search")
    fun searchOutingStudents(@RequestParam("name") name: String?): ResponseEntity<OutingStudentListResponse> {
        return ResponseEntity.ok(outingStudentSearchService.search(name))
    }
}