package com.example.team.haribo.goms.domain.auth.util

import com.example.team.haribo.goms.domain.auth.exception.InvalidEmailFormatException
import com.example.team.haribo.goms.domain.auth.exception.InvalidPasswordPolicyException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class AuthValidatorsTest : DescribeSpec({

    describe("AuthValidators.validateEmail()") {

        context("Given: 유효한 이메일 형식") {
            listOf(
                "user@example.com",
                "user.name@domain.co.kr",
                "user+tag@gsm.hs.kr",
                "user123@test.org"
            ).forEach { email ->
                it("When: '$email' 검증 시 Then: 예외 없이 통과한다") {
                    shouldNotThrow<Exception> { AuthValidators.validateEmail(email) }
                }
            }
        }

        context("Given: 잘못된 이메일 형식") {
            listOf(
                "notanemail",
                "missing@",
                "@nodomain.com",
                "no-at-sign"
            ).forEach { email ->
                it("When: '$email' 검증 시 Then: InvalidEmailFormatException이 발생한다") {
                    shouldThrow<InvalidEmailFormatException> { AuthValidators.validateEmail(email) }
                }
            }
        }
    }

    describe("AuthValidators.validatePassword()") {

        context("Given: 유효한 비밀번호 (6~15자, 문자 포함)") {
            listOf(
                "Pass12",
                "abcDEF123",
                "a12345",
                "Password1!"
            ).forEach { password ->
                it("When: '$password' 검증 시 Then: 예외 없이 통과한다") {
                    shouldNotThrow<Exception> { AuthValidators.validatePassword(password) }
                }
            }
        }

        context("Given: 5자 미만 비밀번호") {
            it("When: 'Abc1' 검증 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> { AuthValidators.validatePassword("Abc1") }
            }
        }

        context("Given: 문자 없는 숫자만 비밀번호") {
            it("When: '123456' 검증 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> { AuthValidators.validatePassword("123456") }
            }
        }

        context("Given: 16자 초과 비밀번호") {
            it("When: 'PasswordTooLong1234' 검증 시 Then: InvalidPasswordPolicyException이 발생한다") {
                shouldThrow<InvalidPasswordPolicyException> {
                    AuthValidators.validatePassword("PasswordTooLong1234")
                }
            }
        }
    }
})
