package com.example.team.haribo.goms.global.security

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.log.RequestLogConstants
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import tools.jackson.databind.ObjectMapper

class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        request.setAttribute(RequestLogConstants.FAILURE_REASON, ErrorCode.FORBIDDEN.name)
        SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.FORBIDDEN)
    }
}