package com.example.team.haribo.goms.domain.studentcouncil.dto.request

import jakarta.validation.constraints.NotEmpty

data class DiscordStudentCouncilApplyRequest(
    @field:NotEmpty(message = "discordUserIds는 비어 있을 수 없습니다.")
    val discordUserIds: List<String>
)