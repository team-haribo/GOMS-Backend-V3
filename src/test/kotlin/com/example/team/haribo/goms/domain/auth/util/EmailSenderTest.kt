package com.example.team.haribo.goms.domain.auth.util

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class EmailSenderTest : DescribeSpec({

    val javaMailSender = mockk<JavaMailSender>(relaxed = true)
    val emailSender = EmailSender(javaMailSender)

    describe("EmailSender.sendVerificationCode()") {

        context("Given: 유효한 이메일과 인증코드") {
            it("When: sendVerificationCode 호출 시 Then: JavaMailSender.send()가 호출된다") {
                emailSender.sendVerificationCode("user@gsm.hs.kr", "123456")
                verify(exactly = 1) { javaMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
