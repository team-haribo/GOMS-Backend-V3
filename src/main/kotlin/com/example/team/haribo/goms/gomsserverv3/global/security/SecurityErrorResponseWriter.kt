package com.example.team.haribo.goms.gomsserverv3.global.security

import com.example.team.haribo.goms.gomsserverv3.global.exception.ErrorCode
import com.example.team.haribo.goms.gomsserverv3.global.exception.ErrorResponse
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
