package com.example.team.haribo.goms.domain.place.controller

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSyncResult
import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Place", description = "장소 관련 API")
@RestController
@RequestMapping("/api/v3/place")
class PlaceSyncController(
    private val placeSyncService: PlaceSyncService
) {

    @Operation(
        summary = "장소 동기화",
        description = "카카오 장소 정보를 동기화합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponse(responseCode = "200", description = "동기화 성공")
    @PostMapping("/sync")
    fun sync(): ResponseEntity<PlaceSyncResult> {
        return ResponseEntity.ok(placeSyncService.sync())
    }
}