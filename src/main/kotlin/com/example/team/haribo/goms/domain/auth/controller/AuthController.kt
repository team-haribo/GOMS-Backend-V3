package com.example.team.haribo.goms.domain.auth.controller

import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.example.team.haribo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.example.team.haribo.goms.domain.auth.dto.request.PasswordChangeRequest
import com.example.team.haribo.goms.domain.auth.dto.request.SigninRequest
import com.example.team.haribo.goms.domain.auth.dto.request.SignupRequest
import com.example.team.haribo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse
import com.example.team.haribo.goms.domain.auth.dto.response.TokenResponse
import com.example.team.haribo.goms.domain.auth.service.EmailVerificationService
import com.example.team.haribo.goms.domain.auth.service.PasswordChangeService
import com.example.team.haribo.goms.domain.auth.service.ReissueService
import com.example.team.haribo.goms.domain.auth.service.SigninService
import com.example.team.haribo.goms.domain.auth.service.SignoutService
import com.example.team.haribo.goms.domain.auth.service.SignupService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@Validated
@RequestMapping("/api/v3/auth")
class AuthController(
    private val signupService: SignupService,
    private val signinService: SigninService,
    private val reissueService: ReissueService,
    private val passwordChangeService: PasswordChangeService,
    private val signoutService: SignoutService,
    private val emailVerificationService: EmailVerificationService
) {

    @Operation(
        summary = "이메일 인증 코드 전송",
        description = "회원가입 또는 비밀번호 변경에 필요한 이메일 인증 코드를 전송합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = EmailVerificationSendRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "email": "s25038@gsm.hs.kr",
                              "purpose": "SIGNUP"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "인증 코드 전송 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @PostMapping("/email-verifications/send")
    fun sendVerificationCode(
        @Valid @RequestBody request: EmailVerificationSendRequest
    ): ResponseEntity<Void> {
        emailVerificationService.send(request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "이메일 인증 코드 확인",
        description = "전송된 이메일 인증 코드를 검증하고 verifiedToken 을 발급합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = EmailVerificationConfirmRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "email": "s25038@gsm.hs.kr",
                              "code": "123456",
                              "purpose": "SIGNUP"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "인증 성공"),
            ApiResponse(responseCode = "400", description = "인증 실패")
        ]
    )
    @PostMapping("/email-verifications/confirm")
    fun confirmVerificationCode(
        @Valid @RequestBody request: EmailVerificationConfirmRequest
    ): ResponseEntity<EmailVerificationConfirmResponse> {
        return ResponseEntity.ok(
            emailVerificationService.confirm(request)
        )
    }

    @Operation(
        summary = "회원가입",
        description = "verifiedToken 검증 후 회원가입을 진행합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = SignupRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "회원가입 실패")
        ]
    )
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest
    ): ResponseEntity<Void> {
        signupService.signup(request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하고 accessToken, refreshToken 을 발급합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = SigninRequest::class),
                    examples = [
                        ExampleObject(
                            value = """
                            {
                              "email": "s25038@gsm.hs.kr",
                              "password": "password123!"
                            }
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @PostMapping("/signin")
    fun signin(
        @Valid @RequestBody request: SigninRequest
    ): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(
            signinService.signin(request)
        )
    }

    @Operation(
        summary = "토큰 재발급",
        description = "RefreshToken 헤더를 기반으로 accessToken, refreshToken 을 재발급합니다.",
        parameters = [
            Parameter(
                name = "RefreshToken",
                `in` = ParameterIn.HEADER,
                description = "리프레시 토큰",
                required = true,
                example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "재발급 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
        ]
    )
    @PatchMapping("/reissue")
    fun reissue(
        @RequestHeader("RefreshToken")
        @NotBlank(message = "RefreshToken 헤더는 비어 있을 수 없습니다.")
        refreshTokenHeader: String
    ): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(
            reissueService.reissue(refreshTokenHeader)
        )
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "이메일 인증 후 비밀번호를 변경합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = PasswordChangeRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            ApiResponse(responseCode = "400", description = "비밀번호 변경 실패")
        ]
    )
    @PatchMapping("/password")
    fun changePassword(
        @Valid @RequestBody request: PasswordChangeRequest
    ): ResponseEntity<Void> {
        passwordChangeService.changePassword(request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "로그아웃",
        description = "RefreshToken 헤더를 기반으로 로그아웃 처리합니다.",
        parameters = [
            Parameter(
                name = "RefreshToken",
                `in` = ParameterIn.HEADER,
                description = "리프레시 토큰",
                required = true,
                example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
            )
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
        ]
    )
    @DeleteMapping("/signout")
    fun signout(
        @RequestHeader("RefreshToken")
        @NotBlank(message = "RefreshToken 헤더는 비어 있을 수 없습니다.")
        refreshTokenHeader: String
    ): ResponseEntity<Void> {
        signoutService.signout(refreshTokenHeader)
        return ResponseEntity.noContent().build()
    }
}