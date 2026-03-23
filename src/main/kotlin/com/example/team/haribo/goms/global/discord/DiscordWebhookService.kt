package com.example.team.haribo.goms.global.discord

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class DiscordWebhookService(
    private val webClient: WebClient,
    private val discordWebhookProperties: DiscordWebhookProperties,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    fun sendServerStarted() {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val date = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN))
        val time = now.format(DateTimeFormatter.ofPattern("a hh시 mm분 ss초", Locale.KOREAN))

        val payload = mapOf(
            "embeds" to listOf(
                mapOf(
                    "title" to "🚀 서버 시작됨",
                    "description" to "$applicationName 서버 애플리케이션이 정상적으로 시작되었습니다.",
                    "color" to 5793266,
                    "fields" to listOf(
                        mapOf(
                            "name" to "환경",
                            "value" to discordWebhookProperties.deployEnv,
                            "inline" to false
                        ),
                        mapOf(
                            "name" to "날짜",
                            "value" to date,
                            "inline" to false
                        ),
                        mapOf(
                            "name" to "시간",
                            "value" to time,
                            "inline" to false
                        )
                    )
                )
            )
        )

        webClient.post()
            .uri(discordWebhookProperties.webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    fun sendServerStopped() {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val date = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN))
        val time = now.format(DateTimeFormatter.ofPattern("a hh시 mm분 ss초", Locale.KOREAN))

        val payload = mapOf(
            "embeds" to listOf(
                mapOf(
                    "title" to "🛑 서버 종료됨",
                    "description" to "$applicationName 서버 애플리케이션이 종료되었습니다.",
                    "color" to 15548997,
                    "fields" to listOf(
                        mapOf(
                            "name" to "환경",
                            "value" to discordWebhookProperties.deployEnv,
                            "inline" to false
                        ),
                        mapOf(
                            "name" to "날짜",
                            "value" to date,
                            "inline" to false
                        ),
                        mapOf(
                            "name" to "시간",
                            "value" to time,
                            "inline" to false
                        )
                    )
                )
            )
        )

        webClient.post()
            .uri(discordWebhookProperties.webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .block()
    }
}