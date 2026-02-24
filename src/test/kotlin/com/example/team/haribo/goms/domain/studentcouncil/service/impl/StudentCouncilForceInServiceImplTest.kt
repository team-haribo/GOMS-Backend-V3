package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Action
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.exception.NotOutingException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.OutingFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class StudentCouncilForceInServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val outingRepository = mockk<OutingRepository>()
    val service = StudentCouncilForceInServiceImpl(memberRepository, outingRepository)

    describe("StudentCouncilForceInService") {

        context("Given: OUTING 상태 멤버 + 활성 Outing 있음") {
            val member = MemberFixture.outing(id = 1L)
            val activeOuting = OutingFixture.active(member)
            every { memberRepository.findById(1L) } returns Optional.of(member)
            every { outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(1L) } returns activeOuting

            it("When: 강제 귀교 시 Then: comingAt이 설정되고 상태가 COMING으로 변경된다") {
                val response = service.`in`(1L)
                response.action shouldBe Action.IN
                response.status shouldBe Status.COMING
                response.outingId shouldBe activeOuting.id!!
                activeOuting.comingAt shouldNotBe null
                member.status shouldBe Status.COMING
            }
        }

        context("Given: 존재하지 않는 memberId") {
            every { memberRepository.findById(999L) } returns Optional.empty()

            it("When: 강제 귀교 시 Then: NotFoundMemberException이 발생한다") {
                shouldThrow<NotFoundMemberException> {
                    service.`in`(999L)
                }
            }
        }

        context("Given: 활성 Outing 없음") {
            val member = MemberFixture.student(id = 2L)
            every { memberRepository.findById(2L) } returns Optional.of(member)
            every { outingRepository.findTopByMemberIdAndComingAtIsNullOrderByIdDesc(2L) } returns null

            it("When: 강제 귀교 시 Then: NotOutingException이 발생한다") {
                shouldThrow<NotOutingException> {
                    service.`in`(2L)
                }
            }
        }
    }
})
