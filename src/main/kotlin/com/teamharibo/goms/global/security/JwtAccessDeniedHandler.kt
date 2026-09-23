package com.teamharibo.goms.global.security

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.log.RequestLogConstants
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