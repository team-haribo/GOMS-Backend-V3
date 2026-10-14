package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentsListResponse

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
