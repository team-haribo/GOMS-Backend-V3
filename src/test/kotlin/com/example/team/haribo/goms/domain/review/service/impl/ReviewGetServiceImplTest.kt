package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ReviewGetServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val service = ReviewGetServiceImpl(placeRepository, reviewRepository)

    val place = PlaceFixture.place(id = 1L)
    val placeId = place.id!!

    describe("ReviewGetService.getPlaceReviews()") {

        context("Given: 존재하는 placeId + 리뷰 있음") {
            val member = MemberFixture.student()
            val reviews = listOf(
                ReviewFixture.review(id = 1L, place = place, member = member, content = "좋아요"),
                ReviewFixture.review(id = 2L, place = place, member = member, content = "또 왔어요")
            )
            every { placeRepository.existsById(placeId) } returns true
            every { reviewRepository.findAllActiveByPlaceId(placeId) } returns reviews

            it("When: 리뷰 목록 조회 시 Then: 리뷰 목록을 반환한다") {
                val response = service.getPlaceReviews(placeId)
                response.reviews.size shouldBe 2
                response.reviews[0].review_id shouldBe 1L
                response.reviews[0].content shouldBe "좋아요"
                response.reviews[0].name shouldBe member.name
                response.reviews[0].grade shouldBe member.grade
                response.reviews[0].department shouldBe member.department
            }
        }

        context("Given: 존재하는 placeId + 리뷰 없음") {
            every { placeRepository.existsById(placeId) } returns true
            every { reviewRepository.findAllActiveByPlaceId(placeId) } returns emptyList()

            it("When: 리뷰 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getPlaceReviews(placeId)
                response.reviews.shouldBeEmpty()
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { placeRepository.existsById(999L) } returns false

            it("When: 리뷰 목록 조회 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.getPlaceReviews(999L)
                }
            }
        }
    }

    describe("ReviewGetService.countPlaceReviews()") {

        context("Given: 존재하는 placeId + 리뷰 5개") {
            every { placeRepository.existsById(placeId) } returns true
            every { reviewRepository.countActiveByPlaceId(placeId) } returns 5L

            it("When: 리뷰 수 조회 시 Then: 5를 반환한다") {
                val response = service.countPlaceReviews(placeId)
                response.reviewCount shouldBe 5L
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { placeRepository.existsById(999L) } returns false

            it("When: 리뷰 수 조회 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.countPlaceReviews(999L)
                }
            }
        }
    }
})