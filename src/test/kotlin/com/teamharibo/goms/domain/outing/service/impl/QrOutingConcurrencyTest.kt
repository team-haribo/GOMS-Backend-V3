package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.late.job.LateAutoCreateJob
import com.teamharibo.goms.domain.late.repository.LateRepository
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.outing.entity.Outing
import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.exception.AlreadyOutingException
import com.teamharibo.goms.domain.outing.exception.CannotOutingException
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.domain.outing.service.impl.QrComingServiceImpl
import com.teamharibo.goms.domain.studentcouncil.service.impl.StudentCouncilForceOutServiceImpl
import com.teamharibo.goms.domain.studentcouncil.service.impl.StudentCouncilOutingAllowedServiceImpl
import com.teamharibo.goms.global.util.MemberUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Outing 상태 변경의 경합을 실제 DB(H2 및 MariaDB Testcontainers)와 스레드로 재현·검증한다.
 *
 * @DataJpaTest는 기본적으로 테스트 메서드 전체를 하나의 트랜잭션으로 감싸고 롤백하지만,
 * 그렇게 하면 스레드별로 실제 커밋되는 트랜잭션 경합을 재현할 수 없다.
 * 그래서 클래스 레벨에 Propagation.NOT_SUPPORTED를 걸어 테스트 메서드 자체는 트랜잭션 밖에서 실행되게 하고,
 * 서비스 메서드에 걸린 @Transactional이 스레드마다 독립적으로 커밋되도록 한다.
 */
