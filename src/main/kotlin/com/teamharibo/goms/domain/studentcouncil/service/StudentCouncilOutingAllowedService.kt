package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.common.enums.Status

interface StudentCouncilOutingAllowedService {
    fun update(memberId: Long, status: Status)
}
