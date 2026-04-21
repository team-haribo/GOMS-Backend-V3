package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.common.enums.Platform
import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.entity.DeviceToken
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class DeviceTokenRegisterServiceImplTest : DescribeSpec({

    val deviceTokenRepository = mockk<DeviceTokenRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = DeviceTokenRegisterServiceImpl(deviceTokenRepository, memberUtil)

    val member = MemberFixture.student(id = 1L)

    beforeTest {
        clearMocks(deviceTokenRepository, memberUtil)
    }

    describe("DeviceTokenRegisterService") {

        it("기존 member/deviceId 토큰이 없으면 새 토큰을 저장한다") {
            val request = DeviceTokenRegisterRequest(
                fcmToken = "token-1",
                platform = Platform.ANDROID,
                deviceId = "device-1"
            )

            every { memberUtil.currentMember() } returns member
            every { deviceTokenRepository.findByFcmToken("token-1") } returns null
            every { deviceTokenRepository.findByMember_IdAndDeviceId(1L, "device-1") } returns null
            every { deviceTokenRepository.save(any()) } answers { firstArg() }

            service.register(request)

            verify(exactly = 1) { deviceTokenRepository.save(any()) }
            verify(exactly = 0) { deviceTokenRepository.delete(any()) }
            verify(exactly = 0) { deviceTokenRepository.flush() }
        }

        it("동일 member/deviceId 토큰이 이미 존재하면 기존 토큰을 갱신한다") {
            val request = DeviceTokenRegisterRequest(
                fcmToken = "new-token",
                platform = Platform.IOS,
                deviceId = "device-1"
            )
            val existing = DeviceToken(
                member = member,
                deviceId = "device-1",
                fcmToken = "old-token",
                platform = Platform.ANDROID
            )

            every { memberUtil.currentMember() } returns member
            every { deviceTokenRepository.findByFcmToken("new-token") } returns null
            every { deviceTokenRepository.findByMember_IdAndDeviceId(1L, "device-1") } returns existing

            service.register(request)

            existing.fcmToken shouldBe "new-token"
            existing.platform shouldBe Platform.IOS

            verify(exactly = 0) { deviceTokenRepository.save(any()) }
            verify(exactly = 0) { deviceTokenRepository.delete(any()) }
            verify(exactly = 0) { deviceTokenRepository.flush() }
        }

        it("다른 사용자 또는 다른 deviceId가 같은 fcmToken을 사용 중이면 중복 토큰을 정리하고 새 토큰을 저장한다") {
            val request = DeviceTokenRegisterRequest(
                fcmToken = "duplicated-token",
                platform = Platform.ANDROID,
                deviceId = "device-1"
            )
            val otherToken = DeviceToken(
                member = MemberFixture.student(id = 2L),
                deviceId = "device-999",
                fcmToken = "duplicated-token",
                platform = Platform.IOS
            )

            every { memberUtil.currentMember() } returns member
            every { deviceTokenRepository.findByFcmToken("duplicated-token") } returns otherToken
            justRun { deviceTokenRepository.delete(otherToken) }
            justRun { deviceTokenRepository.flush() }
            every { deviceTokenRepository.findByMember_IdAndDeviceId(1L, "device-1") } returns null
            every { deviceTokenRepository.save(any()) } answers { firstArg() }

            service.register(request)

            verify(exactly = 1) { deviceTokenRepository.delete(otherToken) }
            verify(exactly = 1) { deviceTokenRepository.flush() }
            verify(exactly = 1) { deviceTokenRepository.save(any()) }
        }
    }
})