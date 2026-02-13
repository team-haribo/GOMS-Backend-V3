package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class QrIssueServiceImpl : QrIssueService {

    override fun issue(): QrIssueResponse {
        val uuid = UUID.randomUUID().toString()

        val exp = Instant.now()
            .plusSeconds(300) //5분
            .toEpochMilli()

        return QrIssueResponse(
            uuid = uuid,
            exp = exp
        )
    }
}
