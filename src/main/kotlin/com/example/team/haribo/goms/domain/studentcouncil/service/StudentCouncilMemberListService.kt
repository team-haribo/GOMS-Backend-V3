package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse

interface StudentCouncilMemberListService {
    fun list(): StudentsListResponse
}
