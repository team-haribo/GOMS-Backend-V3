package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilLateListService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Student Council Late", description = "학생회 지각 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilLateController(
    private val lateListService: StudentCouncilLateListService
) {

    @Operation(
        summary = "지각 학생 목록 조회",
        description = "특정 날짜 기준 지각 학생 목록을 조회합니다. date가 없으면 오늘 기준으로 조회합니다.",
        parameters = [
            Parameter(
                name = "date",
                description = "조회 날짜",
                required = false,
                example = "2026-03-27"
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/late")
    fun listLate(
        @RequestParam("date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<LateStudentsListResponse> {
        return ResponseEntity.ok(lateListService.list(date))
    }
}