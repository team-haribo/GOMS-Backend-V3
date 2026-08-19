package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.exception.AlreadyOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.studentcouncil.service.impl.StudentCouncilForceOutServiceImpl
import com.example.team.haribo.goms.global.util.MemberUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * QrOutingServiceImpl.outing()의 "조회 -> 상태 검증 -> 저장" 흐름이
 * 동시 요청에서도 활성 Outing을 하나만 생성하는지 실제 DB(H2)와 스레드로 재현·검증한다.
 *
 * @DataJpaTest는 기본적으로 테스트 메서드 전체를 하나의 트랜잭션으로 감싸고 롤백하지만,
 * 그렇게 하면 스레드별로 실제 커밋되는 트랜잭션 경합을 재현할 수 없다.
 * 그래서 클래스 레벨에 Propagation.NOT_SUPPORTED를 걸어 테스트 메서드 자체는 트랜잭션 밖에서 실행되게 하고,
 * 서비스 메서드에 걸린 @Transactional이 스레드마다 독립적으로 커밋되도록 한다.
 */
@DataJpaTest
@TestPropertySource(properties = ["spring.profiles.active="])
@Import(MemberUtil::class, QrOutingServiceImpl::class, StudentCouncilForceOutServiceImpl::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class QrOutingConcurrencyTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val outingRepository: OutingRepository,
    private val qrOutingService: QrOutingServiceImpl,
    private val studentCouncilForceOutService: StudentCouncilForceOutServiceImpl
) {

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

        readyLatch.await(5, TimeUnit.SECONDS)
        startLatch.countDown()
        doneLatch.await(15, TimeUnit.SECONDS)
        executor.shutdown()

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

        readyLatch.await(5, TimeUnit.SECONDS)
        startLatch.countDown()
        doneLatch.await(15, TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(0, unexpectedCount.get(), "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(1, successCount.get(), "두 흐름 중 정확히 하나만 성공해야 한다")
        assertEquals(1, alreadyOutingCount.get(), "나머지 하나는 AlreadyOutingException으로 처리되어야 한다")

        val activeOutingCount = outingRepository.findAllActiveWithMember()
            .count { it.member.id == memberId }
        assertEquals(1, activeOutingCount, "서로 다른 서비스 진입점이어도 활성 Outing row는 정확히 1건이어야 한다")
    }
}
