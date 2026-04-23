package com.example.team.haribo.goms.domain.health.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/v3/health")
class HealthController {

    @Operation(
        summary = "헬스 체크",
        description = "서버가 정상 동작 중인지 확인합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "서버 정상")
        ]
    )
    @GetMapping
    fun health(): ResponseEntity<Void> {
        return ResponseEntity.ok().build()
    }
}