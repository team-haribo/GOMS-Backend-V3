package com.example.team.haribo.goms.GomsServerV3.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(exception: GlobalException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = exception.errorCode.status,
            message = exception.errorCode.message
        )
        return ResponseEntity.status(response.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled Exception: ", exception)

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ErrorCode.INTERNAL_SERVER_ERROR.message
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}