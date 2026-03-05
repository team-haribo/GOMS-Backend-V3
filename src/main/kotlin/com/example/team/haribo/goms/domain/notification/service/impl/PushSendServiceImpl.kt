package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.PushSendService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushSendServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository
) : PushSendService {

    @Transactional
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

        val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)

        if (response.failureCount == 0) return

        val invalidTokens = mutableListOf<String>()
        response.responses.forEachIndexed { index, r ->
            if (!r.isSuccessful) invalidTokens.add(tokens[index])
        }

        if (invalidTokens.isNotEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(invalidTokens)
        }
    }
}