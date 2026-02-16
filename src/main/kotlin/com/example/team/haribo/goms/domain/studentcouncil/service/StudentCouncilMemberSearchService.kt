package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.StudentSearchResponse

interface StudentCouncilMemberSearchService {
    fun search(name: String?): StudentSearchResponse
}
