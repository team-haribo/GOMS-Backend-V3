package com.example.team.haribo.goms.global.security

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.log.RequestLogConstants
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
        request.setAttribute(RequestLogConstants.FAILURE_REASON, ErrorCode.UNAUTHORIZED.name)
        SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.UNAUTHORIZED)
    }
}