package com.teamharibo.goms.domain.member.service

import com.teamharibo.goms.domain.member.dto.response.ProfileImageResponse
import org.springframework.web.multipart.MultipartFile

interface ProfileImageUpdateService {
    fun execute(image: MultipartFile): ProfileImageResponse
}