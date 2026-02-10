package com.example.team.haribo.goms.gomsserverv3.global.security

import com.example.team.haribo.goms.gomsserverv3.global.exception.ErrorCode
import com.example.team.haribo.goms.gomsserverv3.global.jwt.JwtProvider
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
                    SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN)
                    return
                }

                val memberId = claims.subject.toLong()
                val role = claims["role"]?.toString() ?: "ROLE_STUDENT"

                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority(role))
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: ExpiredJwtException) {
                SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.EXPIRED_TOKEN)
                return
            } catch (e: JwtException) {
                SecurityErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN)
                return
            } catch (e: IllegalArgumentException) {
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
