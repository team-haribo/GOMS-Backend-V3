package com.example.team.haribo.goms.domain.late.controller

import com.example.team.haribo.goms.domain.late.dto.response.LateRankListResponse
import com.example.team.haribo.goms.domain.late.service.LateRankService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Late", description = "지각 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v3/late")
class LateRankController(
    private val lateRankService: LateRankService
) {

    @Operation(
        summary = "지각 랭킹 조회",
        description = "지각 횟수 상위 5명의 랭킹을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @GetMapping("/rank")
    fun getRank(): ResponseEntity<LateRankListResponse> {
        return ResponseEntity.ok(lateRankService.getTop5())
    }
}