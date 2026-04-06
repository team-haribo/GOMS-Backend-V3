package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.member.dto.response.ProfileImageResponse
import com.example.team.haribo.goms.domain.member.exception.AlreadyProfileImageException
import com.example.team.haribo.goms.domain.member.service.ProfileImageCreateService
import com.example.team.haribo.goms.domain.s3.service.ImageUploadService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ProfileImageCreateServiceImpl(
    private val memberUtil: MemberUtil,
    private val imageUploadService: ImageUploadService
) : ProfileImageCreateService {

    @Transactional
    override fun execute(image: MultipartFile): ProfileImageResponse {
        val member = memberUtil.currentMember()

        if (member.profileImageUrl != null) {
            throw AlreadyProfileImageException()
        }

        val uploaded = imageUploadService.uploadProfileImage(member.id!!, image)
        member.profileImageUrl = uploaded.imageUrl

        return ProfileImageResponse(
            imageUrl = member.profileImageUrl
        )
    }
}