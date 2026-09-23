package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.common.enums.Role

interface StudentCouncilRoleUpdateService {
    fun update(memberId: Long, role: Role)
}
