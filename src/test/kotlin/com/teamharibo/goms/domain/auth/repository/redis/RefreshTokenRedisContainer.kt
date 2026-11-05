package com.teamharibo.goms.domain.auth.repository.redis

import io.kotest.core.annotation.Condition
import io.kotest.core.spec.Spec
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import kotlin.reflect.KClass

class RefreshTokenRedisContainer : GenericContainer<RefreshTokenRedisContainer>("redis:7.4") {
    init {
        withExposedPorts(REDIS_PORT)
    }

    fun connectionFactory(): LettuceConnectionFactory =
        LettuceConnectionFactory(host, getMappedPort(REDIS_PORT)).apply {
            afterPropertiesSet()
            start()
        }

    companion object {
        private const val REDIS_PORT = 6379
    }
}

class DockerAvailableCondition : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean =
        runCatching { DockerClientFactory.instance().isDockerAvailable }.getOrDefault(false)
}
