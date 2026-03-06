package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenQueryServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository
) : DeviceTokenQueryService {

    @Transactional(readOnly = true)
    override fun getTokensByMemberIds(memberIds: List<Long>): List<String> {
        if (memberIds.isEmpty()) return emptyList()

        return deviceTokenRepository.findAllByMember_IdIn(memberIds)
            .map { it.fcmToken }
    }
}