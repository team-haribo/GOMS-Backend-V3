package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.PushSendService
import com.example.team.haribo.goms.global.log.LogFormat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushSendServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository
) : PushSendService {

    private val log = LoggerFactory.getLogger(PushSendServiceImpl::class.java)

    override fun send(tokens: List<String>, title: String, body: String) {
        if (tokens.isEmpty()) {
            log.info(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "푸시 발송 스킵",
                    "title" to title,
                    "reason" to "대상 토큰 없음"
                )
            )
            return
        }

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "푸시 발송 시작",
                "title" to title,
                "targetCount" to tokens.size
            )
        )

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .addAllTokens(tokens)
            .build()

        val response = try {
            FirebaseMessaging.getInstance().sendEachForMulticast(message)
        } catch (e: FirebaseMessagingException) {
            log.error(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "푸시 발송 실패",
                    "title" to title,
                    "reason" to "Firebase 오류"
                ),
                e
            )
            return
        }

        if (response.failureCount == 0) {
            log.info(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "푸시 발송 완료",
                    "title" to title,
                    "targetCount" to tokens.size,
                    "successCount" to response.successCount,
                    "failureCount" to response.failureCount
                )
            )
            return
        }

        val invalidTokens = mutableListOf<String>()

        response.responses.forEachIndexed { index, r ->
            if (!r.isSuccessful) {
                invalidTokens.add(tokens[index])
            }
        }

        if (invalidTokens.isNotEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(invalidTokens)

            log.warn(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "유효하지 않은 토큰 정리",
                    "count" to invalidTokens.size
                )
            )
        }

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "푸시 발송 완료",
                "title" to title,
                "targetCount" to tokens.size,
                "successCount" to response.successCount,
                "failureCount" to response.failureCount
            )
        )
    }
}