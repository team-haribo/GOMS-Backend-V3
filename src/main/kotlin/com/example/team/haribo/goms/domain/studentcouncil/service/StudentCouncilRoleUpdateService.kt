package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.common.enums.Role

interface StudentCouncilRoleUpdateService {
    fun update(memberId: Long, role: Role)
}
