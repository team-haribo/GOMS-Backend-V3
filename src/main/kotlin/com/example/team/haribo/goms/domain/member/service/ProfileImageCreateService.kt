package com.example.team.haribo.goms.domain.member.service

import com.example.team.haribo.goms.domain.member.dto.response.ProfileImageResponse
import org.springframework.web.multipart.MultipartFile

interface ProfileImageCreateService {
    fun execute(image: MultipartFile): ProfileImageResponse
}