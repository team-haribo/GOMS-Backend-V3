package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import java.time.LocalDate

interface StudentCouncilLateListService {
    fun list(date: LocalDate?): LateStudentsListResponse
}
