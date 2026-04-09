package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.exception.AlreadyRecommendedPlaceException
import com.example.team.haribo.goms.domain.place.exception.AlreadyUnrecommendedPlaceException
import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class PlaceRecommendServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val recommendRepository = mockk<PlaceRecommendRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = PlaceRecommendServiceImpl(placeRepository, recommendRepository, reviewRepository, memberUtil)

    val member = MemberFixture.student()
    val place = PlaceFixture.place(id = 1L, isActive = true)
    val placeId = place.id!!
    val memberId = member.id!!

    describe("PlaceRecommendService.recommend()") {

        context("Given: 추천 레코드 없음 (신규)") {
            every { memberUtil.currentMember() } returns member
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.empty()
            every { recommendRepository.save(any()) } returnsArgument 0

            it("When: 추천 시 Then: 신규 추천 레코드를 저장하고 true를 반환한다") {
                val response = service.recommend(placeId)
                response.recommended shouldBe true
                verify(exactly = 1) { recommendRepository.save(any()) }
            }
        }

        context("Given: 기존 미추천 레코드 있음") {
            val existingUnrecommended = PlaceFixture.recommend(place = place, member = member, recommended = false)

            every { memberUtil.currentMember() } returns member
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.of(existingUnrecommended)
            every { recommendRepository.save(any()) } returnsArgument 0

            it("When: 추천 시 Then: recommended=true로 업데이트한다") {
                val response = service.recommend(placeId)
                response.recommended shouldBe true
                existingUnrecommended.recommended shouldBe true
            }
        }

        context("Given: 이미 추천 상태") {
            val alreadyRecommended = PlaceFixture.recommend(place = place, member = member, recommended = true)

            every { memberUtil.currentMember() } returns member
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.of(alreadyRecommended)

            it("When: 추천 시 Then: AlreadyRecommendedPlaceException이 발생한다") {
                shouldThrow<AlreadyRecommendedPlaceException> {
                    service.recommend(placeId)
                }
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { memberUtil.currentMember() } returns member
            every { placeRepository.findByIdAndIsActiveTrue(999L) } returns Optional.empty()

            it("When: 추천 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.recommend(999L)
                }
            }
        }
    }

    describe("PlaceRecommendService.unrecommend()") {

        context("Given: 추천 상태 레코드 있음") {
            val recommended = PlaceFixture.recommend(place = place, member = member, recommended = true)

            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.existsByIdAndIsActiveTrue(placeId) } returns true
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.of(recommended)
            every { recommendRepository.save(any()) } returnsArgument 0

            it("When: 추천 취소 시 Then: recommended=false로 변경한다") {
                val response = service.unrecommend(placeId)
                response.recommended shouldBe false
                recommended.recommended shouldBe false
            }
        }

        context("Given: 이미 미추천 상태") {
            val unrecommended = PlaceFixture.recommend(place = place, member = member, recommended = false)

            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.existsByIdAndIsActiveTrue(placeId) } returns true
            every { recommendRepository.findByPlaceIdAndMemberId(placeId, memberId) } returns Optional.of(unrecommended)

            it("When: 추천 취소 시 Then: AlreadyUnrecommendedPlaceException이 발생한다") {
                shouldThrow<AlreadyUnrecommendedPlaceException> {
                    service.unrecommend(placeId)
                }
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { memberUtil.currentMemberId() } returns memberId
            every { placeRepository.existsByIdAndIsActiveTrue(999L) } returns false

            it("When: 추천 취소 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.unrecommend(999L)
                }
            }
        }
    }

    describe("PlaceRecommendService.getRecommendedPlaces()") {

        context("Given: 추천 장소 있음") {
            val rec = PlaceFixture.recommend(place = place, member = member)
            val recommendProjection = mockk<PlaceRecommendRepository.PlaceRecommendCountProjection>()
            val reviewProjection = mockk<ReviewRepository.PlaceReviewCountProjection>()

            every { recommendProjection.placeId } returns placeId
            every { recommendProjection.recommendCount } returns 4L

            every { reviewProjection.placeId } returns placeId
            every { reviewProjection.reviewCount } returns 2L

            every { memberUtil.currentMemberId() } returns memberId
            every { recommendRepository.findAllByMemberIdAndRecommendedTrue(memberId) } returns listOf(rec)
            every { recommendRepository.countRecommendedByPlaceIds(listOf(placeId)) } returns listOf(recommendProjection)
            every { reviewRepository.countActiveByPlaceIds(listOf(placeId)) } returns listOf(reviewProjection)

            it("When: 추천 장소 목록 조회 시 Then: 목록과 count를 반환한다") {
                val response = service.getRecommendedPlaces()

                response.places.size shouldBe 1
                response.places[0].placeId shouldBe placeId
                response.places[0].placeName shouldBe place.placeName
                response.places[0].address shouldBe place.address
                response.places[0].roadAddress shouldBe place.roadAddress
                response.places[0].latitude shouldBe place.latitude
                response.places[0].longitude shouldBe place.longitude
                response.places[0].categoryGroupName shouldBe place.categoryGroupName
                response.places[0].categoryName shouldBe place.categoryName
                response.places[0].reviewCount shouldBe 2L
                response.places[0].recommendCount shouldBe 4L
                response.places[0].recommended shouldBe true
            }
        }

        context("Given: 추천 장소 없음") {
            every { memberUtil.currentMemberId() } returns memberId
            every { recommendRepository.findAllByMemberIdAndRecommendedTrue(memberId) } returns emptyList()

            it("When: 추천 장소 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getRecommendedPlaces()
                response.places.shouldBeEmpty()
            }
        }
    }

    describe("PlaceRecommendService.getRecommendedCount()") {

        context("Given: 추천 N개") {
            every { memberUtil.currentMemberId() } returns memberId
            every { recommendRepository.countByMemberIdAndRecommendedTrue(memberId) } returns 7L

            it("When: 추천 수 조회 시 Then: count를 반환한다") {
                val response = service.getRecommendedCount()
                response.recommendCount shouldBe 7L
            }
        }
    }
})