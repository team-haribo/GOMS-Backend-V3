package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.common.enums.Platform
import com.example.team.haribo.goms.domain.notification.entity.DeviceToken
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class DeviceTokenQueryServiceImplTest : DescribeSpec({

    val deviceTokenRepository = mockk<DeviceTokenRepository>()
    val service = DeviceTokenQueryServiceImpl(deviceTokenRepository)

    describe("DeviceTokenQueryService") {

        context("Given: memberIds가 비어있음") {
            it("When: 토큰 조회 시 Then: 빈 리스트를 반환한다") {
                service.getTokensByMemberIds(emptyList()).shouldBeEmpty()
            }
        }

        context("Given: memberIds에 해당하는 토큰이 존재함") {
            val member = MemberFixture.student(id = 1L)
            val tokens = listOf(
                DeviceToken(
                    member = member,
                    deviceId = "device-1",
                    fcmToken = "token-1",
                    platform = Platform.ANDROID
                ),
                DeviceToken(
                    member = member,
                    deviceId = "device-2",
                    fcmToken = "token-2",
                    platform = Platform.IOS
                )
            )

            every { deviceTokenRepository.findAllByMember_IdIn(listOf(1L)) } returns tokens

            it("When: 토큰 조회 시 Then: fcmToken 목록을 반환한다") {
                val result = service.getTokensByMemberIds(listOf(1L))
                result shouldBe listOf("token-1", "token-2")
            }
        }
    }
})