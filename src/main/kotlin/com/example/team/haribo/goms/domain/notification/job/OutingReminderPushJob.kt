package com.example.team.haribo.goms.domain.notification.job

import com.example.team.haribo.goms.domain.notification.service.DeviceTokenQueryService
import com.example.team.haribo.goms.domain.notification.service.OutingMemberQueryService
import com.example.team.haribo.goms.domain.notification.service.PushSendService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutingReminderPushJob(
    private val outingMemberQueryService: OutingMemberQueryService,
    private val deviceTokenQueryService: DeviceTokenQueryService,
    private val pushSendService: PushSendService
) {

    private val log = LoggerFactory.getLogger(OutingReminderPushJob::class.java)

    @Scheduled(cron = "0 15 17 * * MON,WED")
    fun sendOutingReminder() {
        val start = System.currentTimeMillis()

        log.info(
            LogFormat.message(
                domain = "SCHEDULER",
                event = "외출 알림 시작"
            )
        )

        val memberIds = outingMemberQueryService.getOutingMemberIds()
        val tokens = deviceTokenQueryService.getTokensByMemberIds(memberIds)

        log.info(
            LogFormat.message(
                domain = "SCHEDULER",
                event = "외출 알림 조회 완료",
                "outingMemberCount" to memberIds.size,
                "tokenCount" to tokens.size
            )
        )

        pushSendService.send(
            tokens = tokens,
            title = "복귀 시간 임박",
            body = "현재 외출 상태입니다. 복귀까지 10분 남았어요!"
        )

        val durationMs = System.currentTimeMillis() - start

        log.info(
            LogFormat.message(
                domain = "SCHEDULER",
                event = "외출 알림 완료",
                "outingMemberCount" to memberIds.size,
                "tokenCount" to tokens.size,
                "durationMs" to durationMs
            )
        )
    }
}