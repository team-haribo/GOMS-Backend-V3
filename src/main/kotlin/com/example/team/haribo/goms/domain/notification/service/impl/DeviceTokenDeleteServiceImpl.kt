package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.exception.NotFoundDeviceTokenException
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenDeleteService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenDeleteServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val memberUtil: MemberUtil
) : DeviceTokenDeleteService {

    @Transactional
    override fun delete(deviceId: String) {
        val memberId = memberUtil.currentMemberId()
        val token = deviceTokenRepository.findByMember_IdAndDeviceId(memberId, deviceId)
            ?: throw NotFoundDeviceTokenException()

        deviceTokenRepository.delete(token)
    }
}