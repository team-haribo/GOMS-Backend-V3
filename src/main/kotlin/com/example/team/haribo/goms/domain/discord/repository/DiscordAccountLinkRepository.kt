package com.example.team.haribo.goms.domain.discord.repository

import com.example.team.haribo.goms.domain.discord.entity.DiscordAccountLink
import org.springframework.data.jpa.repository.JpaRepository

interface DiscordAccountLinkRepository : JpaRepository<DiscordAccountLink, Long> {

    fun findAllByDiscordUserIdIn(discordUserIds: Collection<String>): List<DiscordAccountLink>
}