package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
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
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceDetailServiceImpl(placeRepository, recommendRepository, memberUtil)

    val memberId = 1L
    val placeId = 1L

    describe("PlaceDetailService") {

        context("Given: 존재하는 placeId") {
            val place = PlaceFixture.place(id = placeId)
            val recommend = PlaceFixture.recommend(place = place, recommended = true)

            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.findById(placeId) } returns Optional.of(place)
            every { recommendRepository.countByPlaceIdAndRecommendedTrue(placeId) } returns 5L
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns
                Optional.of(recommend)

            it("When: 상세 조회 시 Then: 장소 정보 + 추천 수 + 현재 유저 추천 여부를 반환한다") {
                val response = service.getDetail(placeId)
                response.placeId shouldBe placeId
                response.recommendCount shouldBe 5L
                response.recommended shouldBe true
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.findById(999L) } returns Optional.empty()

            it("When: 상세 조회 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.getDetail(999L)
                }
            }
        }
    }
})
