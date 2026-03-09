package com.example.team.haribo.goms.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOriginPatterns: List<String> = emptyList()
)