package com.example.team.haribo.goms.domain.s3.service

import com.example.team.haribo.goms.domain.s3.dto.response.ImageUploadResponse
import org.springframework.web.multipart.MultipartFile

interface ImageUploadService {
    fun uploadProfileImage(memberId: Long, image: MultipartFile): ImageUploadResponse
}