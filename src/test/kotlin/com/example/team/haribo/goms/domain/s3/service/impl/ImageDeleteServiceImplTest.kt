package com.example.team.haribo.goms.domain.s3.service.impl

import com.example.team.haribo.goms.global.config.S3Properties
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse

class ImageDeleteServiceImplTest : DescribeSpec({

    val s3Client = mockk<S3Client>()
    val s3Properties = S3Properties(
        credentials = S3Properties.Credentials(
            accessKey = "test-access",
            secretKey = "test-secret"
        ),
        region = S3Properties.Region(
            static = "ap-northeast-2"
        ),
        s3 = S3Properties.S3(
            bucket = "test-bucket"
        )
    )

    val service = ImageDeleteServiceImpl(s3Client, s3Properties)

    describe("ImageDeleteService") {

        context("Given: S3 이미지 URL") {
            val requestSlot = slot<DeleteObjectRequest>()

            every { s3Client.deleteObject(capture(requestSlot)) } returns DeleteObjectResponse.builder().build()

            it("When: URL로 삭제 시 Then: bucket과 key를 추출해 삭제한다") {
                service.deleteByUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/member/1/test.png")

                verify(exactly = 1) { s3Client.deleteObject(any<DeleteObjectRequest>()) }
                requestSlot.captured.bucket() shouldBe "test-bucket"
                requestSlot.captured.key() shouldBe "profile/member/1/test.png"
            }
        }
    }
})