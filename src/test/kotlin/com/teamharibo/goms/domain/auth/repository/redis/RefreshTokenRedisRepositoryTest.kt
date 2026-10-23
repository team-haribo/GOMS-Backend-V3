package com.teamharibo.goms.domain.auth.repository.redis

import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

@EnabledIf(DockerAvailableCondition::class)
class RefreshTokenRedisRepositoryTest : DescribeSpec({

    val memberId = 1L
    val key = "auth:refresh:$memberId"
    val redis = RefreshTokenRedisContainer()

    lateinit var connectionFactory: LettuceConnectionFactory
    lateinit var redisTemplate: StringRedisTemplate
    lateinit var repository: RefreshTokenRedisRepository

    beforeSpec { redis.start() }

    afterSpec { redis.stop() }

    beforeEach {
        connectionFactory = redis.connectionFactory()
        redisTemplate = StringRedisTemplate(connectionFactory)
        redisTemplate.execute { connection ->
            connection.serverCommands().flushAll()
            null
        }
        repository = RefreshTokenRedisRepository(redisTemplate)
    }

    afterEach {
        connectionFactory.destroy()
    }

    describe("RefreshTokenRedisRepository.rotate") {

        context("Given: 저장된 토큰과 요청 토큰이 일치") {
            it("When: 교체 시 Then: 새 토큰으로 바꾸고 TTL을 새 값으로 설정한다") {
                repository.save(memberId, "current", 10)

                repository.rotate(memberId, "current", "next", 300) shouldBe true

                repository.findByMemberId(memberId) shouldBe "next"
                redisTemplate.getExpire(key, TimeUnit.SECONDS).toInt() shouldBeInRange 11..300
            }
        }

        context("Given: 저장된 토큰과 요청 토큰이 다름") {
            it("When: 교체 시 Then: 기존 토큰을 유지한다") {
                repository.save(memberId, "current", 300)

                repository.rotate(memberId, "stale", "next", 300) shouldBe false

                repository.findByMemberId(memberId) shouldBe "current"
            }
        }

        context("Given: 저장된 토큰 없음") {
            it("When: 교체 시 Then: 새 토큰을 만들지 않는다") {
                repository.rotate(memberId, "current", "next", 300) shouldBe false

                repository.findByMemberId(memberId).shouldBeNull()
            }
        }
    }

    describe("RefreshTokenRedisRepository.deleteIfMatch") {

        context("Given: 저장된 토큰과 요청 토큰이 일치") {
            it("When: 삭제 시 Then: 토큰을 삭제한다") {
                repository.save(memberId, "current", 300)

                repository.deleteIfMatch(memberId, "current") shouldBe true

                repository.findByMemberId(memberId).shouldBeNull()
            }
        }

        context("Given: 저장된 토큰과 요청 토큰이 다름") {
            it("When: 삭제 시 Then: 기존 토큰을 유지한다") {
                repository.save(memberId, "current", 300)

                repository.deleteIfMatch(memberId, "stale") shouldBe false

                repository.findByMemberId(memberId) shouldBe "current"
            }
        }
    }
})
