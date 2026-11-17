package com.teamharibo.goms.domain.member.service.impl

import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Platform
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.exception.NotFoundMemberException
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.notification.entity.DeviceToken
import com.teamharibo.goms.domain.notification.repository.DeviceTokenRepository
import com.teamharibo.goms.domain.outing.entity.Outing
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.global.util.MemberUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CountingRefreshTokenRedisRepository : RefreshTokenRedisRepository(StringRedisTemplate()) {
    val deleteCount = AtomicInteger(0)

    override fun deleteByMemberId(memberId: Long) {
        deleteCount.incrementAndGet()
    }
}

@TestConfiguration(proxyBeanMethods = false)
class MemberWithdrawConcurrencyConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(4)

    @Bean
    fun refreshTokenRedisRepository(): CountingRefreshTokenRedisRepository = CountingRefreshTokenRedisRepository()
}

@DataJpaTest
@TestPropertySource(properties = ["spring.profiles.active="])
@Import(MemberUtil::class, MemberWithdrawServiceImpl::class, MemberWithdrawConcurrencyConfiguration::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
open class MemberWithdrawConcurrencyTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRedisRepository: CountingRefreshTokenRedisRepository,
    private val withdrawService: MemberWithdrawServiceImpl
) {

    private data class RaceResult(val success: Int, val notFound: Int, val unexpected: Int)

    @BeforeEach
    fun cleanDatabase() {
        outingRepository.deleteAllInBatch()
        deviceTokenRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
        refreshTokenRedisRepository.deleteCount.set(0)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @RepeatedTest(10)
    fun `같은 회원이 동시에 탈퇴를 여러 번 요청해도 탈퇴는 한 번만 처리된다`() {
        val memberId = saveMemberWithReferences()
        val threadCount = 8

        val result = race(memberId, threadCount)

        assertEquals(0, result.unexpected, "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(1, result.success, "정확히 한 요청만 탈퇴를 처리해야 한다")
        assertEquals(threadCount - 1, result.notFound, "나머지는 NotFoundMemberException으로 거부되어야 한다")
        assertFalse(memberRepository.existsById(memberId))
        assertTrue(outingRepository.findAll().isEmpty())
        assertTrue(deviceTokenRepository.findAllByMember_Id(memberId).isEmpty())
        assertEquals(1, refreshTokenRedisRepository.deleteCount.get(), "리프레시 토큰 삭제는 한 번만 실행되어야 한다")
    }

    private fun race(memberId: Long, threadCount: Int): RaceResult {
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val notFoundCount = AtomicInteger(0)
        val unexpectedCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        repeat(threadCount) {
            executor.submit {
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(memberId, null, emptyList())
                readyLatch.countDown()
                try {
                    startLatch.await()
                    withdrawService.withdraw(MemberWithdrawRequest("password"))
                    successCount.incrementAndGet()
                } catch (e: NotFoundMemberException) {
                    notFoundCount.incrementAndGet()
                } catch (e: Exception) {
                    unexpectedCount.incrementAndGet()
                } finally {
                    SecurityContextHolder.clearContext()
                    doneLatch.countDown()
                }
            }
        }

        try {
            assertTrue(readyLatch.await(5, TimeUnit.SECONDS), "모든 작업 스레드가 준비되어야 한다")
            startLatch.countDown()
            assertTrue(doneLatch.await(15, TimeUnit.SECONDS), "모든 동시 요청이 제한 시간 안에 끝나야 한다")
        } finally {
            startLatch.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "작업 스레드가 종료되어야 한다")
        }

        return RaceResult(successCount.get(), notFoundCount.get(), unexpectedCount.get())
    }

    private fun saveMemberWithReferences(): Long {
        val member = memberRepository.saveAndFlush(
            Member(
                email = "withdraw-race@gsm.hs.kr",
                password = requireNotNull(passwordEncoder.encode("password")),
                name = "탈퇴테스트",
                grade = 1,
                department = Department.SW,
                gender = Gender.MALE,
                role = Role.ROLE_STUDENT,
                status = Status.COMING
            )
        )
        outingRepository.saveAndFlush(Outing(member = member))
        deviceTokenRepository.saveAndFlush(
            DeviceToken(member = member, deviceId = "withdraw-race-device", fcmToken = "withdraw-race-fcm", platform = Platform.ANDROID)
        )
        return requireNotNull(member.id)
    }
}
