package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse

interface DiscordStudentCouncilApplyService {

    fun apply(
        internalSecret: String?,
        discordUserIds: List<String>
    ): DiscordStudentCouncilApplyResponse
}