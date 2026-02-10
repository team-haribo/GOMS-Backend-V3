package com.example.team.haribo.goms.GomsServerV3.global.security

import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorCode
import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = ErrorCode.FORBIDDEN.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            objectMapper.writeValueAsString(
                ErrorResponse(
                    status = ErrorCode.FORBIDDEN.status,
                    message = ErrorCode.FORBIDDEN.message
                )
            )
        )
    }
}
