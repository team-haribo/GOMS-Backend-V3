package com.example.team.haribo.goms.domain.place.scheduler

import com.example.team.haribo.goms.domain.place.service.PlaceSyncService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class PlaceSyncScheduler(
    private val placeSyncService: PlaceSyncService
) {

    private val log = LoggerFactory.getLogger(PlaceSyncScheduler::class.java)

    @Scheduled(cron = "0 0 4 */7 * *", zone = "Asia/Seoul")
    fun syncPlaces() {
        log.info(
            LogFormat.message(
                domain = "SCHEDULER",
                event = "장소 동기화 시작",
                "job" to "PLACE_SYNC"
            )
        )

        try {
            lateinit var result: Any

            val durationMs = measureTimeMillis {
                result = placeSyncService.sync()
            }

            log.info(
                LogFormat.message(
                    domain = "SCHEDULER",
                    event = "장소 동기화 완료",
                    "job" to "PLACE_SYNC",
                    "durationMs" to durationMs,
                    "result" to result
                )
            )
        } catch (e: Exception) {
            log.error(
                LogFormat.message(
                    domain = "SCHEDULER",
                    event = "장소 동기화 실패",
                    "job" to "PLACE_SYNC",
                    "exceptionType" to e::class.simpleName,
                    "message" to e.message
                ),
                e
            )
        }
    }
}