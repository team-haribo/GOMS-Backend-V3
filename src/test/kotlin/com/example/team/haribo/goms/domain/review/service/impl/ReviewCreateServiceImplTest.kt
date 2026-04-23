package com.example.team.haribo.goms.domain.review.service.impl

import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.review.dto.request.ReviewCreateRequest
import com.example.team.haribo.goms.domain.review.exception.AlreadyReviewedPlaceException
import com.example.team.haribo.goms.domain.review.exception.ReviewContentEmptyException
import com.example.team.haribo.goms.domain.review.exception.ReviewContentTooLongException
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.PlaceFixture
import com.example.team.haribo.goms.fixture.ReviewFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class ReviewCreateServiceImplTest : DescribeSpec({

    val placeRepository = mockk<PlaceRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val memberUtil = mockk<MemberUtil>()
    val service = ReviewCreateServiceImpl(placeRepository, reviewRepository, memberUtil)

    val member = MemberFixture.student(id = 1L)
    val place = PlaceFixture.place(id = 1L, isActive = true)
    val placeId = 1L

    describe("ReviewCreateService") {

        context("Given: 유효한 내용 + 첫 리뷰") {
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { memberUtil.currentMember() } returns member
            every { reviewRepository.existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId, member.id!!) } returns false
            every { reviewRepository.save(any()) } answers {
                ReviewFixture.review(
                    id = 10L,
                    place = place,
                    member = member,
                    content = "좋은 장소입니다."
                )
            }

            it("When: 리뷰 작성 시 Then: 저장하고 review_id를 반환한다") {
                val response = service.create(placeId, ReviewCreateRequest("좋은 장소입니다."))
                response.review_id shouldBe 10L
                verify(exactly = 1) { reviewRepository.save(any()) }
            }
        }

        context("Given: 빈 내용") {
            it("When: 빈 문자열로 작성 시 Then: ReviewContentEmptyException이 발생한다") {
                shouldThrow<ReviewContentEmptyException> {
                    service.create(placeId, ReviewCreateRequest(""))
                }
            }
        }

        context("Given: 공백만 있는 내용") {
            it("When: 공백 내용으로 작성 시 Then: ReviewContentEmptyException이 발생한다") {
                shouldThrow<ReviewContentEmptyException> {
                    service.create(placeId, ReviewCreateRequest("   "))
                }
            }
        }

        context("Given: 500자 초과 내용") {
            it("When: 501자 내용으로 작성 시 Then: ReviewContentTooLongException이 발생한다") {
                shouldThrow<ReviewContentTooLongException> {
                    service.create(placeId, ReviewCreateRequest(ReviewFixture.longContent(501)))
                }
            }
        }

        context("Given: 정확히 500자 내용") {
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { memberUtil.currentMember() } returns member
            every { reviewRepository.existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId, member.id!!) } returns false
            every { reviewRepository.save(any()) } answers {
                ReviewFixture.review(
                    id = 2L,
                    place = place,
                    member = member,
                    content = ReviewFixture.longContent(500)
                )
            }

            it("When: 500자 내용으로 작성 시 Then: 정상적으로 저장된다") {
                val response = service.create(placeId, ReviewCreateRequest(ReviewFixture.longContent(500)))
                response.review_id shouldBe 2L
            }
        }

        context("Given: 존재하지 않는 placeId") {
            every { placeRepository.findByIdAndIsActiveTrue(999L) } returns Optional.empty()

            it("When: 리뷰 작성 시 Then: NotFoundPlaceException이 발생한다") {
                shouldThrow<NotFoundPlaceException> {
                    service.create(999L, ReviewCreateRequest("내용"))
                }
            }
        }

        context("Given: 이미 리뷰한 장소") {
            every { placeRepository.findByIdAndIsActiveTrue(placeId) } returns Optional.of(place)
            every { memberUtil.currentMember() } returns member
            every { reviewRepository.existsByPlaceIdAndMemberIdAndDeletedAtIsNull(placeId, member.id!!) } returns true

            it("When: 리뷰 작성 시 Then: AlreadyReviewedPlaceException이 발생한다") {
                shouldThrow<AlreadyReviewedPlaceException> {
                    service.create(placeId, ReviewCreateRequest("또 리뷰합니다."))
                }
            }
        }
    }
})