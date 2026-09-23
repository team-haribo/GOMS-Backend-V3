package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.StudentSearchResponse

interface StudentCouncilMemberSearchService {
    fun search(name: String?): StudentSearchResponse
}
