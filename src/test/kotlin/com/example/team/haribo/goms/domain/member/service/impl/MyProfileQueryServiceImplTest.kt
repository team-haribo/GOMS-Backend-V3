package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MyProfileQueryServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val service = MyProfileQueryServiceImpl(memberUtil)

    describe("MyProfileQueryService") {
        context("Given: 로그인한 사용자가 있음") {
            val member = MemberFixture.student(id = 1L, profileImageUrl = "profile.png")
            every { memberUtil.currentMember() } returns member

            it("When: 내 프로필 조회 시 Then: 프로필 정보를 반환한다") {
                val response = service.execute()

                response.memberId shouldBe member.id!!
                response.email shouldBe member.email
                response.name shouldBe member.name
                response.grade shouldBe member.grade
                response.department shouldBe member.department
                response.gender shouldBe member.gender
                response.role shouldBe member.role
                response.status shouldBe member.status
                response.profileImageUrl shouldBe "profile.png"
            }
        }
    }
})