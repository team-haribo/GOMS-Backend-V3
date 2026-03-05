package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.entity.DeviceToken
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenRegisterServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val memberUtil: MemberUtil
) {

    @Transactional
    fun register(request: DeviceTokenRegisterRequest) {
        val member = memberUtil.currentMember()
        val memberId = memberUtil.currentMemberId()

        deviceTokenRepository.findByFcmToken(request.fcmToken)?.let {
            if (it.member.id != memberId || it.deviceId != request.deviceId) {
                deviceTokenRepository.delete(it)
                deviceTokenRepository.flush()
            }
        }

        val existing = deviceTokenRepository.findByMember_IdAndDeviceId(memberId, request.deviceId)

        if (existing != null) {
            existing.update(request.fcmToken, request.platform)
            return
        }

        deviceTokenRepository.save(
            DeviceToken(
                member = member,
                deviceId = request.deviceId,
                fcmToken = request.fcmToken,
                platform = request.platform
            )
        )
    }
}