package com.example.team.haribo.goms.global.discord

data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int,
    val fields: List<DiscordField>
)