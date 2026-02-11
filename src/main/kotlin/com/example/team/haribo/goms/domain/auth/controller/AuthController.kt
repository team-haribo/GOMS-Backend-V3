package com.example.team.haribo.goms.domain.auth.controller

import com.example.team.haribo.goms.domain.auth.dto.request.*
import com.example.team.haribo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/auth")
class AuthController(
    private val signupService: SignupService,
    private val signinService: SigninService,
    private val reissueService: ReissueService,
    private val passwordChangeService: PasswordChangeService,
    private val signoutService: SignoutService,
    private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/email-verifications/send")
    fun sendVerificationCode(
        @RequestBody request: EmailVerificationSendRequest
    ): ResponseEntity<Void> {
        emailVerificationService.send(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/email-verifications/confirm")
    fun confirmVerificationCode(
        @RequestBody request: EmailVerificationConfirmRequest
    ): ResponseEntity<EmailVerificationConfirmResponse> {
        return ResponseEntity.ok(
            emailVerificationService.confirm(request)
        )
    }

    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest
    ): ResponseEntity<Void> {
        signupService.signup(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/signin")
    fun signin(
        @RequestBody request: SigninRequest
    ): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(
            signinService.signin(request)
        )
    }

    @PatchMapping("/reissue")
    fun reissue(
        @RequestHeader("RefreshToken") refreshTokenHeader: String
    ): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(
            reissueService.reissue(refreshTokenHeader)
        )
    }

    @PatchMapping("/password")
    fun changePassword(
        @RequestBody request: PasswordChangeRequest
    ): ResponseEntity<Void> {
        passwordChangeService.changePassword(request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/signout")
    fun signout(
        @RequestHeader("RefreshToken") refreshTokenHeader: String
    ): ResponseEntity<Void> {
        signoutService.signout(refreshTokenHeader)
        return ResponseEntity.ok().build()
    }
}
