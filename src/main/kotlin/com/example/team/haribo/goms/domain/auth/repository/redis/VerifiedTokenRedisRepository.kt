package com.example.team.haribo.goms.domain.auth.repository.redis

import com.example.team.haribo.goms.domain.common.enums.Purpose
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class VerifiedTokenRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    fun save(email: String, purpose: Purpose, verifiedToken: String, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(
            key(email, purpose),
            verifiedToken,
            Duration.ofSeconds(ttlSeconds)
        )
    }

    fun find(email: String, purpose: Purpose): String? {
        return redisTemplate.opsForValue().get(key(email, purpose))
    }

    fun delete(email: String, purpose: Purpose) {
        redisTemplate.delete(key(email, purpose))
    }

    private fun key(email: String, purpose: Purpose): String {
        return "auth:verified-token:${purpose.name}:$email"
    }
}