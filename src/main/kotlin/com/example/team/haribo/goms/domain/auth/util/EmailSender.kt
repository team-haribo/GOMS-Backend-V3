package com.example.team.haribo.goms.domain.auth.util

import jakarta.mail.internet.MimeMessage
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class EmailSender(
    private val mailSender: JavaMailSender
) {

    fun sendVerificationCode(email: String, code: String) {
        val html = ClassPathResource("templates/mail/verification.html")
            .inputStream
            .readBytes()
            .toString(StandardCharsets.UTF_8)
            .replace("{{code}}", code)

        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(email)
        helper.setSubject("[GOMS] 이메일 인증번호")
        helper.setText(html, true)

        mailSender.send(message)
    }
}