package com.example.team.haribo.goms.fixture

import com.example.team.haribo.goms.domain.auth.entity.AuthRefreshToken
import com.example.team.haribo.goms.domain.auth.entity.EmailVerification
import com.example.team.haribo.goms.domain.common.enums.Purpose
import java.time.LocalDateTime

object AuthFixture {

    const val VALID_REFRESH_TOKEN = "valid.refresh.token"
    const val VERIFIED_TOKEN = "valid-verified-uuid"

    fun activeRefreshToken(memberId: Long = 1L) = AuthRefreshToken(
        memberId = memberId,
        refreshToken = VALID_REFRESH_TOKEN,
        expiresAt = LocalDateTime.now().plusDays(7)
    )

    fun expiredRefreshToken(memberId: Long = 1L) = AuthRefreshToken(
        memberId = memberId,
        refreshToken = VALID_REFRESH_TOKEN,
        expiresAt = LocalDateTime.now().minusDays(1)
    )

    fun revokedRefreshToken(memberId: Long = 1L) = AuthRefreshToken(
        memberId = memberId,
        refreshToken = VALID_REFRESH_TOKEN,
        expiresAt = LocalDateTime.now().plusDays(7),
        revokedAt = LocalDateTime.now().minusHours(1)
    )

    fun signupVerification(
        email: String = "student@gsm.hs.kr",
        verifiedToken: String = VERIFIED_TOKEN
    ) = EmailVerification(
        email = email,
        purpose = Purpose.SIGNUP,
        code = "123456",
        codeExpiresAt = LocalDateTime.now().plusMinutes(5),
        verifiedToken = verifiedToken,
        verifiedTokenExpiresAt = LocalDateTime.now().plusMinutes(10)
    )

    fun expiredVerifiedTokenVerification(email: String = "student@gsm.hs.kr") = EmailVerification(
        email = email,
        purpose = Purpose.SIGNUP,
        code = "123456",
        codeExpiresAt = LocalDateTime.now().plusMinutes(5),
        verifiedToken = VERIFIED_TOKEN,
        verifiedTokenExpiresAt = LocalDateTime.now().minusMinutes(1)
    )

    fun passwordChangeVerification(
        email: String = "student@gsm.hs.kr",
        verifiedToken: String = VERIFIED_TOKEN
    ) = EmailVerification(
        email = email,
        purpose = Purpose.PASSWORD_CHANGE,
        code = "654321",
        codeExpiresAt = LocalDateTime.now().plusMinutes(5),
        verifiedToken = verifiedToken,
        verifiedTokenExpiresAt = LocalDateTime.now().plusMinutes(10)
    )

    fun freshCodeVerification(
        email: String = "student@gsm.hs.kr",
        code: String = "123456",
        purpose: Purpose = Purpose.SIGNUP
    ) = EmailVerification(
        email = email,
        purpose = purpose,
        code = code,
        codeExpiresAt = LocalDateTime.now().plusMinutes(5)
    )

    fun expiredCodeVerification(
        email: String = "student@gsm.hs.kr",
        code: String = "123456"
    ) = EmailVerification(
        email = email,
        purpose = Purpose.SIGNUP,
        code = code,
        codeExpiresAt = LocalDateTime.now().minusMinutes(1)
    )

    fun recentlySentVerification(email: String = "student@gsm.hs.kr") = EmailVerification(
        email = email,
        purpose = Purpose.SIGNUP,
        code = "123456",
        codeExpiresAt = LocalDateTime.now().plusMinutes(5),
        lastSentAt = LocalDateTime.now().minusSeconds(30)
    )
}
