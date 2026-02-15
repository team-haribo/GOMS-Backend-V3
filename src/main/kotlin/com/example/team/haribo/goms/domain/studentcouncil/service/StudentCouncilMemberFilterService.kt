package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse

interface StudentCouncilMemberFilterService {
    fun filter(
        name: String?,
        grade: Long?,
        department: Department?,
        status: Status?,
        role: Role?
    ): StudentsListResponse
}
