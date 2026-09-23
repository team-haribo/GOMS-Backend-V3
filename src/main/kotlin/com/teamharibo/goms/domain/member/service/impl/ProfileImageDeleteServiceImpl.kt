package com.teamharibo.goms.domain.member.service.impl

import com.teamharibo.goms.domain.member.exception.NotExistsProfileImageException
import com.teamharibo.goms.domain.member.service.ProfileImageDeleteService
import com.teamharibo.goms.domain.s3.service.ImageDeleteService
import com.teamharibo.goms.global.util.MemberUtil
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