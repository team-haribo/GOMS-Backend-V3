package com.example.team.haribo.goms.domain.s3.service.impl

import com.example.team.haribo.goms.domain.s3.dto.response.ImageUploadResponse
import com.example.team.haribo.goms.domain.s3.exception.EmptyImageException
import com.example.team.haribo.goms.domain.s3.exception.ImageSizeExceededException
import com.example.team.haribo.goms.domain.s3.exception.InvalidImageTypeException
import com.example.team.haribo.goms.domain.s3.service.ImageUploadService
import com.example.team.haribo.goms.global.config.S3Properties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class ImageUploadServiceImpl(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties
) : ImageUploadService {

    private val allowedContentTypes = setOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/webp"
    )

    private val allowedExtensions = setOf(
        "png",
        "jpg",
        "jpeg",
        "webp"
    )

    private val maxFileSize = 5 * 1024 * 1024L

    override fun uploadProfileImage(memberId: Long, image: MultipartFile): ImageUploadResponse {
        validate(image)

        val extension = extractExtension(image)
        val key = "profile/member/$memberId/${UUID.randomUUID()}.$extension"

        val request = PutObjectRequest.builder()
            .bucket(s3Properties.s3.bucket)
            .key(key)
            .contentType(image.contentType)
            .build()

        image.inputStream.use {
            s3Client.putObject(request, RequestBody.fromInputStream(it, image.size))
        }

        return ImageUploadResponse(
            imageUrl = "https://${s3Properties.s3.bucket}.s3.${s3Properties.region.static}.amazonaws.com/$key"
        )
    }

    private fun validate(image: MultipartFile) {
        if (image.isEmpty) {
            throw EmptyImageException()
        }

        if (image.size > maxFileSize) {
            throw ImageSizeExceededException()
        }

        if (image.contentType !in allowedContentTypes) {
            throw InvalidImageTypeException()
        }

        val extension = image.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()

        if (extension !in allowedExtensions) {
            throw InvalidImageTypeException()
        }
    }

    private fun extractExtension(image: MultipartFile): String {
        return image.originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.let { if (it == "jpg") "jpeg" else it }
            ?: throw InvalidImageTypeException()
    }
}