package com.teamharibo.goms.domain.auth.repository.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val KEY_PREFIX = "auth:refresh"

        private val ROTATE_SCRIPT = DefaultRedisScript(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                return 1
            end
            return 0
            """.trimIndent(),
            Long::class.java
        )

        private val DELETE_IF_MATCH_SCRIPT = DefaultRedisScript(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """.trimIndent(),
            Long::class.java
        )
    }

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

    fun rotate(memberId: Long, currentToken: String, newToken: String, ttlSeconds: Long): Boolean {
        return redisTemplate.execute(
            ROTATE_SCRIPT,
            listOf(key(memberId)),
            currentToken,
            newToken,
            ttlSeconds.toString()
        ) == 1L
    }

    fun deleteIfMatch(memberId: Long, token: String): Boolean {
        return redisTemplate.execute(
            DELETE_IF_MATCH_SCRIPT,
            listOf(key(memberId)),
            token
        ) == 1L
    }

    private fun key(memberId: Long): String {
        return "$KEY_PREFIX:$memberId"
    }
}