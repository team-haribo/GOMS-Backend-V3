package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.StatusConflictException
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class StudentCouncilOutingAllowedServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = StudentCouncilOutingAllowedServiceImpl(memberRepository)

    describe("StudentCouncilOutingAllowedService") {

        context("Given: COMING 상태 멤버 → CANNOT_OUTING으로 변경") {
            val member = MemberFixture.student(id = 1L, status = Status.COMING)
            every { memberRepository.findById(1L) } returns Optional.of(member)

            it("When: 상태 변경 시 Then: CANNOT_OUTING으로 업데이트된다") {
                service.update(1L, Status.CANNOT_OUTING)
                member.status shouldBe Status.CANNOT_OUTING
            }
        }

        context("Given: CANNOT_OUTING 상태 멤버 → COMING으로 변경") {
            val member = MemberFixture.cannotOuting(id = 2L)
            every { memberRepository.findById(2L) } returns Optional.of(member)

            it("When: 상태 변경 시 Then: COMING으로 업데이트된다") {
                service.update(2L, Status.COMING)
                member.status shouldBe Status.COMING
            }
        }

        context("Given: 존재하지 않는 memberId") {
            every { memberRepository.findById(999L) } returns Optional.empty()

            it("When: COMING으로 변경 시 Then: NotFoundMemberException이 발생한다") {
                shouldThrow<NotFoundMemberException> {
                    service.update(999L, Status.COMING)
                }
            }
        }

        context("Given: 요청 상태가 OUTING") {
            it("When: OUTING으로 변경 시도 시 Then: INVALID_REQUEST 예외가 발생한다") {
                shouldThrow<GlobalException> {
                    service.update(1L, Status.OUTING)
                }.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }

        context("Given: 현재 OUTING 상태인 멤버") {
            val outingMember = MemberFixture.outing(id = 4L)
            every { memberRepository.findById(4L) } returns Optional.of(outingMember)

            it("When: 상태 변경 시 Then: StatusConflictException이 발생한다") {
                shouldThrow<StatusConflictException> {
                    service.update(4L, Status.COMING)
                }
            }
        }

        context("Given: 동일 상태로 변경 시도") {
            val comingMember = MemberFixture.student(id = 5L, status = Status.COMING)
            every { memberRepository.findById(5L) } returns Optional.of(comingMember)

            it("When: 이미 COMING인 멤버를 COMING으로 변경 시 Then: StatusConflictException이 발생한다") {
                shouldThrow<StatusConflictException> {
                    service.update(5L, Status.COMING)
                }
            }
        }
    }
})
