package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenDeleteServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val memberUtil: MemberUtil
) {

    @Transactional
    fun delete(deviceId: String) {
        val memberId = memberUtil.currentMemberId()
        deviceTokenRepository.deleteByMember_IdAndDeviceId(memberId, deviceId)
    }
}