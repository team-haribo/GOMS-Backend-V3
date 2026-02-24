package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class StudentCouncilMemberListServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = StudentCouncilMemberListServiceImpl(memberRepository)

    describe("StudentCouncilMemberListService") {

        context("Given: 멤버 목록 있음") {
            val members = listOf(
                MemberFixture.student(id = 1L),
                MemberFixture.council(id = 2L)
            )
            every { memberRepository.findAllSorted() } returns members

            it("When: 목록 조회 시 Then: DTO로 매핑된 목록을 반환한다") {
                val response = service.list()
                response.students.size shouldBe 2
                response.students[0].memberId shouldBe 1L
                response.students[1].memberId shouldBe 2L
            }
        }

        context("Given: 멤버 없음") {
            every { memberRepository.findAllSorted() } returns emptyList()

            it("When: 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.list()
                response.students.shouldBeEmpty()
            }
        }
    }
})
