package com.example.team.haribo.goms.GomsServerV3.global.security

import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorCode
import com.example.team.haribo.goms.GomsServerV3.global.exception.ErrorResponse
import com.example.team.haribo.goms.GomsServerV3.global.jwt.JwtProvider
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

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
                jwtProvider.validate(token)

                val memberId = jwtProvider.getMemberId(token)
                val role = jwtProvider.getRole(token) ?: "ROLE_STUDENT"

                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority(role))
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: ExpiredJwtException) {
                writeError(response, ErrorCode.EXPIRED_TOKEN)
                return
            } catch (e: JwtException) {
                writeError(response, ErrorCode.INVALID_TOKEN)
                return
            } catch (e: IllegalArgumentException) {
                writeError(response, ErrorCode.INVALID_TOKEN)
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

    private fun writeError(response: HttpServletResponse, errorCode: ErrorCode) {
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
