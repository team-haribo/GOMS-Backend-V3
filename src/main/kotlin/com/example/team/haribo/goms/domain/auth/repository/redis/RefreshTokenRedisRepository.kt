package com.example.team.haribo.goms.domain.auth.repository.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    fun save(memberId: Long, refreshToken: String, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(
            key(memberId),
            refreshToken,
            Duration.ofSeconds(ttlSeconds)
        )
    }

    fun findByMemberId(memberId: Long): String? {
        return redisTemplate.opsForValue().get(key(memberId))
    }

    fun deleteByMemberId(memberId: Long) {
        redisTemplate.delete(key(memberId))
    }

    private fun key(memberId: Long): String {
        return "auth:refresh:$memberId"
    }
}