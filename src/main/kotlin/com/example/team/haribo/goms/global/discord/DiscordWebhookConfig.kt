package com.example.team.haribo.goms.global.discord

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(DiscordWebhookProperties::class)
class DiscordWebhookConfig {

    @Bean
    fun webClient(): WebClient =
        WebClient.builder().build()
}