package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.SendResponse
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify

class PushSendServiceImplTest : DescribeSpec({

    val deviceTokenRepository = mockk<DeviceTokenRepository>(relaxed = true)
    val firebaseMessaging = mockk<FirebaseMessaging>()
    val service = PushSendServiceImpl(deviceTokenRepository)

    beforeEach {
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns firebaseMessaging
    }

    afterEach {
        unmockkStatic(FirebaseMessaging::class)
    }

    describe("PushSendService") {

        context("Given: 대상 토큰이 없음") {
            it("When: 푸시 발송 시 Then: Firebase를 호출하지 않는다") {
                service.send(emptyList(), "제목", "본문")

                verify(exactly = 0) { FirebaseMessaging.getInstance() }
                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: 모든 토큰 발송 성공") {
            val response = mockk<BatchResponse>()
            every { response.failureCount } returns 0
            every { response.successCount } returns 2
            every { response.responses } returns emptyList()

            every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns response

            it("When: 푸시 발송 시 Then: 토큰 삭제 없이 종료된다") {
                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 1) { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) }
                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: 일부 토큰 발송 실패") {
            val success = mockk<SendResponse>()
            val failure = mockk<SendResponse>()
            val response = mockk<BatchResponse>()

            every { success.isSuccessful } returns true
            every { failure.isSuccessful } returns false

            every { response.failureCount } returns 1
            every { response.successCount } returns 1
            every { response.responses } returns listOf(success, failure)

            every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns response

            it("When: 푸시 발송 시 Then: 실패한 토큰을 삭제한다") {
                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 1) { deviceTokenRepository.deleteAllByFcmTokenIn(listOf("token-2")) }
            }
        }
    }
})