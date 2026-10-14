package com.teamharibo.goms.domain.notification.service

interface DeviceTokenQueryService {
    fun getTokensByMemberIds(memberIds: List<Long>): List<String>
}