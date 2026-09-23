package com.teamharibo.goms.domain.member.service.impl

import com.teamharibo.goms.domain.member.exception.NotExistsProfileImageException
import com.teamharibo.goms.domain.s3.service.ImageDeleteService
import com.teamharibo.goms.fixture.MemberFixture
import com.teamharibo.goms.global.util.MemberUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class ProfileImageDeleteServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val imageDeleteService = mockk<ImageDeleteService>()
    val service = ProfileImageDeleteServiceImpl(memberUtil, imageDeleteService)

    describe("ProfileImageDeleteService") {

        context("Given: 프로필 이미지가 존재함") {
            val member = MemberFixture.student(id = 1L, profileImageUrl = "https://image.url/profile.png")
            every { memberUtil.currentMember() } returns member
            justRun { imageDeleteService.deleteByUrl(member.profileImageUrl!!) }

            it("When: 프로필 이미지 삭제 시 Then: S3 이미지 삭제 후 profileImageUrl이 null이 된다") {
                service.execute()

                verify(exactly = 1) { imageDeleteService.deleteByUrl("https://image.url/profile.png") }
                member.profileImageUrl shouldBe null
            }
        }

        context("Given: 프로필 이미지가 존재하지 않음") {
            val member = MemberFixture.student(id = 1L, profileImageUrl = null)
            every { memberUtil.currentMember() } returns member

            it("When: 프로필 이미지 삭제 시 Then: NotExistsProfileImageException이 발생한다") {
                shouldThrow<NotExistsProfileImageException> {
                    service.execute()
                }
            }
        }
    }
})