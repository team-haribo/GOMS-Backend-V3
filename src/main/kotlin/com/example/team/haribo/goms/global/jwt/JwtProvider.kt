package com.example.team.haribo.goms.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.crypto.SecretKey

class JwtProvider(
    private val jwtProperties: JwtProperties
) {

    private val key: SecretKey =
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    fun createAccessToken(memberId: Long, role: String): String {
        val now = Date()
        val exp = Date(now.time + jwtProperties.accessExpSeconds * 1000)

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("role", role)
            .claim("type", "ACCESS")
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(memberId: Long): String {
        val now = Date()
        val exp = Date(now.time + jwtProperties.refreshExpSeconds * 1000)

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("type", "REFRESH")
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun getAccessExpirationDate(): LocalDateTime {
        return LocalDateTime.now().plusSeconds(jwtProperties.accessExpSeconds)
    }

    fun getRefreshExpirationDate(): LocalDateTime {
        return LocalDateTime.now().plusSeconds(jwtProperties.refreshExpSeconds)
    }

    fun validate(token: String) {
        try {
            parseClaims(token)
        } catch (e: ExpiredJwtException) {
            throw e
        } catch (e: JwtException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw e
        }
    }
}
