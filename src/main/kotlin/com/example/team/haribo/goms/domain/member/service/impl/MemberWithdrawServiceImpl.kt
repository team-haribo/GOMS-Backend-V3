package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.exception.MemberWithdrawPasswordMismatchException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.member.service.MemberWithdrawService
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.report.repository.ReviewReportRepository
import com.example.team.haribo.goms.domain.review.repository.ReviewRepository
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
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
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
    private val memberRepository: MemberRepository
) : MemberWithdrawService {

    private val log = LoggerFactory.getLogger(MemberWithdrawServiceImpl::class.java)

    @Transactional
    override fun withdraw(request: MemberWithdrawRequest) {
        val member = memberUtil.currentMember()
        val memberId = member.id!!

        log.info(
            LogFormat.message(
                domain = "MEMBER",
                event = "회원 탈퇴 시도",
                "memberId" to memberId,
                "email" to member.email
            )
        )

        if (!passwordEncoder.matches(request.password, member.password)) {
            log.warn(
                LogFormat.message(
                    domain = "MEMBER",
                    event = "회원 탈퇴 실패",
                    "memberId" to memberId,
                    "email" to member.email,
                    "reason" to "비밀번호 불일치"
                )
            )
            throw MemberWithdrawPasswordMismatchException()
        }

        reviewReportRepository.deleteAllByMemberId(memberId)
        reviewReportRepository.deleteAllByReview_Member_Id(memberId)
        lateRepository.deleteAllByMemberId(memberId)
        reviewRepository.deleteAllByMember_Id(memberId)
        placeRecommendRepository.deleteAllByMember_Id(memberId)
        outingRepository.deleteAllByMember_Id(memberId)
        refreshTokenRedisRepository.deleteByMemberId(memberId)
        memberRepository.delete(member)

        log.info(
            LogFormat.message(
                domain = "MEMBER",
                event = "회원 탈퇴 완료",
                "memberId" to memberId,
                "email" to member.email
            )
        )
    }
}