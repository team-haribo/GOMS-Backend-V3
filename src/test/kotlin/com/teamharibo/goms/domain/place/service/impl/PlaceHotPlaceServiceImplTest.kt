package com.teamharibo.goms.domain.place.service.impl

import com.teamharibo.goms.domain.place.repository.PlaceRecommendRepository
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.place.util.PlaceSummaryMapper
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.fixture.PlaceFixture
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.util.MemberUtil
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
    val placeSummaryMapper = PlaceSummaryMapper()
    val service = PlaceHotPlaceServiceImpl(
        placeRepository,
        recommendRepository,
        reviewRepository,
        memberUtil,
        placeSummaryMapper
    )

    val memberId = 1L

    fun setupCommon(hotIds: List<Long>) {
        every { memberUtil.currentMemberId() } returns memberId
        every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
        every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns hotIds

        if (hotIds.isNotEmpty()) {
            val places = hotIds.map { PlaceFixture.place(id = it, isActive = true) }
            every { placeRepository.findAllById(hotIds) } returns places

            val recommendProjections = hotIds.map { placeId ->
                val projection = mockk<PlaceRecommendRepository.PlaceRecommendCountProjection>()
                every { projection.placeId } returns placeId
                every { projection.recommendCount } returns 5L
                projection
            }

            val reviewProjections = hotIds.map { placeId ->
                val projection = mockk<ReviewRepository.PlaceReviewCountProjection>()
                every { projection.placeId } returns placeId
                every { projection.reviewCount } returns 0L
                projection
            }

            every {
                recommendRepository.countRecommendedByPlaceIdsSince(
                    hotIds,
                    any<LocalDateTime>()
                )
            } returns recommendProjections

            every { reviewRepository.countActiveByPlaceIds(hotIds) } returns reviewProjections
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

        context("Given: HotPlace의 집계 Query 결과가 없음") {
            it("When: 핫플레이스 조회 시 Then: 기본값 0L이 반환된다"){
                val hotIds = listOf(1L)
                val place = PlaceFixture.place(id = 1L, isActive = true)

                every { memberUtil.currentMemberId() } returns memberId
                every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
                every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns hotIds
                every { placeRepository.findAllById(hotIds) } returns listOf(place)
                every { recommendRepository.countRecommendedByPlaceIdsSince(hotIds, any<LocalDateTime>()) } returns emptyList()
                every { reviewRepository.countActiveByPlaceIds(hotIds) } returns emptyList()

                val response = service.getHotPlaces(1L)

                response.places[0].recommendCount shouldBe 0L
                response.places[0].reviewCount shouldBe 0L
                response.places[0].recommended shouldBe false
            }
        }

        context("Given: HotPlace ID 순서([3L, 1L, 2L])와 findAllById 리턴 순서([1L, 2L, 3L])가 다름") {
            it("When: 핫플레이스 조회 시 Then: 응답 places 순서는 hotIds 순서([3L, 1L, 2L])를 따른다") {
                val hotIds = listOf(3L, 1L, 2L)
                val place1 = PlaceFixture.place(id = 1L, isActive = true)
                val place2 = PlaceFixture.place(id = 2L, isActive = true)
                val place3 = PlaceFixture.place(id = 3L, isActive = true)

                every { memberUtil.currentMemberId() } returns memberId
                every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
                every { recommendRepository.countRecommendedByPlaceIdsSince(hotIds, any<LocalDateTime>()) } returns emptyList()
                every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns hotIds
                every { placeRepository.findAllById(hotIds) } returns listOf(place1, place2, place3)
                every { reviewRepository.countActiveByPlaceIds(hotIds) } returns emptyList()

                val response = service.getHotPlaces(1L)

                response.places.map { it.placeId } shouldBe listOf(3L, 1L, 2L)
            }
        }

        context("Given: HotPlace 중 비활성 Place가 포함됨") {
            it("When: 핫플레이스 조회 시 Then: 비활성 Place는 응답에서 제외된다") {
                val hotIds = listOf(1L, 2L)
                val place1 = PlaceFixture.place(id = 1L, isActive = false)
                val place2 = PlaceFixture.place(id = 2L, isActive = true)

                every { memberUtil.currentMemberId() } returns memberId
                every { recommendRepository.findRecommendedPlaceIds(memberId) } returns emptyList()
                every { recommendRepository.findHotPlaceIdsSince(any<LocalDateTime>(), any<Pageable>()) } returns hotIds
                every { placeRepository.findAllById(hotIds) } returns listOf(place1, place2)
                every { recommendRepository.countRecommendedByPlaceIdsSince(hotIds, any<LocalDateTime>()) } returns emptyList()
                every { reviewRepository.countActiveByPlaceIds(hotIds) } returns emptyList()

                val response = service.getHotPlaces(1L)

                response.places.map { it.placeId } shouldBe listOf(2L)
            }
        }

        context("Given: HotPlace 중 회원이 추천한 Place가 포함됨") {
            it("When: 핫플레이스 조회 시 Then: 추천한 Place는 recommended=true로 반환된다") {
                val hotIds = listOf(1L, 2L)
                val place1 = PlaceFixture.place(id = 1L, isActive = true)
                val place2 = PlaceFixture.place(id = 2L, isActive = true)

                every { memberUtil.currentMemberId() } returns memberId
                every { recommendRepository.findRecommendedPlaceIds(memberId) } returns listOf(1L)
                every { recommendRepository.findHotPlaceIdsSince(any(), any()) } returns hotIds
                every { placeRepository.findAllById(hotIds) } returns listOf(place1, place2)
                every { recommendRepository.countRecommendedByPlaceIdsSince(hotIds, any<LocalDateTime>()) } returns emptyList()
                every { reviewRepository.countActiveByPlaceIds(hotIds) } returns emptyList()

                val response = service.getHotPlaces(1L)

                response.places.find { it.placeId == 1L }?.recommended shouldBe true
                response.places.find { it.placeId == 2L }?.recommended shouldBe false

            }
        }
    }
})
