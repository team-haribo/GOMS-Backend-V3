package com.teamharibo.goms.domain.studentcouncil.service.impl

import com.teamharibo.goms.domain.common.enums.Action
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.member.exception.NotFoundMemberException
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.outing.exception.AlreadyOutingException
import com.teamharibo.goms.domain.outing.exception.CannotOutingException
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.fixture.MemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class StudentCouncilForceOutServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val outingRepository = mockk<OutingRepository>()
    val service = StudentCouncilForceOutServiceImpl(memberRepository, outingRepository)

    describe("StudentCouncilForceOutService") {

        context("Given: COMING 상태 멤버") {
            val member = MemberFixture.student(id = 1L, status = Status.COMING)
            every { memberRepository.findByIdForUpdate(1L) } returns member
            every { outingRepository.save(any()) } answers {
                firstArg<com.teamharibo.goms.domain.outing.entity.Outing>().also { it.id = 10L }
            }

            it("When: 강제 외출 시 Then: 상태가 OUTING으로 변경되고 Outing이 저장된다") {
                val response = service.out(1L)
                response.action shouldBe Action.OUT
                response.status shouldBe Status.OUTING
                member.status shouldBe Status.OUTING
                verify(exactly = 1) { outingRepository.save(any()) }
            }
        }

        context("Given: 존재하지 않는 memberId") {
            every { memberRepository.findByIdForUpdate(999L) } returns null

            it("When: 강제 외출 시 Then: NotFoundMemberException이 발생한다") {
                shouldThrow<NotFoundMemberException> {
                    service.out(999L)
                }
            }
        }

        context("Given: CANNOT_OUTING 상태 멤버") {
            every { memberRepository.findByIdForUpdate(3L) } returns MemberFixture.cannotOuting(id = 3L)

            it("When: 강제 외출 시 Then: CannotOutingException이 발생한다") {
                shouldThrow<CannotOutingException> {
                    service.out(3L)
                }
            }
        }

        context("Given: 이미 OUTING 상태 멤버") {
            every { memberRepository.findByIdForUpdate(4L) } returns MemberFixture.outing(id = 4L)

            it("When: 강제 외출 시 Then: AlreadyOutingException이 발생한다") {
                shouldThrow<AlreadyOutingException> {
                    service.out(4L)
                }
            }
        }
    }
})
