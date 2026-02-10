package com.example.team.haribo.goms.GomsServerV3.global.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessExpSeconds: Long,
    val refreshExpSeconds: Long
)
