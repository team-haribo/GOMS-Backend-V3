package com.example.team.haribo.goms.domain.auth.controller

import com.example.team.haribo.goms.domain.auth.dto.request.*
import com.example.team.haribo.goms.domain.auth.dto.response.*
import com.example.team.haribo.goms.domain.auth.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/auth")
class AuthController(
    private val emailVerificationService: EmailVerificationService,
    private val authService: AuthService
) {

    @PostMapping("/email-verifications/send")
    fun send(@RequestBody request: EmailVerificationSendRequest)
            : ResponseEntity<Void> {
        emailVerificationService.send(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/email-verifications/confirm")
    fun confirm(
        @RequestBody request: EmailVerificationConfirmRequest
    ): ResponseEntity<EmailVerificationConfirmResponse> {
        return ResponseEntity.ok(emailVerificationService.confirm(request))
    }

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest)
            : ResponseEntity<Void> {
        authService.signup(request)
        return ResponseEntity.status(201).build()
    }

    @PostMapping("/signin")
    fun signin(@RequestBody request: SigninRequest)
            : ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(authService.signin(request))
    }

    @PatchMapping("/reissue")
    fun reissue(
        @RequestHeader("RefreshToken") refreshToken: String
    ): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(authService.reissue(refreshToken))
    }

    @PatchMapping("/password")
    fun changePassword(@RequestBody request: PasswordChangeRequest)
            : ResponseEntity<Void> {
        authService.changePassword(request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/signout")
    fun signout(
        @RequestHeader("RefreshToken") refreshToken: String
    ): ResponseEntity<Void> {
        authService.signout(refreshToken)
        return ResponseEntity.ok().build()
    }
}
