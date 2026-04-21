package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.exception.NotFoundDeviceTokenException
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DeviceTokenDeleteServiceImplTest : DescribeSpec({

    val deviceTokenRepository = mockk<DeviceTokenRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = DeviceTokenDeleteServiceImpl(deviceTokenRepository, memberUtil)

    describe("DeviceTokenDeleteService") {

        context("Given: 디바이스 토큰이 존재함") {
            every { memberUtil.currentMemberId() } returns 1L
            every { deviceTokenRepository.deleteByMember_IdAndDeviceId(1L, "device-1") } returns 1L

            it("When: 삭제 요청 시 Then: 토큰이 삭제된다") {
                service.delete("device-1")
                verify(exactly = 1) { deviceTokenRepository.deleteByMember_IdAndDeviceId(1L, "device-1") }
            }
        }

        context("Given: 디바이스 토큰이 존재하지 않음") {
            every { memberUtil.currentMemberId() } returns 1L
            every { deviceTokenRepository.deleteByMember_IdAndDeviceId(1L, "device-404") } returns 0L

            it("When: 삭제 요청 시 Then: NotFoundDeviceTokenException이 발생한다") {
                shouldThrow<NotFoundDeviceTokenException> {
                    service.delete("device-404")
                }
            }
        }
    }
})