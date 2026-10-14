package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.response.OutingStudentListResponse

interface OutingStudentListService {
    fun getList(): OutingStudentListResponse
}
