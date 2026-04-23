package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.s3.dto.response.ImageUploadResponse
import com.example.team.haribo.goms.domain.s3.service.ImageDeleteService
import com.example.team.haribo.goms.domain.s3.service.ImageUploadService
import com.example.team.haribo.goms.fixture.MemberFixture
import com.example.team.haribo.goms.global.util.MemberUtil
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.multipart.MultipartFile

class ProfileImageUpdateServiceImplTest : DescribeSpec({

    val memberUtil = mockk<MemberUtil>()
    val imageUploadService = mockk<ImageUploadService>()
    val imageDeleteService = mockk<ImageDeleteService>()
    val service = ProfileImageUpdateServiceImpl(memberUtil, imageUploadService, imageDeleteService)

    val image = mockk<MultipartFile>()

    describe("ProfileImageUpdateService") {

        context("Given: 기존 프로필 이미지가 없음") {
            val member = MemberFixture.student(id = 1L, profileImageUrl = null)

            every { memberUtil.currentMember() } returns member
            every { imageUploadService.uploadProfileImage(member.id!!, image) } returns ImageUploadResponse(
                imageUrl = "https://new-image.url/profile.png"
            )

            it("When: 프로필 이미지 업데이트 시 Then: 새 이미지 URL이 저장된다") {
                val response = service.execute(image)

                response.imageUrl shouldBe "https://new-image.url/profile.png"
                member.profileImageUrl shouldBe "https://new-image.url/profile.png"
            }
        }

        context("Given: 기존 프로필 이미지가 있음") {
            val member = MemberFixture.student(id = 1L, profileImageUrl = "https://old-image.url/profile.png")

            every { memberUtil.currentMember() } returns member
            every { imageUploadService.uploadProfileImage(member.id!!, image) } returns ImageUploadResponse(
                imageUrl = "https://new-image.url/profile.png"
            )
            justRun { imageDeleteService.deleteByUrl("https://old-image.url/profile.png") }

            it("When: 프로필 이미지 업데이트 시 Then: 기존 이미지를 삭제하고 새 이미지 URL을 저장한다") {
                val response = service.execute(image)

                response.imageUrl shouldBe "https://new-image.url/profile.png"
                member.profileImageUrl shouldBe "https://new-image.url/profile.png"
                verify(exactly = 1) { imageDeleteService.deleteByUrl("https://old-image.url/profile.png") }
            }
        }
    }
})