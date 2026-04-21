package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ReviewMyListServiceImplTest : DescribeSpec({

    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReviewMyListServiceImpl(reviewRepository, memberUtil)

    val member = MemberFixture.student(id = 1L)
    val place = PlaceFixture.place(id = 3L, placeName = "짬뽕관", categoryName = "중식당", address = "광주 광산구")
    val reviewId = 11L

    describe("ReviewMyListService") {

        context("Given: 내가 작성한 리뷰가 있음") {
            val reviews = listOf(
                ReviewFixture.review(id = reviewId, place = place, member = member, content = "맛있음")
            )

            every { memberUtil.currentMemberId() } returns member.id!!
            every { reviewRepository.findAllActiveByMemberId(member.id!!) } returns reviews

            it("When: 내 리뷰 목록 조회 시 Then: 장소 정보와 함께 반환한다") {
                val response = service.getMyReviews()

                response.reviews.size shouldBe 1
                response.reviews[0].reviewId shouldBe reviewId
                response.reviews[0].placeId shouldBe place.id!!
                response.reviews[0].placeName shouldBe "짬뽕관"
                response.reviews[0].categoryName shouldBe "중식당"
                response.reviews[0].address shouldBe "광주 광산구"
                response.reviews[0].content shouldBe "맛있음"
            }
        }

        context("Given: 내가 작성한 리뷰가 없음") {
            every { memberUtil.currentMemberId() } returns member.id!!
            every { reviewRepository.findAllActiveByMemberId(member.id!!) } returns emptyList()

            it("When: 내 리뷰 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getMyReviews()
                response.reviews.shouldBeEmpty()
            }
        }
    }
})