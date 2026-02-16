package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import java.time.LocalDate

interface StudentCouncilLateListService {
    fun list(date: LocalDate?): LateStudentsListResponse
}
