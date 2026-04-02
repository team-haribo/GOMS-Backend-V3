package com.example.team.haribo.goms.global.security

import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.jwt.JwtProvider
import com.example.team.haribo.goms.global.log.RequestLogConstants
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveBearerToken(request)

        if (token != null) {
            try {
                val claims = jwtProvider.parseClaims(token)

                val type = claims["type"]?.toString()
                if (type != "ACCESS") {
                    request.setAttribute(RequestLogConstants.FAILURE_REASON, "INVALID_TOKEN")
                    SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN)
                    return
                }

                val memberId = claims.subject.toLong()
                val role = claims["role"]?.toString() ?: "ROLE_STUDENT"

                request.setAttribute(RequestLogConstants.MEMBER_ID, memberId)
                request.setAttribute(RequestLogConstants.ROLE, role)

                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority(role))
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: ExpiredJwtException) {
                request.setAttribute(RequestLogConstants.FAILURE_REASON, "EXPIRED_TOKEN")
                SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.EXPIRED_TOKEN)
                return
            } catch (e: JwtException) {
                request.setAttribute(RequestLogConstants.FAILURE_REASON, "INVALID_TOKEN")
                SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN)
                return
            } catch (e: IllegalArgumentException) {
                request.setAttribute(RequestLogConstants.FAILURE_REASON, "INVALID_TOKEN")
                SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        return header.substring(7).trim().ifEmpty { null }
    }
}