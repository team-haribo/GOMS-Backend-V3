package com.example.team.haribo.goms.global.discord

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ServerLifecycleNotifier(
    private val discordWebhookService: DiscordWebhookService
) {
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        discordWebhookService.sendServerStarted()
    }

    @EventListener(ContextClosedEvent::class)
    fun onApplicationClosed() {
        discordWebhookService.sendServerStopped()
    }
}