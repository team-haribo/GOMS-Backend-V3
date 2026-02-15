package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.common.enums.Status

interface StudentCouncilOutingAllowedService {
    fun update(memberId: Long, status: Status)
}
