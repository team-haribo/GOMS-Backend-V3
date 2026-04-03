package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.outing.exception.EmptyNameException
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.OutingFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OutingStudentSearchServiceImplTest : DescribeSpec({

    val outingRepository = mockk<OutingRepository>()
    val service = OutingStudentSearchServiceImpl(outingRepository)

    describe("OutingStudentSearchService") {

        context("Given: 유효한 검색어") {
            val member = MemberFixture.student()
            val outing = OutingFixture.active(member)
            every { outingRepository.searchActiveWithMemberByName("홍길동") } returns listOf(outing)

            it("When: 이름으로 검색 시 Then: 매칭 결과를 반환한다") {
                val response = service.search("홍길동")
                response.students.size shouldBe 1
                response.students[0].name shouldBe member.name
                response.students[0].role shouldBe member.role
                response.students[0].status shouldBe member.status
            }
        }

        context("Given: null 검색어") {
            it("When: null 이름으로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search(null)
                }
            }
        }

        context("Given: 빈 문자열 검색어") {
            it("When: 빈 문자열로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search("")
                }
            }
        }

        context("Given: 공백만 있는 검색어") {
            it("When: 공백 문자열로 검색 시 Then: EmptyNameException이 발생한다") {
                shouldThrow<EmptyNameException> {
                    service.search("   ")
                }
            }
        }
    }
})