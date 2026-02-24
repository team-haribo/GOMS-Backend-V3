package com.example.team.haribo.goms.domain.outing.service.impl

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.fixture.OutingFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OutingStudentListServiceImplTest : DescribeSpec({

    val outingRepository = mockk<OutingRepository>()
    val service = OutingStudentListServiceImpl(outingRepository)

    describe("OutingStudentListService") {

        context("Given: 활성 외출자 목록 있음") {
            val member = MemberFixture.student()
            val outing = OutingFixture.active(member)
            every { outingRepository.findAllActiveWithMember() } returns listOf(outing)

            it("When: 외출 학생 목록 조회 시 Then: DTO로 매핑된 목록을 반환한다") {
                val response = service.getList()
                response.students.size shouldBe 1
                response.students[0].name shouldBe member.name
                response.students[0].grade shouldBe member.grade.toLong()
                response.students[0].department shouldBe Department.SW.name
            }
        }

        context("Given: 외출자 없음") {
            every { outingRepository.findAllActiveWithMember() } returns emptyList()

            it("When: 외출 학생 목록 조회 시 Then: 빈 리스트를 반환한다") {
                val response = service.getList()
                response.students.shouldBeEmpty()
            }
        }
    }
})
