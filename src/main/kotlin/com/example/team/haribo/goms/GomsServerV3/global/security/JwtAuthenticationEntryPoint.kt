package com.example.team.haribo.goms.GomsServerV3.global.security

import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorCode
import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = ErrorCode.UNAUTHORIZED.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            objectMapper.writeValueAsString(
                ErrorResponse(
                    status = ErrorCode.UNAUTHORIZED.status,
                    message = ErrorCode.UNAUTHORIZED.message
                )
            )
        )
    }
}
