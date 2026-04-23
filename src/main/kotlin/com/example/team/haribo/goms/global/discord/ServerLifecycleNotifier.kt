package com.example.team.haribo.goms.global.discord

import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ServerLifecycleNotifier(
    private val discordWebhookService: DiscordWebhookService
) {

    private val log = LoggerFactory.getLogger(ServerLifecycleNotifier::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        log.info(
            LogFormat.message(
                domain = "SYSTEM",
                event = "서버 시작 완료"
            )
        )
        discordWebhookService.sendServerStarted()
    }

    @EventListener(ContextClosedEvent::class)
    fun onApplicationClosed() {
        log.info(
            LogFormat.message(
                domain = "SYSTEM",
                event = "서버 종료 감지"
            )
        )
        discordWebhookService.sendServerStopped()
    }
}