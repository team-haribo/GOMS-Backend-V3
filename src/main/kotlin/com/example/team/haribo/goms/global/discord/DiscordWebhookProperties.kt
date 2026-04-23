package com.example.team.haribo.goms.global.discord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord")
data class DiscordWebhookProperties(
    val webhookUrl: String,
    val deployEnv: String
)