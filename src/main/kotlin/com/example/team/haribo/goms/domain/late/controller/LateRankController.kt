package com.example.team.haribo.goms.domain.late.controller

import com.example.team.haribo.goms.domain.late.dto.response.LateRankListResponse
import com.example.team.haribo.goms.domain.late.service.LateRankService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/late")
class LateRankController(
    private val lateRankService: LateRankService
) {

    @GetMapping("/rank")
    fun getRank(): ResponseEntity<LateRankListResponse> {
        return ResponseEntity.ok(lateRankService.getTop5())
    }
}
