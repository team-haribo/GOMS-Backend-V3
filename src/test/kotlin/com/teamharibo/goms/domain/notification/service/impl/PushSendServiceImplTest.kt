package com.teamharibo.goms.domain.notification.service.impl

import com.teamharibo.goms.domain.notification.repository.DeviceTokenRepository
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.SendResponse
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify

class PushSendServiceImplTest : DescribeSpec({

    val deviceTokenRepository = mockk<DeviceTokenRepository>(relaxed = true)
    val firebaseMessaging = mockk<FirebaseMessaging>()
    val service = PushSendServiceImpl(deviceTokenRepository)

    fun successResponse(): SendResponse {
        val response = mockk<SendResponse>()
        every { response.isSuccessful } returns true
        every { response.exception } returns null
        return response
    }

    fun failureResponse(errorCode: MessagingErrorCode?): SendResponse {
        val exception = mockk<FirebaseMessagingException>()
        every { exception.messagingErrorCode } returns errorCode
        val response = mockk<SendResponse>()
        every { response.isSuccessful } returns false
        every { response.exception } returns exception
        return response
    }

    fun batchResponse(vararg responses: SendResponse): BatchResponse {
        val batch = mockk<BatchResponse>()
        every { batch.responses } returns responses.toList()
        every { batch.successCount } returns responses.count { it.isSuccessful }
        every { batch.failureCount } returns responses.count { !it.isSuccessful }
        return batch
    }

    beforeEach {
        clearMocks(deviceTokenRepository, firebaseMessaging)
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
            it("When: 푸시 발송 시 Then: 토큰 삭제 없이 종료된다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    successResponse(),
                    successResponse()
                )

                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 1) { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) }
                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: 일부 토큰이 UNREGISTERED로 실패") {
            it("When: 푸시 발송 시 Then: UNREGISTERED 토큰만 삭제한다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    successResponse(),
                    failureResponse(MessagingErrorCode.UNREGISTERED)
                )

                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 1) { deviceTokenRepository.deleteAllByFcmTokenIn(listOf("token-2")) }
            }
        }

        context("Given: 일부 토큰이 SENDER_ID_MISMATCH로 실패") {
            it("When: 푸시 발송 시 Then: SENDER_ID_MISMATCH 토큰을 삭제한다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    failureResponse(MessagingErrorCode.SENDER_ID_MISMATCH),
                    successResponse()
                )

                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 1) { deviceTokenRepository.deleteAllByFcmTokenIn(listOf("token-1")) }
            }
        }

        context("Given: 모든 실패가 일시 오류") {
            it("When: 푸시 발송 시 Then: 토큰을 삭제하지 않는다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    failureResponse(MessagingErrorCode.UNAVAILABLE),
                    failureResponse(MessagingErrorCode.INTERNAL),
                    failureResponse(MessagingErrorCode.QUOTA_EXCEEDED),
                    failureResponse(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)
                )

                service.send(listOf("token-1", "token-2", "token-3", "token-4"), "제목", "본문")

                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: INVALID_ARGUMENT로 실패") {
            it("When: 푸시 발송 시 Then: payload 오류 가능성 때문에 토큰을 보존한다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    failureResponse(MessagingErrorCode.INVALID_ARGUMENT),
                    failureResponse(MessagingErrorCode.INVALID_ARGUMENT)
                )

                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: 실패 응답에 오류 코드가 없음") {
            it("When: 푸시 발송 시 Then: 토큰을 보존한다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    failureResponse(null)
                )

                service.send(listOf("token-1"), "제목", "본문")

                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }

        context("Given: 영구 무효와 일시 오류가 섞여 실패") {
            it("When: 푸시 발송 시 Then: 영구 무효 토큰만 삭제한다") {
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } returns batchResponse(
                    failureResponse(MessagingErrorCode.UNAVAILABLE),
                    successResponse(),
                    failureResponse(MessagingErrorCode.UNREGISTERED),
                    failureResponse(MessagingErrorCode.INTERNAL),
                    failureResponse(MessagingErrorCode.UNREGISTERED)
                )

                service.send(listOf("token-1", "token-2", "token-3", "token-4", "token-5"), "제목", "본문")

                verify(exactly = 1) { deviceTokenRepository.deleteAllByFcmTokenIn(listOf("token-3", "token-5")) }
            }
        }

        context("Given: Firebase 요청 자체가 실패") {
            it("When: 푸시 발송 시 Then: 예외를 전파하지 않고 토큰을 보존한다") {
                val exception = mockk<FirebaseMessagingException>()
                every { exception.message } returns "Firebase 요청 실패"
                every { exception.localizedMessage } returns "Firebase 요청 실패"
                every { exception.cause } returns null
                every { exception.stackTrace } returns emptyArray()
                every { exception.suppressed } returns emptyArray()
                every { firebaseMessaging.sendEachForMulticast(any<MulticastMessage>()) } throws exception

                service.send(listOf("token-1", "token-2"), "제목", "본문")

                verify(exactly = 0) { deviceTokenRepository.deleteAllByFcmTokenIn(any()) }
            }
        }
    }
})