package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.entity.AuthRefreshToken
import com.example.team.haribo.goms.domain.auth.exception.*
import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.auth.repository.EmailVerificationRepository
import com.example.team.haribo.goms.domain.auth.service.AuthService
import com.example.team.haribo.goms.domain.auth.util.AuthValidators
import com.example.team.haribo.goms.domain.common.enums.Purpose
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthServiceImpl(
    private val memberRepository: MemberRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val refreshTokenRepository: AuthRefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : AuthService {

    @Transactional
    override fun signup(request: SignupRequest) {
        if (memberRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException()
        }

        val verification = emailVerificationRepository.findByEmailAndPurpose(request.email, Purpose.SIGNUP)
            .orElseThrow { InvalidVerifiedTokenException() }

        if (verification.verifiedToken == null ||
            verification.verifiedToken != request.verifiedToken ||
            verification.verifiedTokenExpiresAt == null ||
            verification.verifiedTokenExpiresAt!!.isBefore(LocalDateTime.now())
        ) {
            throw InvalidVerifiedTokenException()
        }

        AuthValidators.validatePassword(request.password)

        val encoded = passwordEncoder.encode(request.password) ?: throw InvalidPasswordPolicyException()

        memberRepository.save(
            Member(
                email = request.email,
                password = encoded,
                name = request.name,
                grade = request.grade,
                department = request.department,
                gender = request.gender
            )
        )
    }

    @Transactional
    override fun signin(request: SigninRequest): TokenResponse {
        AuthValidators.validateEmail(request.email)

        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { InvalidCredentialsException() }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw InvalidCredentialsException()
        }

        val accessToken = jwtProvider.createAccessToken(member.id, member.role.name)
        val refreshToken = jwtProvider.createRefreshToken(member.id)

        val existing = refreshTokenRepository.findByMemberId(member.id).orElse(null)

        val entity = existing?.apply {
            this.refreshToken = refreshToken
            this.expiresAt = jwtProvider.getRefreshExpirationDate()
            this.revokedAt = null
        } ?: AuthRefreshToken(
            memberId = member.id,
            refreshToken = refreshToken,
            expiresAt = jwtProvider.getRefreshExpirationDate()
        )

        refreshTokenRepository.save(entity)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProvider.getAccessExpirationDate(),
            refreshTokenExpiresIn = jwtProvider.getRefreshExpirationDate()
        )
    }

    @Transactional
    override fun reissue(refreshTokenHeader: String): TokenResponse {
        val token = refreshTokenHeader.removePrefix("Bearer ").trim()

        val claims = jwtProvider.parseClaims(token)

        if (claims["type"] != "REFRESH") {
            throw InvalidRefreshTokenException()
        }

        val memberId = claims.subject.toLong()

        val stored = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow { InvalidRefreshTokenException() }

        if (stored.refreshToken != token ||
            stored.expiresAt.isBefore(LocalDateTime.now()) ||
            stored.revokedAt != null
        ) {
            throw InvalidRefreshTokenException()
        }

        val member = memberRepository.findById(memberId).orElseThrow { NotFoundUserException() }

        val newAccess = jwtProvider.createAccessToken(memberId, member.role.name)
        val newRefresh = jwtProvider.createRefreshToken(memberId)

        stored.refreshToken = newRefresh
        stored.expiresAt = jwtProvider.getRefreshExpirationDate()
        stored.revokedAt = null

        refreshTokenRepository.save(stored)

        return TokenResponse(
            accessToken = newAccess,
            refreshToken = newRefresh,
            accessTokenExpiresIn = jwtProvider.getAccessExpirationDate(),
            refreshTokenExpiresIn = jwtProvider.getRefreshExpirationDate()
        )
    }

    @Transactional
    override fun changePassword(request: PasswordChangeRequest) {
        val member = memberRepository.findByEmail(request.email)
            .orElseThrow { NotFoundUserException() }

        val verification = emailVerificationRepository.findByEmailAndPurpose(request.email, Purpose.PASSWORD_CHANGE)
            .orElseThrow { InvalidVerifiedTokenException() }

        if (verification.verifiedToken == null ||
            verification.verifiedToken != request.verifiedToken ||
            verification.verifiedTokenExpiresAt == null ||
            verification.verifiedTokenExpiresAt!!.isBefore(LocalDateTime.now())
        ) {
            throw InvalidVerifiedTokenException()
        }

        AuthValidators.validatePassword(request.newPassword)

        val encoded = passwordEncoder.encode(request.newPassword) ?: throw InvalidPasswordPolicyException()
        member.password = encoded
    }

    @Transactional
    override fun signout(refreshTokenHeader: String) {
        val token = refreshTokenHeader.removePrefix("Bearer ").trim()

        val entity = refreshTokenRepository.findByRefreshToken(token)
            .orElseThrow { InvalidRefreshTokenException() }

        entity.revokedAt = LocalDateTime.now()
    }
}
