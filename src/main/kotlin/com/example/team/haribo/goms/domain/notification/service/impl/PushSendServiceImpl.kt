package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.PushSendService
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
        if (tokens.isEmpty()) return

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
            log.error("FCM 멀티캐스트 전송 중 오류가 발생했습니다.", e)
            return
        }

        if (response.failureCount == 0) return

        val invalidTokens = mutableListOf<String>()

        response.responses.forEachIndexed { index, r ->
            if (!r.isSuccessful) {
                invalidTokens.add(tokens[index])
            }
        }

        if (invalidTokens.isNotEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(invalidTokens)
        }
    }
}