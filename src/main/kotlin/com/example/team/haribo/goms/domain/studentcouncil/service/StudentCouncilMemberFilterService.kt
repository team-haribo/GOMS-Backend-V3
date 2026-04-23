package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.common.enums.Department
import com.example.team.haribo.goms.domain.common.enums.Gender
import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentsListResponse

interface StudentCouncilMemberFilterService {
    fun filter(
        name: String?,
        grade: Int?,
        department: Department?,
        gender: Gender?,
        status: Status?,
        role: Role?
    ): StudentsListResponse
}
