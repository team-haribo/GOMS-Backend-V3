package com.example.team.haribo.goms.gomsserverv3.global.security

import com.example.team.haribo.goms.gomsserverv3.global.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import tools.jackson.databind.ObjectMapper

class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.UNAUTHORIZED)
    }
}
