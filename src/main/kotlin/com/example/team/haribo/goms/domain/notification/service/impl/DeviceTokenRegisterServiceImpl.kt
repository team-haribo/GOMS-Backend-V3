package com.example.team.haribo.goms.domain.notification.service.impl

import com.example.team.haribo.goms.domain.notification.dto.request.DeviceTokenRegisterRequest
import com.example.team.haribo.goms.domain.notification.entity.DeviceToken
import com.example.team.haribo.goms.domain.notification.repository.DeviceTokenRepository
import com.example.team.haribo.goms.domain.notification.service.DeviceTokenRegisterService
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenRegisterServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val memberUtil: MemberUtil
) : DeviceTokenRegisterService {

    private val log = LoggerFactory.getLogger(DeviceTokenRegisterServiceImpl::class.java)

    @Transactional
    override fun register(request: DeviceTokenRegisterRequest) {
        val member = memberUtil.currentMember()
        val memberId = member.id!!

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "디바이스 토큰 등록 시도",
                "memberId" to memberId,
                "deviceId" to request.deviceId,
                "platform" to request.platform
            )
        )

        deviceTokenRepository.findByFcmToken(request.fcmToken)?.let {
            if (it.member.id != memberId || it.deviceId != request.deviceId) {
                deviceTokenRepository.delete(it)
                deviceTokenRepository.flush()

                log.warn(
                    LogFormat.message(
                        domain = "NOTIFICATION",
                        event = "중복 토큰 정리",
                        "memberId" to memberId,
                        "deviceId" to request.deviceId
                    )
                )
            }
        }

        val existing = deviceTokenRepository.findByMember_IdAndDeviceId(memberId, request.deviceId)

        if (existing != null) {
            existing.update(request.fcmToken, request.platform)

            log.info(
                LogFormat.message(
                    domain = "NOTIFICATION",
                    event = "디바이스 토큰 갱신",
                    "memberId" to memberId,
                    "deviceId" to request.deviceId,
                    "platform" to request.platform
                )
            )
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

        log.info(
            LogFormat.message(
                domain = "NOTIFICATION",
                event = "디바이스 토큰 등록 완료",
                "memberId" to memberId,
                "deviceId" to request.deviceId,
                "platform" to request.platform
            )
        )
    }
}