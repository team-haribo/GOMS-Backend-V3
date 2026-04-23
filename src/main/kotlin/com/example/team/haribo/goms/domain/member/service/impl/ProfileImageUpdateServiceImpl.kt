package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.member.dto.response.ProfileImageResponse
import com.example.team.haribo.goms.domain.member.service.ProfileImageUpdateService
import com.example.team.haribo.goms.domain.s3.service.ImageDeleteService
import com.example.team.haribo.goms.domain.s3.service.ImageUploadService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ProfileImageUpdateServiceImpl(
    private val memberUtil: MemberUtil,
    private val imageUploadService: ImageUploadService,
    private val imageDeleteService: ImageDeleteService
) : ProfileImageUpdateService {

    @Transactional
    override fun execute(image: MultipartFile): ProfileImageResponse {
        val member = memberUtil.currentMember()
        val currentImageUrl = member.profileImageUrl

        val uploaded = imageUploadService.uploadProfileImage(member.id!!, image)
        member.profileImageUrl = uploaded.imageUrl

        if (currentImageUrl != null) {
            imageDeleteService.deleteByUrl(currentImageUrl)
        }

        return ProfileImageResponse(
            imageUrl = member.profileImageUrl
        )
    }
}