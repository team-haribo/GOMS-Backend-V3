package com.teamharibo.goms.global.security

import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.ErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import tools.jackson.databind.ObjectMapper

object SecurityErrorResponseWriter {

    fun write(response: HttpServletResponse, objectMapper: ObjectMapper, errorCode: ErrorCode) {
        response.status = errorCode.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            objectMapper.writeValueAsString(
                ErrorResponse(
                    status = errorCode.status,
                    message = errorCode.message
                )
            )
        )
    }
}
