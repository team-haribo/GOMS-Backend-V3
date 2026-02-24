package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.member.exception.NotFoundMemberException
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.RoleConflictException
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class StudentCouncilRoleUpdateServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = StudentCouncilRoleUpdateServiceImpl(memberRepository)

    describe("StudentCouncilRoleUpdateService") {

        context("Given: ROLE_STUDENT 멤버 → ROLE_STUDENT_COUNCIL로 변경") {
            val member = MemberFixture.student(id = 1L)
            every { memberRepository.findById(1L) } returns Optional.of(member)

            it("When: 역할 변경 시 Then: ROLE_STUDENT_COUNCIL로 업데이트된다") {
                service.update(1L, Role.ROLE_STUDENT_COUNCIL)
                member.role shouldBe Role.ROLE_STUDENT_COUNCIL
            }
        }

        context("Given: 존재하지 않는 memberId") {
            every { memberRepository.findById(999L) } returns Optional.empty()

            it("When: 역할 변경 시 Then: NotFoundMemberException이 발생한다") {
                shouldThrow<NotFoundMemberException> {
                    service.update(999L, Role.ROLE_STUDENT_COUNCIL)
                }
            }
        }

        context("Given: 동일 역할로 변경 시도") {
            val member = MemberFixture.student(id = 2L)
            every { memberRepository.findById(2L) } returns Optional.of(member)

            it("When: ROLE_STUDENT → ROLE_STUDENT로 변경 시 Then: RoleConflictException이 발생한다") {
                shouldThrow<RoleConflictException> {
                    service.update(2L, Role.ROLE_STUDENT)
                }
            }
        }
    }
})
