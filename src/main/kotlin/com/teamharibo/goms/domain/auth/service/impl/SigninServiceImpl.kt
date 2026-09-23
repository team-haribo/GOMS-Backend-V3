package com.teamharibo.goms.domain.auth.service.impl

import com.teamharibo.goms.domain.auth.dto.request.SigninRequest
import com.teamharibo.goms.domain.auth.dto.response.TokenResponse
import com.teamharibo.goms.domain.auth.exception.NotFoundEmailException
import com.teamharibo.goms.domain.auth.exception.PasswordMismatchException
import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.domain.auth.service.SigninService
import com.teamharibo.goms.domain.auth.util.AuthValidators
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProvider
import com.teamharibo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class SigninServiceImpl(
    private val memberRepository: MemberRepository,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : SigninService {

    private val log = LoggerFactory.getLogger(SigninServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun signin(request: SigninRequest): TokenResponse {
        AuthValidators.validateEmail(request.email)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "로그인 시도",
                "email" to request.email
            )
        )

        val member = memberRepository.findByEmail(request.email)
            .orElseThrow {
                log.warn(
                    LogFormat.message(
                        domain = "AUTH",
                        event = "로그인 실패",
                        "email" to request.email,
                        "reason" to "존재하지 않는 이메일"
                    )
                )
                NotFoundEmailException()
            }

        if (!passwordEncoder.matches(request.password, member.password)) {
            log.warn(
                LogFormat.message(
                    domain = "AUTH",
                    event = "로그인 실패",
                    "email" to request.email,
                    "reason" to "비밀번호 불일치"
                )
            )
            throw PasswordMismatchException()
        }

        val memberId = member.id ?: throw GlobalException(ErrorCode.NOT_FOUND_MEMBER)

        val accessToken = jwtProvider.createAccessToken(memberId, member.role.name)
        val refreshToken = jwtProvider.createRefreshToken(memberId)
        val accessExpiresAt = jwtProvider.getAccessExpirationDate()
        val refreshExpiresAt = jwtProvider.getRefreshExpirationDate()

        val refreshTtlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), refreshExpiresAt)
            .coerceAtLeast(1)

        refreshTokenRedisRepository.save(memberId, refreshToken, refreshTtlSeconds)

        log.info(
            LogFormat.message(
                domain = "AUTH",
                event = "로그인 성공",
                "memberId" to memberId,
                "email" to member.email,
                "role" to member.role
            )
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = accessExpiresAt,
            refreshTokenExpiresIn = refreshExpiresAt
        )
    }
}