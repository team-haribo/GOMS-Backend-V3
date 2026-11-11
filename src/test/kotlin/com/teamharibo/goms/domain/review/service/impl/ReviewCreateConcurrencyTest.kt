package com.teamharibo.goms.domain.review.service.impl

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.place.entity.Place
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.review.dto.request.ReviewCreateRequest
import com.teamharibo.goms.domain.review.exception.AlreadyReviewedPlaceException
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.global.util.MemberUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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

@DataJpaTest
@TestPropertySource(properties = ["spring.profiles.active="])
@Import(MemberUtil::class, ReviewCreateServiceImpl::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
open class ReviewCreateConcurrencyTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val placeRepository: PlaceRepository,
    private val reviewRepository: ReviewRepository,
    private val reviewCreateService: ReviewCreateServiceImpl
) {

    private data class RaceResult(val success: Int, val alreadyReviewed: Int, val unexpected: Int)

    @BeforeEach
    fun cleanDatabase() {
        reviewRepository.deleteAllInBatch()
        placeRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @RepeatedTest(10)
    fun `같은 회원이 같은 장소에 동시에 후기를 작성해도 활성 후기는 하나만 생성된다`() {
        val memberId = saveMember("same-member")
        val placeId = savePlace("same-place")
        val threadCount = 8

        val result = race(List(threadCount) { memberId to placeId })

        assertEquals(0, result.unexpected, "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(1, result.success, "정확히 한 요청만 성공해야 한다")
        assertEquals(threadCount - 1, result.alreadyReviewed, "나머지는 AlreadyReviewedPlaceException으로 거부되어야 한다")
        assertEquals(1L, reviewRepository.countActiveByPlaceId(placeId), "활성 후기는 정확히 1건이어야 한다")
    }

    @Test
    fun `다른 회원이 같은 장소에 동시에 후기를 작성하면 모두 성공한다`() {
        val placeId = savePlace("shared-place")
        val memberIds = (1..4).map { saveMember("member-$it") }

        val result = race(memberIds.map { it to placeId })

        assertEquals(0, result.unexpected, "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(memberIds.size, result.success, "회원이 다르면 모두 성공해야 한다")
        assertEquals(memberIds.size.toLong(), reviewRepository.countActiveByPlaceId(placeId))
    }

    @Test
    fun `같은 회원이 다른 장소에 동시에 후기를 작성하면 모두 성공한다`() {
        val memberId = saveMember("multi-place-member")
        val placeIds = (1..4).map { savePlace("place-$it") }

        val result = race(placeIds.map { memberId to it })

        assertEquals(0, result.unexpected, "예상치 못한 예외는 발생하지 않아야 한다")
        assertEquals(placeIds.size, result.success, "장소가 다르면 모두 성공해야 한다")
        assertEquals(placeIds.size.toLong(), reviewRepository.countAllByMemberIdAndDeletedAtIsNull(memberId))
    }

    @Test
    fun `순차로 중복 작성하면 두 번째 요청은 AlreadyReviewedPlaceException으로 거부된다`() {
        val memberId = saveMember("serial-member")
        val placeId = savePlace("serial-place")
        authenticate(memberId)

        reviewCreateService.create(placeId, ReviewCreateRequest("첫 후기"))

        assertThrows(AlreadyReviewedPlaceException::class.java) {
            reviewCreateService.create(placeId, ReviewCreateRequest("두 번째 후기"))
        }
        assertEquals(1L, reviewRepository.countActiveByPlaceId(placeId))
    }

    @Test
    fun `후기를 soft delete한 뒤에는 같은 장소에 다시 작성할 수 있다`() {
        val memberId = saveMember("rewrite-member")
        val placeId = savePlace("rewrite-place")
        authenticate(memberId)

        val firstReviewId = reviewCreateService.create(placeId, ReviewCreateRequest("첫 후기")).review_id
        val firstReview = reviewRepository.findById(firstReviewId).orElseThrow()
        firstReview.softDelete("MEMBER")
        reviewRepository.save(firstReview)

        reviewCreateService.create(placeId, ReviewCreateRequest("다시 쓴 후기"))

        assertEquals(1L, reviewRepository.countActiveByPlaceId(placeId), "활성 후기는 1건이어야 한다")
        assertEquals(2L, reviewRepository.count(), "삭제된 후기는 보존되어야 한다")
    }

    private fun race(requests: List<Pair<Long, Long>>): RaceResult {
        val threadCount = requests.size
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val alreadyReviewedCount = AtomicInteger(0)
        val unexpectedCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        requests.forEachIndexed { index, (memberId, placeId) ->
            executor.submit {
                authenticate(memberId)
                readyLatch.countDown()
                try {
                    startLatch.await()
                    reviewCreateService.create(placeId, ReviewCreateRequest("동시 작성 후기 $index"))
                    successCount.incrementAndGet()
                } catch (e: AlreadyReviewedPlaceException) {
                    alreadyReviewedCount.incrementAndGet()
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

        return RaceResult(successCount.get(), alreadyReviewedCount.get(), unexpectedCount.get())
    }

    private fun authenticate(memberId: Long) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(memberId, null, emptyList())
    }

    private fun saveMember(key: String): Long =
        requireNotNull(
            memberRepository.save(
                Member(
                    email = "review-$key@gsm.hs.kr",
                    password = "encoded_password",
                    name = "후기테스트",
                    grade = 1,
                    department = Department.SW,
                    gender = Gender.MALE,
                    role = Role.ROLE_STUDENT,
                    status = Status.COMING
                )
            ).id
        )

    private fun savePlace(key: String): Long =
        requireNotNull(
            placeRepository.save(
                Place(
                    externalPlaceId = "review-$key",
                    placeName = "테스트 장소 $key",
                    address = "광주광역시 광산구",
                    latitude = 35.14,
                    longitude = 126.80,
                    isActive = true,
                    lastSyncedAt = LocalDateTime.now()
                )
            ).id
        )
}
