package com.example.team.haribo.goms.global.log

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/api/v3/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader("X-Request-Id")
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString().replace("-", "").take(16)
        val startTime = System.currentTimeMillis()

        request.setAttribute(RequestLogConstants.REQUEST_ID, requestId)
        request.setAttribute(RequestLogConstants.START_TIME, startTime)

        MDC.put("requestId", requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = System.currentTimeMillis() - startTime
            val memberId = request.getAttribute(RequestLogConstants.MEMBER_ID)
            val role = request.getAttribute(RequestLogConstants.ROLE)
            val failureReason = request.getAttribute(RequestLogConstants.FAILURE_REASON)
            val exceptionType = request.getAttribute(RequestLogConstants.EXCEPTION_TYPE)

            if (shouldLog(request, response, durationMs)) {
                log.info(
                    LogFormat.message(
                        domain = "HTTP",
                        event = "${request.method} ${request.requestURI}",
                        "requestId" to requestId,
                        "status" to response.status,
                        "durationMs" to durationMs,
                        "memberId" to memberId,
                        "role" to role,
                        "failureReason" to failureReason,
                        "exceptionType" to exceptionType,
                        "query" to request.queryString
                    )
                )
            }

            MDC.remove("requestId")
        }
    }

    private fun shouldLog(
        request: HttpServletRequest,
        response: HttpServletResponse,
        durationMs: Long
    ): Boolean {
        if (response.status >= 400) return true
        if (request.method != "GET") return true
        return durationMs >= 1000
    }
}