package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.entity.AuthRefreshToken
import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.auth.service.SigninService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SigninServiceImpl(
    private val memberRepository: MemberRepository,
    private val refreshTokenRepository: AuthRefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : SigninService {

    @Transactional
    override fun signin(request: SigninRequest): TokenResponse {
        AuthValidators.validateEmail(request.email)

        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { GlobalException(ErrorCode.INVALID_CREDENTIALS) }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw GlobalException(ErrorCode.INVALID_CREDENTIALS)
        }

        val accessToken = jwtProvider.createAccessToken(member.id, member.role.authority)
        val refreshToken = jwtProvider.createRefreshToken(member.id)

        val refreshExpiresAt = jwtProvider.getRefreshExpirationDate()

        val entity = refreshTokenRepository.findByMemberId(member.id).orElse(null)
            ?.apply {
                this.refreshToken = refreshToken
                this.expiresAt = refreshExpiresAt
                this.revokedAt = null
            }
            ?: AuthRefreshToken(
                memberId = member.id,
                refreshToken = refreshToken,
                expiresAt = refreshExpiresAt
            )

        refreshTokenRepository.save(entity)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProvider.getAccessExpirationDate(),
            refreshTokenExpiresIn = jwtProvider.getRefreshExpirationDate()
        )
    }
}
