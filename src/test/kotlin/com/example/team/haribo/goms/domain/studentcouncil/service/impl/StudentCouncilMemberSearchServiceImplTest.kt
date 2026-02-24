package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.outing.exception.EmptyNameException
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class StudentCouncilMemberSearchServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val service = StudentCouncilMemberSearchServiceImpl(memberRepository)

    describe("StudentCouncilMemberSearchService") {

        context("Given: 유효한 이름") {
            val members = listOf(MemberFixture.student(id = 1L))
            every { memberRepository.searchByNameSorted("홍길동") } returns members

            it("When: 이름으로 검색 시 Then: 검색 결과를 반환한다") {
                val response = service.search("홍길동")
                response.students.size shouldBe 1
                response.students[0].name shouldBe "홍길동"
            }
        }

        context("Given: null 이름") {
            it("When: null 이름으로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search(null)
                }
            }
        }

        context("Given: 빈 문자열 이름") {
            it("When: 빈 이름으로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search("")
                }
            }
        }

        context("Given: 공백만 있는 이름") {
            it("When: 공백 이름으로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search("   ")
                }
            }
        }
    }
})
