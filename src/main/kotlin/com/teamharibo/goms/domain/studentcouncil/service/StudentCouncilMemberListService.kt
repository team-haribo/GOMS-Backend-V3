package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentsListResponse

interface StudentCouncilMemberListService {
    fun list(): StudentsListResponse
}
