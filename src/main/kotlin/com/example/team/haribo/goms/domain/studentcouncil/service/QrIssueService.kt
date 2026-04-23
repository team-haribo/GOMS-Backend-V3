package com.example.team.haribo.goms.domain.studentcouncil.service

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse

interface QrIssueService {
    fun issue(): QrIssueResponse
}
