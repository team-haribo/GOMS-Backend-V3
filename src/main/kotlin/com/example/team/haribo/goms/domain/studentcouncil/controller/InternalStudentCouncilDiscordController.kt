package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.request.DiscordStudentCouncilApplyRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.DiscordStudentCouncilApplyService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v3/internal/student-council/discord")
class InternalStudentCouncilDiscordController(
    private val discordStudentCouncilApplyService: DiscordStudentCouncilApplyService
) {

    @PostMapping("/apply")
    fun apply(
        @RequestHeader("X-Internal-Secret", required = false) internalSecret: String?,
        @Valid @RequestBody request: DiscordStudentCouncilApplyRequest
    ): ResponseEntity<DiscordStudentCouncilApplyResponse> {
        return ResponseEntity.ok(
            discordStudentCouncilApplyService.apply(
                internalSecret = internalSecret,
                discordUserIds = request.discordUserIds
            )
        )
    }
}