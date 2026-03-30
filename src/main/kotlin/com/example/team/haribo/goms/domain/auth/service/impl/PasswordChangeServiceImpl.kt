package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.PasswordChangeService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordChangeServiceImpl(
    private val memberRepository: MemberRepository,
    private val verifiedTokenRedisRepository: VerifiedTokenRedisRepository,
    private val passwordEncoder: PasswordEncoder
) : PasswordChangeService {

    private val log = LoggerFactory.getLogger(PasswordChangeServiceImpl::class.java)

    @Transactional
    override fun changePassword(request: PasswordChangeRequest) {
        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "비밀번호 변경 시도",
                "email" to request.email
            )
        )

        val member = memberRepository.findByEmail(request.email)
            .orElseThrow {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "비밀번호 변경 실패",
                        "email" to request.email,
                        "reason" to "존재하지 않는 사용자"
                    )
                )
                GlobalException(ErrorCode.NOT_FOUND_MEMBER)
            }

        val storedVerifiedToken = verifiedTokenRedisRepository.find(request.email, Purpose.PASSWORD_CHANGE)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "비밀번호 변경 실패",
                        "email" to request.email,
                        "reason" to "유효하지 않은 verified token"
                    )
                )
                throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
            }

        if (storedVerifiedToken != request.verifiedToken) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "비밀번호 변경 실패",
                    "email" to request.email,
                    "reason" to "verified token 불일치"
                )
            )
            throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
        }

        AuthValidators.validatePassword(request.newPassword)

        val encoded = passwordEncoder.encode(request.newPassword)
            ?: throw GlobalException(ErrorCode.INVALID_PASSWORD_POLICY)

        member.password = encoded
        verifiedTokenRedisRepository.delete(request.email, Purpose.PASSWORD_CHANGE)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "비밀번호 변경 완료",
                "memberId" to member.id,
                "email" to member.email
            )
        )
    }
}