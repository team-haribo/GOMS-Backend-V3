package com.example.team.haribo.goms.domain.notification.job

import com.example.team.haribo.goms.domain.notification.service.DeviceTokenQueryService
import com.example.team.haribo.goms.domain.notification.service.OutingMemberQueryService
import com.example.team.haribo.goms.domain.notification.service.PushSendService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutingReminderPushJob(
    private val outingMemberQueryService: OutingMemberQueryService,
    private val deviceTokenQueryService: DeviceTokenQueryService,
    private val pushSendService: PushSendService
) {

    @Scheduled(cron = "0 15 17 * * MON,WED")
    fun sendOutingReminder() {
        val memberIds = outingMemberQueryService.getOutingMemberIds()
        val tokens = deviceTokenQueryService.getTokensByMemberIds(memberIds)

        pushSendService.send(
            tokens = tokens,
            title = "복귀 시간 임밤",
            body = "현재 외출 상태입니다. 복귀까지 10분 남았어요!"
        )
    }
}