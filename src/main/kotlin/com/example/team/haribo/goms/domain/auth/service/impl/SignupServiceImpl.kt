package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest
import com.example.team.haribo.goms.domain.auth.repository.redis.VerifiedTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.SignupService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupServiceImpl(
    private val memberRepository: MemberRepository,
    private val verifiedTokenRedisRepository: VerifiedTokenRedisRepository,
    private val passwordEncoder: PasswordEncoder
) : SignupService {

    private val log = LoggerFactory.getLogger(SignupServiceImpl::class.java)

    @Transactional
    override fun signup(request: SignupRequest) {
        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "회원가입 시도",
                "email" to request.email,
                "name" to request.name,
                "grade" to request.grade,
                "department" to request.department
            )
        )

        if (memberRepository.existsByEmail(request.email)) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "회원가입 실패",
                    "email" to request.email,
                    "reason" to "이미 가입된 이메일"
                )
            )
            throw GlobalException(ErrorCode.ALREADY_REGISTERED_EMAIL)
        }

        val storedVerifiedToken = verifiedTokenRedisRepository.find(request.email, Purpose.SIGNUP)
            ?: run {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "회원가입 실패",
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
                    event = "회원가입 실패",
                    "email" to request.email,
                    "reason" to "verified token 불일치"
                )
            )
            throw GlobalException(ErrorCode.INVALID_VERIFIED_TOKEN)
        }

        AuthValidators.validatePassword(request.password)

        val encoded = passwordEncoder.encode(request.password)
            ?: throw GlobalException(ErrorCode.INVALID_PASSWORD_POLICY)

        val member = Member(
            email = request.email,
            password = encoded,
            name = request.name,
            grade = request.grade.toInt(),
            department = request.department,
            gender = request.gender,
            role = Role.ROLE_STUDENT
        )

        memberRepository.save(member)
        verifiedTokenRedisRepository.delete(request.email, Purpose.SIGNUP)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "회원가입 완료",
                "memberId" to member.id,
                "email" to member.email,
                "grade" to member.grade,
                "department" to member.department,
                "role" to member.role
            )
        )
    }
}