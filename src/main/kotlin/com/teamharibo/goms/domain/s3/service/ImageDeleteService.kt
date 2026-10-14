package com.teamharibo.goms.domain.s3.service

interface ImageDeleteService {
    fun deleteByUrl(imageUrl: String)
}