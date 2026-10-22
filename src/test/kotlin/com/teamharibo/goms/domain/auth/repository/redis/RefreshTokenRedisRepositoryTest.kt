package com.teamharibo.goms.domain.auth.repository.redis

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.TimeUnit

class RefreshTokenRedisContainer : GenericContainer<RefreshTokenRedisContainer>("redis:7.4")

@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenRedisRepositoryTest {

    companion object {
        private const val MEMBER_ID = 1L
        private const val KEY = "auth:refresh:$MEMBER_ID"

        @Container
        @JvmField
        val redis: RefreshTokenRedisContainer = RefreshTokenRedisContainer().withExposedPorts(6379)
    }

    private lateinit var connectionFactory: LettuceConnectionFactory
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var repository: RefreshTokenRedisRepository

    @BeforeEach
    fun setUp() {
        connectionFactory = LettuceConnectionFactory(redis.host, redis.getMappedPort(6379)).apply {
            afterPropertiesSet()
            start()
        }
        redisTemplate = StringRedisTemplate(connectionFactory)
        redisTemplate.execute { connection ->
            connection.serverCommands().flushAll()
            null
        }
        repository = RefreshTokenRedisRepository(redisTemplate)
    }

    @AfterEach
    fun tearDown() {
        connectionFactory.destroy()
    }

    @Test
    fun `저장된 토큰과 일치하면 새 토큰으로 교체하고 TTL을 다시 설정한다`() {
        repository.save(MEMBER_ID, "current", 10)

        assertTrue(repository.rotate(MEMBER_ID, "current", "next", 300))

        assertEquals("next", repository.findByMemberId(MEMBER_ID))
        val ttl = redisTemplate.getExpire(KEY, TimeUnit.SECONDS)
        assertTrue(ttl in 11..300, "교체한 토큰의 TTL은 새 값으로 설정되어야 한다: $ttl")
    }

    @Test
    fun `저장된 토큰과 다르면 교체하지 않는다`() {
        repository.save(MEMBER_ID, "current", 300)

        assertFalse(repository.rotate(MEMBER_ID, "stale", "next", 300))

        assertEquals("current", repository.findByMemberId(MEMBER_ID))
    }

    @Test
    fun `저장된 토큰이 없으면 교체하지 않고 새로 만들지도 않는다`() {
        assertFalse(repository.rotate(MEMBER_ID, "current", "next", 300))

        assertNull(repository.findByMemberId(MEMBER_ID))
    }

    @Test
    fun `저장된 토큰과 일치하면 삭제한다`() {
        repository.save(MEMBER_ID, "current", 300)

        assertTrue(repository.deleteIfMatch(MEMBER_ID, "current"))

        assertNull(repository.findByMemberId(MEMBER_ID))
    }

    @Test
    fun `저장된 토큰과 다르면 삭제하지 않는다`() {
        repository.save(MEMBER_ID, "current", 300)

        assertFalse(repository.deleteIfMatch(MEMBER_ID, "stale"))

        assertEquals("current", repository.findByMemberId(MEMBER_ID))
    }
}
