package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.exception.NotFoundEmailException
import com.example.team.haribo.goms.domain.auth.exception.PasswordMismatchException
import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.auth.service.SigninService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
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

    @Transactional(readOnly = true)
    override fun signin(request: SigninRequest): TokenResponse {
        AuthValidators.validateEmail(request.email)

        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { NotFoundEmailException() }

        if (!passwordEncoder.matches(request.password, member.password)) {
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

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = accessExpiresAt,
            refreshTokenExpiresIn = refreshExpiresAt
        )
    }
}