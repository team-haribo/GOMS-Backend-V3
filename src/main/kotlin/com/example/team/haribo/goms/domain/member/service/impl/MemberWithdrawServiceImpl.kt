package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.auth.repository.AuthRefreshTokenRepository
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.exception.MemberWithdrawPasswordMismatchException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.member.service.MemberWithdrawService
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberWithdrawServiceImpl(
    private val memberUtil: MemberUtil,
    private val passwordEncoder: PasswordEncoder,
    private val reviewReportRepository: ReviewReportRepository,
    private val lateRepository: LateRepository,
    private val reviewRepository: ReviewRepository,
    private val placeRecommendRepository: PlaceRecommendRepository,
    private val outingRepository: OutingRepository,
    private val authRefreshTokenRepository: AuthRefreshTokenRepository,
    private val memberRepository: MemberRepository
) : MemberWithdrawService {

    @Transactional
    override fun withdraw(request: MemberWithdrawRequest) {
        val member = memberUtil.currentMember()
        val memberId = member.id!!

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw MemberWithdrawPasswordMismatchException()
        }

        reviewReportRepository.deleteAllByMemberId(memberId)
        reviewReportRepository.deleteAllByReview_Member_Id(memberId)
        lateRepository.deleteAllByMember_Id(memberId)
        reviewRepository.deleteAllByMember_Id(memberId)
        placeRecommendRepository.deleteAllByMember_Id(memberId)
        outingRepository.deleteAllByMember_Id(memberId)
        authRefreshTokenRepository.deleteAllByMemberId(memberId)
        memberRepository.delete(member)
    }
}