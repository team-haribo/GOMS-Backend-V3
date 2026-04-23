package com.example.team.haribo.goms.domain.s3.service.impl

import com.example.team.haribo.goms.domain.s3.exception.EmptyImageException
import com.example.team.haribo.goms.domain.s3.exception.ImageSizeExceededException
import com.example.team.haribo.goms.domain.s3.exception.InvalidImageTypeException
import com.example.team.haribo.goms.global.config.S3Properties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.ByteArrayInputStream

class ImageUploadServiceImplTest : DescribeSpec({

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

    val service = ImageUploadServiceImpl(s3Client, s3Properties)

    describe("ImageUploadService") {

        context("Given: 정상적인 png 이미지") {
            val image = mockk<MultipartFile>()

            every { image.isEmpty } returns false
            every { image.size } returns 1024L
            every { image.contentType } returns "image/png"
            every { image.originalFilename } returns "profile.png"
            every { image.inputStream } returns ByteArrayInputStream("image".toByteArray())
            every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns PutObjectResponse.builder().build()

            it("When: 프로필 이미지 업로드 시 Then: 업로드 URL을 반환한다") {
                val response = service.uploadProfileImage(1L, image)

                response.imageUrl.shouldStartWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/member/1/")
                response.imageUrl.shouldContain(".png")
            }
        }

        context("Given: 빈 파일") {
            val image = mockk<MultipartFile>()

            every { image.isEmpty } returns true

            it("When: 업로드 시 Then: EmptyImageException이 발생한다") {
                shouldThrow<EmptyImageException> {
                    service.uploadProfileImage(1L, image)
                }
            }
        }

        context("Given: 파일 크기가 5MB 초과") {
            val image = mockk<MultipartFile>()

            every { image.isEmpty } returns false
            every { image.size } returns 5 * 1024 * 1024L + 1
            every { image.contentType } returns "image/png"
            every { image.originalFilename } returns "profile.png"

            it("When: 업로드 시 Then: ImageSizeExceededException이 발생한다") {
                shouldThrow<ImageSizeExceededException> {
                    service.uploadProfileImage(1L, image)
                }
            }
        }

        context("Given: 허용되지 않은 contentType") {
            val image = mockk<MultipartFile>()

            every { image.isEmpty } returns false
            every { image.size } returns 1024L
            every { image.contentType } returns "application/pdf"
            every { image.originalFilename } returns "profile.pdf"

            it("When: 업로드 시 Then: InvalidImageTypeException이 발생한다") {
                shouldThrow<InvalidImageTypeException> {
                    service.uploadProfileImage(1L, image)
                }
            }
        }

        context("Given: 확장자가 허용되지 않음") {
            val image = mockk<MultipartFile>()

            every { image.isEmpty } returns false
            every { image.size } returns 1024L
            every { image.contentType } returns "image/png"
            every { image.originalFilename } returns "profile.gif"

            it("When: 업로드 시 Then: InvalidImageTypeException이 발생한다") {
                shouldThrow<InvalidImageTypeException> {
                    service.uploadProfileImage(1L, image)
                }
            }
        }
    }
})