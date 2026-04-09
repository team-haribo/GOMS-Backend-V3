package com.example.team.haribo.goms.domain.place.scheduler

import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PlaceSyncScheduler(
    private val placeSyncService: PlaceSyncService
) {
    @Scheduled(cron = "0 0 4 */7 * *", zone = "Asia/Seoul")
    fun syncPlaces() {
        placeSyncService.sync()
    }
}