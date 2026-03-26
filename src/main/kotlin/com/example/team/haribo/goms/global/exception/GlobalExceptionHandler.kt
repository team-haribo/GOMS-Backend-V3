package com.example.team.haribo.goms.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(e: GlobalException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = e.errorCode.status,
            message = e.errorCode.message
        )
        return ResponseEntity.status(e.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        return invalidRequestResponse()
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        return invalidRequestResponse()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled Exception: ", e)

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ErrorCode.INTERNAL_SERVER_ERROR.message
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private fun invalidRequestResponse(): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = ErrorCode.INVALID_REQUEST.status,
            message = ErrorCode.INVALID_REQUEST.message
        )
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.status).body(response)
    }
}