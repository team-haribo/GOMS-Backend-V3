package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceInService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceOutService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Student Council Outing", description = "학생회 외출 상태 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/student-council")
class StudentCouncilOutingController(
    private val forceOutService: StudentCouncilForceOutService,
    private val forceInService: StudentCouncilForceInService
) {

    @Operation(
        summary = "학생 강제 외출 처리",
        description = "학생회가 특정 학생을 강제로 외출 처리합니다.",
        parameters = [
            Parameter(
                name = "memberId",
                description = "학생 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "강제 외출 처리 성공")
        ]
    )
    @PostMapping("/status/out/{memberId}")
    fun forceOut(
        @PathVariable
        @Positive(message = "memberId 는 1 이상이어야 합니다.")
        memberId: Long
    ): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(forceOutService.out(memberId))
    }

    @Operation(
        summary = "학생 강제 복귀 처리",
        description = "학생회가 특정 학생을 강제로 복귀 처리합니다.",
        parameters = [
            Parameter(
                name = "memberId",
                description = "학생 ID",
                required = true,
                example = "1"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "강제 복귀 처리 성공")
        ]
    )
    @PostMapping("/status/in/{memberId}")
    fun forceIn(
        @PathVariable
        @Positive(message = "memberId 는 1 이상이어야 합니다.")
        memberId: Long
    ): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(forceInService.`in`(memberId))
    }
}