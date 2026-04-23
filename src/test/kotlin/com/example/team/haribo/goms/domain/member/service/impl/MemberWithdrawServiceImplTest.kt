package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.exception.MemberWithdrawPasswordMismatchException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class MemberWithdrawServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val reviewReportRepository = mockk<ReviewReportRepository>()
    val lateRepository = mockk<LateRepository>()
    val reviewRepository = mockk<ReviewRepository>()
    val placeRecommendRepository = mockk<PlaceRecommendRepository>()
    val outingRepository = mockk<OutingRepository>()
    val refreshTokenRedisRepository = mockk<RefreshTokenRedisRepository>()
    val memberRepository = mockk<MemberRepository>()

    val service = MemberWithdrawServiceImpl(
        memberUtil,
        passwordEncoder,
        reviewReportRepository,
        lateRepository,
        reviewRepository,
        placeRecommendRepository,
        outingRepository,
        refreshTokenRedisRepository,
        memberRepository
    )

    val member = MemberFixture.student(id = 1L)

    describe("MemberWithdrawService") {

        context("Given: 비밀번호가 일치함") {
            every { memberUtil.currentMember() } returns member
            every { passwordEncoder.matches("1234", member.password) } returns true
            every { reviewReportRepository.deleteAllByMemberId(member.id!!) } returns 1L
            every { reviewReportRepository.deleteAllByReview_Member_Id(member.id!!) } returns 1L
            every { lateRepository.deleteAllByMemberId(member.id!!) } returns 1L
            every { reviewRepository.deleteAllByMember_Id(member.id!!) } returns 1L
            every { placeRecommendRepository.deleteAllByMember_Id(member.id!!) } returns 1L
            every { outingRepository.deleteAllByMember_Id(member.id!!) } returns 1L
            justRun { refreshTokenRedisRepository.deleteByMemberId(member.id!!) }
            justRun { memberRepository.delete(member) }

            it("When: 회원 탈퇴 시 Then: 연관 데이터와 함께 회원이 삭제된다") {
                service.withdraw(MemberWithdrawRequest(password = "1234"))

                verify(exactly = 1) { reviewReportRepository.deleteAllByMemberId(member.id!!) }
                verify(exactly = 1) { reviewReportRepository.deleteAllByReview_Member_Id(member.id!!) }
                verify(exactly = 1) { lateRepository.deleteAllByMemberId(member.id!!) }
                verify(exactly = 1) { reviewRepository.deleteAllByMember_Id(member.id!!) }
                verify(exactly = 1) { placeRecommendRepository.deleteAllByMember_Id(member.id!!) }
                verify(exactly = 1) { outingRepository.deleteAllByMember_Id(member.id!!) }
                verify(exactly = 1) { refreshTokenRedisRepository.deleteByMemberId(member.id!!) }
                verify(exactly = 1) { memberRepository.delete(member) }
            }
        }

        context("Given: 비밀번호가 일치하지 않음") {
            every { memberUtil.currentMember() } returns member
            every { passwordEncoder.matches("wrong", member.password) } returns false

            it("When: 회원 탈퇴 시 Then: MemberWithdrawPasswordMismatchException이 발생한다") {
                shouldThrow<MemberWithdrawPasswordMismatchException> {
                    service.withdraw(MemberWithdrawRequest(password = "wrong"))
                }
            }
        }
    }
})