@DataJpaTest
@TestPropertySource(properties = ["spring.profiles.active="])
@Import(
    MemberUtil::class,
    QrOutingServiceImpl::class,
    QrComingServiceImpl::class,
    StudentCouncilForceOutServiceImpl::class,
    StudentCouncilOutingAllowedServiceImpl::class,
    LateAutoCreateJob::class,
    ConcurrencyRepositoryGateConfiguration::class
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
open class QrOutingConcurrencyTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository,
    private val lateRepository: LateRepository,
    private val qrOutingService: QrOutingServiceImpl,
    private val qrComingService: QrComingServiceImpl,
    private val studentCouncilForceOutService: StudentCouncilForceOutServiceImpl,
    private val outingAllowedService: StudentCouncilOutingAllowedServiceImpl,
    private val lateAutoCreateJob: LateAutoCreateJob
) {

    @BeforeEach
    fun cleanDatabase() {
        lateRepository.deleteAllInBatch()
        outingRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
        ConcurrencyRepositoryGate.clear()
    }

    @AfterEach
    fun clearGate() {
        ConcurrencyRepositoryGate.clear()
    }

    @Test
    fun `동시에 외출 요청을 보내도 활성 Outing은 하나만 생성된다`() {
        val member = memberRepository.save(
            Member(
                email = "concurrency-test@gsm.hs.kr",
                password = "encoded_password",
                name = "동시성테스트",
                grade = 1,
                department = Department.SW,
                gender = Gender.MALE,
                role = Role.ROLE_STUDENT,
                status = Status.COMING
            )
        )
        val memberId = requireNotNull(member.id)

        val threadCount = 8
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val alreadyOutingCount = AtomicInteger(0)
        val unexpectedCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        repeat(threadCount) {
            executor.submit {
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(memberId, null, emptyList())
                readyLatch.countDown()
                startLatch.await()
                try {
                    qrOutingService.outing(
                        QrToggleRequest(uuid = "concurrency-uuid", exp = System.currentTimeMillis() + 60_000)
                    )
                    successCount.incrementAndGet()
                } catch (e: AlreadyOutingException) {
                    alreadyOutingCount.incrementAndGet()
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

        assertEquals(0, unexpectedCount.get(), "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(1, successCount.get(), "정확히 한 건만 외출 처리에 성공해야 한다")
        assertEquals(threadCount - 1, alreadyOutingCount.get(), "나머지는 AlreadyOutingException으로 처리되어야 한다")

        val activeOutingCount = outingRepository.findAllActiveWithMember()
            .count { it.member.id == memberId }
        assertEquals(1, activeOutingCount, "동시 요청 이후에도 활성 Outing row는 정확히 1건이어야 한다")
    }

    @Test
    fun `학생의 QR 외출과 학생회의 강제 외출이 동시에 들어와도 활성 Outing은 하나만 생성된다`() {
        val member = memberRepository.save(
            Member(
                email = "concurrency-cross-flow@gsm.hs.kr",
                password = "encoded_password",
                name = "교차흐름테스트",
                grade = 1,
                department = Department.SW,
                gender = Gender.MALE,
                role = Role.ROLE_STUDENT,
                status = Status.COMING
            )
        )
        val memberId = requireNotNull(member.id)

        val readyLatch = CountDownLatch(2)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(2)
        val successCount = AtomicInteger(0)
        val alreadyOutingCount = AtomicInteger(0)
        val unexpectedCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(2)

        // Thread 1: 학생 본인의 QR 외출 요청
        executor.submit {
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(memberId, null, emptyList())
            readyLatch.countDown()
            startLatch.await()
            try {
                qrOutingService.outing(
                    QrToggleRequest(uuid = "cross-flow-uuid", exp = System.currentTimeMillis() + 60_000)
                )
                successCount.incrementAndGet()
            } catch (e: AlreadyOutingException) {
                alreadyOutingCount.incrementAndGet()
            } catch (e: Exception) {
                unexpectedCount.incrementAndGet()
            } finally {
                SecurityContextHolder.clearContext()
                doneLatch.countDown()
            }
        }

        // Thread 2: 같은 학생에 대한 학생회의 강제 외출 요청 (SecurityContext 불필요)
        executor.submit {
            readyLatch.countDown()
            startLatch.await()
            try {
                studentCouncilForceOutService.out(memberId)
                successCount.incrementAndGet()
            } catch (e: AlreadyOutingException) {
                alreadyOutingCount.incrementAndGet()
            } catch (e: Exception) {
                unexpectedCount.incrementAndGet()
            } finally {
                doneLatch.countDown()
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

        assertEquals(0, unexpectedCount.get(), "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(1, successCount.get(), "두 흐름 중 정확히 하나만 성공해야 한다")
        assertEquals(1, alreadyOutingCount.get(), "나머지 하나는 AlreadyOutingException으로 처리되어야 한다")

        val activeOutingCount = outingRepository.findAllActiveWithMember()
            .count { it.member.id == memberId }
        assertEquals(1, activeOutingCount, "서로 다른 서비스 진입점이어도 활성 Outing row는 정확히 1건이어야 한다")
    }

    @RepeatedTest(10)
    fun `외출 허용 변경과 QR 외출 경합에서도 상태와 활성 외출이 일치한다`() {
        val member = createMember(Status.COMING)
        val memberId = requireNotNull(member.id)
        val gate = ConcurrencyRepositoryGate.holdFirst(
            repository = "member",
            methodNames = setOf("findById", "findByIdForUpdate")
        )
        val executor = Executors.newFixedThreadPool(2)
        try {
            val allowedChange = executor.submit<Result<Unit>> {
                runCatching { outingAllowedService.update(memberId, Status.CANNOT_OUTING) }
            }
            assertTrue(gate.firstCallReached.await(5, TimeUnit.SECONDS), "외출 허용 변경이 상태를 읽어야 한다")

            val qrOuting = executor.submit<Result<*>> {
                runCatching {
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(memberId, null, emptyList())
                    qrOutingService.outing(
                        QrToggleRequest(uuid = "allowed-race", exp = System.currentTimeMillis() + 60_000)
                    )
                }.also { SecurityContextHolder.clearContext() }
            }
            assertTrue(gate.secondCallStarted.await(5, TimeUnit.SECONDS), "QR 요청이 회원 잠금을 시도해야 한다")

            if (gate.firstMethod == "findById") {
                assertTrue(qrOuting.get(10, TimeUnit.SECONDS).isSuccess, "잠금 없는 이전 흐름에서는 QR 요청이 먼저 커밋되어야 한다")
                gate.release.countDown()
            } else {
                gate.release.countDown()
            }

            val allowedResult = allowedChange.get(10, TimeUnit.SECONDS)
            val outingResult = qrOuting.get(10, TimeUnit.SECONDS)
            assertTrue(allowedResult.isSuccess, "먼저 잠금을 획득한 허용 변경은 성공해야 한다")
            assertTrue(outingResult.exceptionOrNull() is CannotOutingException, "잠금 후 최신 상태를 읽은 QR 요청은 외출 불가로 거절되어야 한다")

            val finalMember = memberRepository.findById(memberId).orElseThrow()
            val activeCount = outingRepository.findAllActiveWithMember().count { it.member.id == memberId }
            assertEquals(Status.CANNOT_OUTING, finalMember.status)
            assertEquals(0, activeCount, "외출 불가 상태에는 활성 Outing이 없어야 한다")
        } finally {
            gate.release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "경합 작업 스레드가 종료되어야 한다")
            ConcurrencyRepositoryGate.clear()
        }
    }

    @RepeatedTest(10)
    fun `QR 복귀가 지각 처리보다 먼저 끝나면 지각이 생성되지 않는다`() {
        val member = createMember(Status.OUTING)
        val memberId = requireNotNull(member.id)
        outingRepository.save(Outing(member = member, outingAt = LocalDateTime.now().minusHours(1)))
        val gate = ConcurrencyRepositoryGate.holdFirst(
            repository = "outing",
            methodNames = setOf("findAllActiveWithOutingMember", "findAllActiveMemberIds")
        )
        val executor = Executors.newFixedThreadPool(2)
        try {
            val lateJob = executor.submit<Result<Unit>> {
                runCatching { lateAutoCreateJob.createLatesForOutingMembers() }
            }
            assertTrue(gate.firstCallReached.await(5, TimeUnit.SECONDS), "지각 작업이 대상 외출을 읽어야 한다")

            val qrComing = executor.submit<Result<*>> {
                runCatching {
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(memberId, null, emptyList())
                    qrComingService.coming(
                        QrToggleRequest(uuid = "late-race", exp = System.currentTimeMillis() + 60_000)
                    )
                }.also { SecurityContextHolder.clearContext() }
            }
            assertTrue(qrComing.get(10, TimeUnit.SECONDS).isSuccess, "지각 처리가 잠금을 얻기 전 QR 복귀가 완료되어야 한다")
            gate.release.countDown()
            assertTrue(lateJob.get(10, TimeUnit.SECONDS).isSuccess)

            assertEquals(Status.COMING, memberRepository.findById(memberId).orElseThrow().status)
            assertEquals(0, lateRepository.countByMemberId(memberId), "복귀 완료 후 지각이 생성되면 안 된다")
            assertEquals(0, outingRepository.findAllActiveWithMember().count { it.member.id == memberId })
        } finally {
            gate.release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "경합 작업 스레드가 종료되어야 한다")
            ConcurrencyRepositoryGate.clear()
        }
    }

    @RepeatedTest(10)
    fun `중복 실행된 지각 작업은 같은 외출에 Late를 한 건만 생성한다`() {
        val member = createMember(Status.OUTING)
        val memberId = requireNotNull(member.id)
        outingRepository.save(Outing(member = member, outingAt = LocalDateTime.now().minusHours(1)))
        val gate = ConcurrencyRepositoryGate.holdFirst(
            repository = "outing",
            methodNames = setOf("findAllActiveWithOutingMember", "findAllActiveMemberIds"),
            holdCount = 2
        )
        val executor = Executors.newFixedThreadPool(2)
        try {
            val jobs = List(2) {
                executor.submit<Result<Unit>> { runCatching { lateAutoCreateJob.createLatesForOutingMembers() } }
            }
            assertTrue(gate.allCallsReached.await(5, TimeUnit.SECONDS), "두 스케줄러 실행이 같은 후보를 읽어야 한다")
            gate.release.countDown()
            jobs.forEach { assertTrue(it.get(10, TimeUnit.SECONDS).isSuccess) }

            assertEquals(1, lateRepository.countByMemberId(memberId), "동일 Outing에는 Late가 최대 한 건이어야 한다")
            assertEquals(Status.CANNOT_OUTING, memberRepository.findById(memberId).orElseThrow().status)
            assertEquals(0, outingRepository.findAllActiveWithMember().count { it.member.id == memberId })
        } finally {
            gate.release.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "중복 스케줄러 스레드가 종료되어야 한다")
            ConcurrencyRepositoryGate.clear()
        }
    }

    private fun createMember(status: Status): Member = memberRepository.save(
        Member(
            email = "concurrency-${java.util.UUID.randomUUID()}@gsm.hs.kr",
            password = "encoded_password",
            name = "동시성 테스트",
            grade = 1,
            department = Department.SW,
            gender = Gender.MALE,
            role = Role.ROLE_STUDENT,
            status = status
        )
    )
}
