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

class PlaceSearchServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val recommendRepository = mockk<PlaceRecommendRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceSearchServiceImpl(placeRepository, recommendRepository, reviewRepository, memberUtil)

    val memberId = 1L

    describe("PlaceSearchService") {

        context("Given: 유효한 키워드 + 결과 있음") {
            val place = PlaceFixture.place(id = 1L)
            val projection = mockk<PlaceRecommendRepository.PlaceRecommendCountProjection>()

            every { projection.placeId } returns 1L
            every { projection.recommendCount } returns 3L

            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.searchByKeyword("서울") } returns listOf(place)
            every { recommendRepository.findRecommendedPlaceIds(memberId) } returns listOf(1L)
            every { recommendRepository.countRecommendedByPlaceIds(listOf(1L)) } returns listOf(projection)
            every { reviewRepository.countActiveByPlaceId(1L) } returns 2L

            it("When: 검색 시 Then: 결과와 장소 정보를 반환한다") {
                val response = service.search("서울")

                response.places.size shouldBe 1
                response.places[0].placeId shouldBe 1L
                response.places[0].placeName shouldBe place.placeName
                response.places[0].address shouldBe place.address
                response.places[0].roadAddress shouldBe place.roadAddress
                response.places[0].latitude shouldBe place.latitude
                response.places[0].longitude shouldBe place.longitude
                response.places[0].categoryGroupName shouldBe place.categoryGroupName
                response.places[0].categoryName shouldBe place.categoryName
                response.places[0].reviewCount shouldBe 2L
                response.places[0].recommendCount shouldBe 3L
                response.places[0].recommended shouldBe true
            }
        }

        context("Given: null 키워드") {
            it("When: 검색 시 Then: INVALID_REQUEST 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.search(null)
                }.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }

        context("Given: 빈 키워드") {
            it("When: 검색 시 Then: INVALID_REQUEST 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.search("   ")
                }.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }

        context("Given: 결과 없음") {
            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.searchByKeyword(any()) } returns emptyList()

            it("When: 검색 시 Then: 빈 리스트를 반환한다") {
                val response = service.search("존재하지않는장소")
                response.places.shouldBeEmpty()
            }
        }
    }
})