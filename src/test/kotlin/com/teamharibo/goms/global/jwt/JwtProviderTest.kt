package com.teamharibo.goms.global.jwt

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

class JwtProviderTest : DescribeSpec({

    val jwtProvider = JwtProvider(
        JwtProperties(
            secret = "jwt-provider-test-secret-key-1234567890-abcdefghij",
            accessExpSeconds = 300,
            refreshExpSeconds = 300
        )
    )

    describe("JwtProvider") {

        context("Given: 같은 회원의 Refresh Token을 같은 시각에 연속 발급") {
            it("When: 두 토큰을 비교하면 Then: 발급 ID가 달라 토큰 값이 서로 다르다") {
                val first = jwtProvider.createRefreshToken(1L)
                val second = jwtProvider.createRefreshToken(1L)

                first shouldNotBe second

                val firstClaims = jwtProvider.parseClaims(first)
                firstClaims.id.shouldNotBeBlank()
                firstClaims["type"] shouldBe "REFRESH"
                firstClaims.id shouldNotBe jwtProvider.parseClaims(second).id
            }
        }
    }
})
