package com.example.team.haribo.goms.domain.late.service.impl

import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.fixture.LateFixture
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.Pageable

class LateRankServiceImplTest : DescribeSpec({

    val lateRepository = mockk<LateRepository>()
    val service = LateRankServiceImpl(lateRepository)

    describe("LateRankService") {

        context("Given: Top5 데이터 있음") {
            val member1 = MemberFixture.student(id = 1L)
            val member2 = MemberFixture.student(id = 2L).also { it.name = "김철수" }
            val lates = listOf(
                LateFixture.late(member = member1, lateCount = 10L),
                LateFixture.late(member = member2, lateCount = 7L)
            )
            every { lateRepository.findLateRank(any<Pageable>()) } returns lates

            it("When: Top5 조회 시 Then: lateCount 내림차순 DTO를 반환한다") {
                val response = service.getTop5()
                response.students.size shouldBe 2
                response.students[0].memberId shouldBe member1.id!!
                response.students[0].name shouldBe member1.name
            }
        }

        context("Given: 데이터 없음") {
            every { lateRepository.findLateRank(any<Pageable>()) } returns emptyList()

            it("When: Top5 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getTop5()
                response.students.shouldBeEmpty()
            }
        }
    }
})
