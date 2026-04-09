package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class PlaceHotPlaceServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val recommendRepository = mockk<PlaceRecommendRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceHotPlaceServiceImpl(placeRepository, recommendRepository, reviewRepository, memberUtil)

    val memberId = 1L

    fun setupCommon(hotIds: List<Long>) {
        every { memberUtil.currentMemberId() } returns memberId
        every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
        every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns hotIds

        if (hotIds.isNotEmpty()) {
            val places = hotIds.map { PlaceFixture.place(id = it, isActive = true) }
            every { placeRepository.findAllById(hotIds) } returns places
            every { reviewRepository.countActiveByPlaceId(any()) } returns 0L

            val projections = hotIds.map { placeId ->
                val projection = mockk<PlaceRecommendRepository.PlaceRecommendCountProjection>()
                every { projection.placeId } returns placeId
                every { projection.recommendCount } returns 5L
                projection
            }

            every {
                recommendRepository.countRecommendedByPlaceIdsSince(
                    hotIds,
                    any<LocalDateTime>()
                )
            } returns projections
        }
    }

    describe("PlaceHotPlaceService") {

        context("Given: days=null (기본 3일)") {
            setupCommon(listOf(1L, 2L, 3L))

            it("When: 핫플레이스 조회 시 Then: Top3 결과를 반환한다") {
                val response = service.getHotPlaces(null)
                response.places.size shouldBe 3
                response.places[0].placeId shouldBe 1L
            }
        }

        context("Given: days=1 (최솟값)") {
            setupCommon(listOf(1L))

            it("When: 핫플레이스 조회 시 Then: 정상적으로 결과를 반환한다") {
                val response = service.getHotPlaces(1L)
                response.places.size shouldBe 1
                response.places[0].placeId shouldBe 1L
            }
        }

        context("Given: days=30 (최댓값)") {
            setupCommon(listOf(10L, 20L))

            it("When: 핫플레이스 조회 시 Then: 정상적으로 결과를 반환한다") {
                val response = service.getHotPlaces(30L)
                response.places.size shouldBe 2
                response.places[0].placeId shouldBe 10L
                response.places[1].placeId shouldBe 20L
            }
        }

        context("Given: days=0 (범위 초과)") {
            it("When: 핫플레이스 조회 시 Then: INVALID_REQUEST 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.getHotPlaces(0L)
                }.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }

        context("Given: days=31 (범위 초과)") {
            it("When: 핫플레이스 조회 시 Then: INVALID_REQUEST 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.getHotPlaces(31L)
                }.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }

        context("Given: 결과 없음") {
            every { memberUtil.currentMemberId() } returns memberId
            every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
            every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns emptyList()

            it("When: 핫플레이스 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getHotPlaces(3L)
                response.places.shouldBeEmpty()
            }
        }
    }
})