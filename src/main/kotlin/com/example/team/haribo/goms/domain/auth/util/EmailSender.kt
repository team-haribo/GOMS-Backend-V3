package com.example.team.haribo.goms.domain.auth.util

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class EmailSender(
    private val mailSender: JavaMailSender
) {

    fun sendVerificationCode(email: String, code: String) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "[GOMS] 이메일 인증번호"
        message.text = "인증번호: $code"
        mailSender.send(message)
    }
}
