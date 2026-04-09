package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class PlaceDetailServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val recommendRepository = mockk<PlaceRecommendRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceDetailServiceImpl(placeRepository, recommendRepository, reviewRepository, memberUtil)

    val memberId = 1L
    val placeId = 1L

    describe("PlaceDetailService") {

        context("Given: 존재하는 활성 placeId") {
            val place = PlaceFixture.place(id = placeId, isActive = true)
            val recommend = PlaceFixture.recommend(place = place, recommended = true)

            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { recommendRepository.countByPlaceIdAndRecommendedTrue(placeId) } returns 5L
            every { reviewRepository.countActiveByPlaceId(placeId) } returns 3L
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.of(recommend)

            it("When: 상세 조회 시 Then: 장소 상세 정보를 반환한다") {
                val response = service.getDetail(placeId)

                response.placeId shouldBe placeId
                response.placeName shouldBe place.placeName
                response.address shouldBe place.address
                response.roadAddress shouldBe place.roadAddress
                response.latitude shouldBe place.latitude
                response.longitude shouldBe place.longitude
                response.categoryGroupName shouldBe place.categoryGroupName
                response.categoryName shouldBe place.categoryName
                response.phone shouldBe place.phone
                response.placeUrl shouldBe place.placeUrl
                response.reviewCount shouldBe 3L
                response.recommendCount shouldBe 5L
                response.recommended shouldBe true
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.findByIdAndIsActiveTrue(999L) } returns Optional.empty()

            it("When: 상세 조회 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.getDetail(999L)
                }
            }
        }
    }
})