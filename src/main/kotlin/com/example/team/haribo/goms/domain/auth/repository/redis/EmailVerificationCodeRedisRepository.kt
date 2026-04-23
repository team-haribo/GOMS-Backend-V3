package com.example.team.haribo.goms.domain.auth.repository.redis

import com.example.team.haribo.goms.domain.common.enums.Purpose
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class EmailVerificationCodeRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val CODE_KEY_PREFIX = "auth:email-code"
        private const val COOLDOWN_KEY_PREFIX = "auth:email-code-cooldown"
        private const val CONFIRM_FAIL_KEY_PREFIX = "auth:email-code-confirm-fail"
    }

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

    fun getConfirmFailCount(email: String, purpose: Purpose): Long {
        return redisTemplate.opsForValue().get(confirmFailKey(email, purpose))?.toLongOrNull() ?: 0L
    }

    fun increaseConfirmFailCount(email: String, purpose: Purpose, ttlSeconds: Long): Long {
        val key = confirmFailKey(email, purpose)
        val count = redisTemplate.opsForValue().increment(key) ?: 0L
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds))
        }
        return count
    }

    fun deleteConfirmFailCount(email: String, purpose: Purpose) {
        redisTemplate.delete(confirmFailKey(email, purpose))
    }

    private fun codeKey(email: String, purpose: Purpose): String {
        return "$CODE_KEY_PREFIX:${purpose.name}:$email"
    }

    private fun cooldownKey(email: String, purpose: Purpose): String {
        return "$COOLDOWN_KEY_PREFIX:${purpose.name}:$email"
    }

    private fun confirmFailKey(email: String, purpose: Purpose): String {
        return "$CONFIRM_FAIL_KEY_PREFIX:${purpose.name}:$email"
    }
}