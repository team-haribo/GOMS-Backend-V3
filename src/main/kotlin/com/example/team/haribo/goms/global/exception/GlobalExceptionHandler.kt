package com.example.team.haribo.goms.global.exception

import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.log.RequestLogConstants
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(
        e: GlobalException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        request.setAttribute(RequestLogConstants.FAILURE_REASON, e.errorCode.name)
        request.setAttribute(RequestLogConstants.EXCEPTION_TYPE, e::class.simpleName)

        log.warn(
            LogFormat.message(
                domain = "EXCEPTION",
                event = "비즈니스 예외",
                "status" to e.errorCode.status,
                "message" to e.errorCode.message
            )
        )

        val response = ErrorResponse(
            status = e.errorCode.status,
            message = e.errorCode.message
        )
        return ResponseEntity.status(e.errorCode.status).body(response)
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
        HttpMessageNotReadableException::class
    )
    fun handleInvalidRequestException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        request.setAttribute(RequestLogConstants.FAILURE_REASON, ErrorCode.INVALID_REQUEST.name)
        request.setAttribute(RequestLogConstants.EXCEPTION_TYPE, e::class.simpleName)

        log.warn(
            LogFormat.message(
                domain = "EXCEPTION",
                event = "잘못된 요청",
                "type" to e::class.simpleName
            )
        )

        return invalidRequestResponse()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        request.setAttribute(RequestLogConstants.FAILURE_REASON, ErrorCode.INTERNAL_SERVER_ERROR.name)
        request.setAttribute(RequestLogConstants.EXCEPTION_TYPE, e::class.simpleName)

        log.error(
            LogFormat.message(
                domain = "EXCEPTION",
                event = "처리되지 않은 예외",
                "type" to e::class.simpleName
            ),
            e
        )

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