package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.exception.NotFoundDeviceTokenException
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenDeleteService
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenDeleteServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val memberUtil: MemberUtil
) : DeviceTokenDeleteService {

    private val log = LoggerFactory.getLogger(DeviceTokenDeleteServiceImpl::class.java)

    @Transactional
    override fun delete(deviceId: String) {
        val memberId = memberUtil.currentMemberId()

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "디바이스 토큰 삭제 시도",
                "memberId" to memberId,
                "deviceId" to deviceId
            )
        )

        val deletedCount = deviceTokenRepository.deleteByMember_IdAndDeviceId(memberId, deviceId)

        if (deletedCount == 0L) {
            log.warn(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "디바이스 토큰 삭제 실패",
                    "memberId" to memberId,
                    "deviceId" to deviceId,
                    "reason" to "존재하지 않는 디바이스 토큰"
                )
            )
            throw NotFoundDeviceTokenException()
        }

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "디바이스 토큰 삭제 완료",
                "memberId" to memberId,
                "deviceId" to deviceId
            )
        )
    }
}