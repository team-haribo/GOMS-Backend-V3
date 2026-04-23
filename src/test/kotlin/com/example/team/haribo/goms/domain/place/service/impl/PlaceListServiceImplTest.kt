package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class PlaceListServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val placeRecommendRepository = mockk<PlaceRecommendRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceListServiceImpl(
        placeRepository,
        placeRecommendRepository,
        reviewRepository,
        memberUtil
    )

    val member = MemberFixture.student(id = 1L)

    describe("PlaceListService") {

        context("Given: 활성 장소가 존재함") {
            val places = listOf(
                PlaceFixture.place(id = 1L, placeName = "학생식당"),
                PlaceFixture.place(id = 2L, placeName = "매점")
            )

            every { memberUtil.currentMemberId() } returns member.id!!
            every { placeRepository.findAllByIsActiveTrue() } returns places
            every { placeRecommendRepository.findRecommendedPlaceIds(member.id!!) } returns listOf(2L)
            every { placeRecommendRepository.countRecommendedByPlaceIds(listOf(1L, 2L)) } returns listOf(
                object : PlaceRecommendRepository.PlaceRecommendCountProjection {
                    override val placeId = 1L
                    override val recommendCount = 3L
                },
                object : PlaceRecommendRepository.PlaceRecommendCountProjection {
                    override val placeId = 2L
                    override val recommendCount = 5L
                }
            )
            every { reviewRepository.countActiveByPlaceIds(listOf(1L, 2L)) } returns listOf(
                object : ReviewRepository.PlaceReviewCountProjection {
                    override val placeId = 1L
                    override val reviewCount = 4L
                },
                object : ReviewRepository.PlaceReviewCountProjection {
                    override val placeId = 2L
                    override val reviewCount = 7L
                }
            )

            it("When: 장소 목록 조회 시 Then: 추천 여부와 리뷰 수를 포함해 반환한다") {
                val response = service.getPlaces()

                response.places.size shouldBe 2

                response.places[0].placeId shouldBe 1L
                response.places[0].placeName shouldBe "학생식당"
                response.places[0].recommended shouldBe false
                response.places[0].recommendCount shouldBe 3L
                response.places[0].reviewCount shouldBe 4L

                response.places[1].placeId shouldBe 2L
                response.places[1].placeName shouldBe "매점"
                response.places[1].recommended shouldBe true
                response.places[1].recommendCount shouldBe 5L
                response.places[1].reviewCount shouldBe 7L
            }
        }

        context("Given: 활성 장소가 없음") {
            every { memberUtil.currentMemberId() } returns member.id!!
            every { placeRepository.findAllByIsActiveTrue() } returns emptyList()

            it("When: 장소 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getPlaces()
                response.places.shouldBeEmpty()
            }
        }
    }
})