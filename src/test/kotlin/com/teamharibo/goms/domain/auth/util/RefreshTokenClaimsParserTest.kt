package com.teamharibo.goms.domain.auth.util

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProperties
import com.teamharibo.goms.global.jwt.JwtProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

class RefreshTokenClaimsParserTest : DescribeSpec({

    val log = LoggerFactory.getLogger(RefreshTokenClaimsParserTest::class.java)
    val failureEvent = "토큰 재발급 실패"

    fun provider(secret: String = "refresh-token-parser-test-secret-key-1234567890", refreshExpSeconds: Long = 300) =
        JwtProvider(
            JwtProperties(
                secret = secret,
                accessExpSeconds = 300,
                refreshExpSeconds = refreshExpSeconds
            )
        )

    describe("RefreshTokenClaimsParser.parse()") {

        context("Given: 유효한 리프레시 토큰") {
            it("When: 파싱 시 Then: Claims를 반환한다") {
                val jwtProvider = provider()
                val token = jwtProvider.createRefreshToken(1L)

                val claims = RefreshTokenClaimsParser.parse(jwtProvider, token, log, failureEvent)

                claims.subject shouldBe "1"
                claims["type"] shouldBe "REFRESH"
            }
        }

        context("Given: 만료된 리프레시 토큰") {
            it("When: 파싱 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생한다") {
                val jwtProvider = provider(refreshExpSeconds = -60)
                val token = jwtProvider.createRefreshToken(1L)

                val exception = shouldThrow<GlobalException> {
                    RefreshTokenClaimsParser.parse(jwtProvider, token, log, failureEvent)
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
            }
        }

        context("Given: 다른 키로 서명된 리프레시 토큰") {
            it("When: 파싱 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생한다") {
                val otherProvider = provider(secret = "other-refresh-token-parser-test-secret-key-0987654321")
                val token = otherProvider.createRefreshToken(1L)

                val exception = shouldThrow<GlobalException> {
                    RefreshTokenClaimsParser.parse(provider(), token, log, failureEvent)
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
            }
        }

        context("Given: JWT 형식이 아닌 토큰") {
            it("When: 파싱 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    RefreshTokenClaimsParser.parse(provider(), "not-a-jwt", log, failureEvent)
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
            }
        }

        context("Given: 빈 토큰") {
            it("When: 파싱 시 Then: 401 INVALID_REFRESH_TOKEN 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    RefreshTokenClaimsParser.parse(provider(), "", log, failureEvent)
                }

                exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                exception.errorCode.status shouldBe 401
            }
        }
    }
})
