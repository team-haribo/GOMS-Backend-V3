package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@TestConfiguration(proxyBeanMethods = false)
class ConcurrencyRepositoryGateConfiguration {

    @Bean
    @Primary
    fun gatedMemberRepository(
        @Qualifier("memberRepository") delegate: MemberRepository
    ): MemberRepository = gatedProxy(delegate, "member") as MemberRepository

    @Bean
    @Primary
    fun gatedOutingRepository(
        @Qualifier("outingRepository") delegate: OutingRepository
    ): OutingRepository = gatedProxy(delegate, "outing") as OutingRepository

    private fun gatedProxy(delegate: Any, repository: String): Any {
        val repositoryInterface = when (repository) {
            "member" -> MemberRepository::class.java
            "outing" -> OutingRepository::class.java
            else -> error("Unsupported repository: $repository")
        }
        return Proxy.newProxyInstance(repositoryInterface.classLoader, arrayOf(repositoryInterface)) { _, method, args ->
            val callNumber = ConcurrencyRepositoryGate.beforeCall(repository, method.name)
            val result = try {
                method.invoke(delegate, *(args ?: emptyArray()))
            } catch (exception: InvocationTargetException) {
                throw exception.targetException
            }
            ConcurrencyRepositoryGate.afterCall(repository, method.name, callNumber)
            result
        }
    }
}

internal object ConcurrencyRepositoryGate {
    @Volatile
    private var activeGate: Gate? = null

    fun holdFirst(repository: String, methodNames: Set<String>, holdCount: Int = 1): Gate {
        check(activeGate == null) { "Only one repository gate can be active" }
        return Gate(repository, methodNames, holdCount).also { activeGate = it }
    }

    fun beforeCall(repository: String, methodName: String): Int =
        activeGate?.beforeCall(repository, methodName) ?: 0

    fun afterCall(repository: String, methodName: String, callNumber: Int) {
        activeGate?.afterCall(repository, methodName, callNumber)
    }

    fun clear() {
        activeGate?.release?.countDown()
        activeGate = null
    }

    class Gate internal constructor(
        private val repository: String,
        private val methodNames: Set<String>,
        private val holdCount: Int
    ) {
        private val matchingCalls = AtomicInteger()
        val firstCallReached = CountDownLatch(1)
        val allCallsReached = CountDownLatch(holdCount)
        val secondCallStarted = CountDownLatch(1)
        val release = CountDownLatch(1)

        @Volatile
        var firstMethod: String? = null
            private set

        fun beforeCall(repository: String, methodName: String): Int {
            if (repository != this.repository || methodName !in methodNames) return 0
            val callNumber = matchingCalls.incrementAndGet()
            if (callNumber == 2) secondCallStarted.countDown()
            return callNumber
        }

        fun afterCall(repository: String, methodName: String, callNumber: Int) {
            if (repository != this.repository || methodName !in methodNames || callNumber !in 1..holdCount) return
            if (callNumber == 1) firstMethod = methodName
            allCallsReached.countDown()
            firstCallReached.countDown()
            check(release.await(20, TimeUnit.SECONDS)) {
                "Timed out waiting to release $repository.$methodName from the concurrency test"
            }
        }
    }
}
