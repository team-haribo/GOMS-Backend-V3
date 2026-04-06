package com.example.team.haribo.goms.domain.member.service.impl

import com.example.team.haribo.goms.domain.member.exception.NotExistsProfileImageException
import com.example.team.haribo.goms.domain.member.service.ProfileImageDeleteService
import com.example.team.haribo.goms.domain.s3.service.ImageDeleteService
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileImageDeleteServiceImpl(
    private val memberUtil: MemberUtil,
    private val imageDeleteService: ImageDeleteService
) : ProfileImageDeleteService {

    @Transactional
    override fun execute() {
        val member = memberUtil.currentMember()
        val currentImageUrl = member.profileImageUrl ?: throw NotExistsProfileImageException()

        imageDeleteService.deleteByUrl(currentImageUrl)
        member.profileImageUrl = null
    }
}