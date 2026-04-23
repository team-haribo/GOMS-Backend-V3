package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class StudentCouncilMemberFilterServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = StudentCouncilMemberFilterServiceImpl(memberRepository)

    describe("StudentCouncilMemberFilterService") {

        context("Given: 모든 파라미터 null") {
            val allMembers = listOf(MemberFixture.student(id = 1L), MemberFixture.council(id = 2L))
            every {
                memberRepository.filterSorted(null, null, null, null, null, null)
            } returns allMembers

            it("When: 필터 조회 시 Then: 전체 목록을 반환한다") {
                val response = service.filter(null, null, null, null, null, null)
                response.students.size shouldBe 2
                response.students[0].role shouldBe allMembers[0].role
                response.students[0].status shouldBe allMembers[0].status
                response.students[1].role shouldBe allMembers[1].role
                response.students[1].status shouldBe allMembers[1].status
            }
        }

        context("Given: 학년 필터 (grade=1)") {
            val grade1Members = listOf(MemberFixture.student(id = 1L))
            every {
                memberRepository.filterSorted(null, 1, null, null, null, null)
            } returns grade1Members

            it("When: 학년으로 필터 시 Then: 해당 학년 멤버만 반환한다") {
                val response = service.filter(null, 1, null, null, null, null)
                response.students.size shouldBe 1
                response.students[0].grade shouldBe 1
                response.students[0].role shouldBe grade1Members[0].role
                response.students[0].status shouldBe grade1Members[0].status
            }
        }

        context("Given: 이름+학과 복합 필터") {
            val filtered = listOf(MemberFixture.student(id = 1L))
            every {
                memberRepository.filterSorted("홍길동", null, Department.SW, null, null, null)
            } returns filtered

            it("When: 이름과 학과로 필터 시 Then: AND 조건 결과를 반환한다") {
                val response = service.filter("홍길동", null, Department.SW, null, null, null)
                response.students.size shouldBe 1
                response.students[0].department shouldBe Department.SW
                response.students[0].role shouldBe filtered[0].role
                response.students[0].status shouldBe filtered[0].status
            }
        }

        context("Given: 성별 필터 (gender=MAN)") {
            val maleMembers = listOf(MemberFixture.student(id = 1L))
            every {
                memberRepository.filterSorted(null, null, null, Gender.MALE, null, null)
            } returns maleMembers

            it("When: 성별로 필터 시 Then: 해당 성별 멤버만 반환한다") {
                val response = service.filter(null, null, null, Gender.MALE, null, null)
                response.students.size shouldBe 1
                response.students[0].role shouldBe maleMembers[0].role
                response.students[0].status shouldBe maleMembers[0].status
            }
        }
    }
})