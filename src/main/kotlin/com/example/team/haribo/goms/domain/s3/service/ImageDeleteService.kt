package com.example.team.haribo.goms.domain.s3.service

interface ImageDeleteService {
    fun deleteByUrl(imageUrl: String)
}