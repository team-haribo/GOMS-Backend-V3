package com.example.team.haribo.goms.domain.outing.service

import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentListResponse

interface OutingStudentListService {
    fun getList(): OutingStudentListResponse
}
