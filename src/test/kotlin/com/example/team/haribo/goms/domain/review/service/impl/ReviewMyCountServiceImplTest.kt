package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ReviewMyCountServiceImplTest : DescribeSpec({

    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReviewMyCountServiceImpl(reviewRepository, memberUtil)

    val member = MemberFixture.student(id = 1L)

    describe("ReviewMyCountService") {

        context("Given: 내가 작성한 리뷰가 3개") {
            every { memberUtil.currentMemberId() } returns member.id!!
            every { reviewRepository.countAllByMemberIdAndDeletedAtIsNull(member.id!!) } returns 3L

            it("When: 내 리뷰 개수 조회 시 Then: 3을 반환한다") {
                val response = service.getMyReviewCount()
                response.count shouldBe 3L
            }
        }

        context("Given: 내가 작성한 리뷰가 없음") {
            every { memberUtil.currentMemberId() } returns member.id!!
            every { reviewRepository.countAllByMemberIdAndDeletedAtIsNull(member.id!!) } returns 0L

            it("When: 내 리뷰 개수 조회 시 Then: 0을 반환한다") {
                val response = service.getMyReviewCount()
                response.count shouldBe 0L
            }
        }
    }
})