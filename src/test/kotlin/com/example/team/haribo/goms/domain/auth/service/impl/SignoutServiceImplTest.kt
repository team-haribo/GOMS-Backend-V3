package com.example.team.haribo.goms.domain.auth.service.impl

import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.fixture.AuthFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class SignoutServiceImplTest : DescribeSpec({

    val refreshTokenRepository = mockk<AuthRefreshTokenRepository>()
    val service = SignoutServiceImpl(refreshTokenRepository)

    describe("SignoutService") {

        context("Given: 유효한 Bearer 리프레시 토큰") {
            val token = AuthFixture.activeRefreshToken()
            every { refreshTokenRepository.findByRefreshToken(AuthFixture.VALID_REFRESH_TOKEN) } returns
                Optional.of(token)

            it("When: 로그아웃 시 Then: revokedAt이 설정된다") {
                service.signout("Bearer ${AuthFixture.VALID_REFRESH_TOKEN}")
                token.revokedAt shouldNotBe null
            }
        }

        context("Given: DB에 없는 토큰") {
            every { refreshTokenRepository.findByRefreshToken(any()) } returns Optional.empty()

            it("When: 로그아웃 시 Then: INVALID_REFRESH_TOKEN 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.signout("Bearer unknown-token")
                }.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
            }
        }

        context("Given: Bearer 접두사 없는 토큰") {
            val token = AuthFixture.activeRefreshToken()
            every { refreshTokenRepository.findByRefreshToken(AuthFixture.VALID_REFRESH_TOKEN) } returns
                Optional.of(token)

            it("When: 로그아웃 시 Then: Bearer 없이도 정상 처리된다") {
                service.signout(AuthFixture.VALID_REFRESH_TOKEN)
                token.revokedAt shouldNotBe null
            }
        }
    }
})
