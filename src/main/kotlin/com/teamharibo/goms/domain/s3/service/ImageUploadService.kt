package com.teamharibo.goms.domain.s3.service

import com.teamharibo.goms.domain.s3.dto.response.ImageUploadResponse
import org.springframework.web.multipart.MultipartFile

interface ImageUploadService {
    fun uploadProfileImage(memberId: Long, image: MultipartFile): ImageUploadResponse
}