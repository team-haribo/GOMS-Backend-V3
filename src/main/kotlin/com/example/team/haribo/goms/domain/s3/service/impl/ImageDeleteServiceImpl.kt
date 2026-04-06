package com.example.team.haribo.goms.domain.s3.service.impl

import com.example.team.haribo.goms.domain.s3.service.ImageDeleteService
import com.example.team.haribo.goms.global.config.S3Properties
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import java.net.URI

@Service
class ImageDeleteServiceImpl(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties
) : ImageDeleteService {

    override fun deleteByUrl(imageUrl: String) {
        val key = URI(imageUrl).path.removePrefix("/")

        val request = DeleteObjectRequest.builder()
            .bucket(s3Properties.s3.bucket)
            .key(key)
            .build()

        s3Client.deleteObject(request)
    }
}