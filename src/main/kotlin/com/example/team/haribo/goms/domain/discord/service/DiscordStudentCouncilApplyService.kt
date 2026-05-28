package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse

interface DiscordStudentCouncilApplyService {

    fun apply(
        internalSecret: String?,
        discordUserIds: List<String>
    ): DiscordStudentCouncilApplyResponse
}