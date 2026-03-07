package com.example.team.haribo.goms.domain.auth.repository.redis

import com.example.team.haribo.goms.domain.common.enums.Purpose
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class EmailVerificationCodeRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    fun save(email: String, purpose: Purpose, code: String, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(
            codeKey(email, purpose),
            code,
            Duration.ofSeconds(ttlSeconds)
        )
    }

    fun find(email: String, purpose: Purpose): String? {
        return redisTemplate.opsForValue().get(codeKey(email, purpose))
    }

    fun delete(email: String, purpose: Purpose) {
        redisTemplate.delete(codeKey(email, purpose))
    }

    fun saveCooldown(email: String, purpose: Purpose, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(
            cooldownKey(email, purpose),
            "1",
            Duration.ofSeconds(ttlSeconds)
        )
    }

    fun existsCooldown(email: String, purpose: Purpose): Boolean {
        return redisTemplate.hasKey(cooldownKey(email, purpose))
    }

    private fun codeKey(email: String, purpose: Purpose): String {
        return "auth:email-code:${purpose.name}:$email"
    }

    private fun cooldownKey(email: String, purpose: Purpose): String {
        return "auth:email-code-cooldown:${purpose.name}:$email"
    }
}