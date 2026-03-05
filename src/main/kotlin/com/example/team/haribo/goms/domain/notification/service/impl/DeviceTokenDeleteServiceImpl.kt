package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenDeleteRequest
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
    override fun delete(request: DeviceTokenDeleteRequest) {

        val memberId = memberUtil.currentMemberId()

        val deleted = deviceTokenRepository
            .deleteByMember_IdAndDeviceId(memberId, request.deviceId)

        if (deleted == 0L) {
            throw NotFoundDeviceTokenException()
        }
    }
}