package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.QrIssueResponse

interface QrIssueService {
    fun issue(): QrIssueResponse
}
