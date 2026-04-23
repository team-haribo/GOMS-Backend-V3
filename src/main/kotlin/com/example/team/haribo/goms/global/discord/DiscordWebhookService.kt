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
        sendServerLifecycleWebhook(
            title = "🚀 서버 시작됨",
            description = "$applicationName 서버 애플리케이션이 정상적으로 시작되었습니다.",
            color = STARTED_COLOR
        )
    }

    fun sendServerStopped() {
        sendServerLifecycleWebhook(
            title = "🛑 서버 종료됨",
            description = "$applicationName 서버 애플리케이션이 종료되었습니다.",
            color = STOPPED_COLOR
        )
    }

    private fun sendServerLifecycleWebhook(
        title: String,
        description: String,
        color: Int
    ) {
        val now = ZonedDateTime.now(KOREA_ZONE_ID)
        val payload = DiscordWebhookRequest(
            embeds = listOf(
                DiscordEmbed(
                    title = title,
                    description = description,
                    color = color,
                    fields = createFields(now)
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

    private fun createFields(now: ZonedDateTime): List<DiscordField> {
        val date = now.format(DATE_FORMATTER)
        val time = now.format(TIME_FORMATTER)

        return listOf(
            DiscordField(
                name = "환경",
                value = discordWebhookProperties.deployEnv,
                inline = false
            ),
            DiscordField(
                name = "날짜",
                value = date,
                inline = false
            ),
            DiscordField(
                name = "시간",
                value = time,
                inline = false
            )
        )
    }

    companion object {
        private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN)
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("a hh시 mm분 ss초", Locale.KOREAN)

        private const val STARTED_COLOR = 5793266
        private const val STOPPED_COLOR = 15548997
    }
}