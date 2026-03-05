package com.example.team.haribo.goms.domain.notification.repository

import com.example.team.haribo.goms.domain.notification.entity.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {

    fun findByMember_IdAndDeviceId(memberId: Long, deviceId: String): DeviceToken?

    fun findByFcmToken(fcmToken: String): DeviceToken?

    fun deleteByMember_IdAndDeviceId(memberId: Long, deviceId: String): Long

    fun findAllByMember_Id(memberId: Long): List<DeviceToken>

    fun findAllByMember_IdIn(memberIds: List<Long>): List<DeviceToken>

    fun deleteAllByFcmTokenIn(tokens: List<String>)
}