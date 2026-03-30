package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.QrIssueResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.QrIssueService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class QrIssueServiceImpl : QrIssueService {

    private val log = LoggerFactory.getLogger(QrIssueServiceImpl::class.java)

    override fun issue(): QrIssueResponse {
        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "QR 발급 시도"
            )
        )

        val uuid = UUID.randomUUID().toString()

        val exp = Instant.now()
            .plusSeconds(300)
            .toEpochMilli()

        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "QR 발급 완료",
                "exp" to exp
            )
        )

        return QrIssueResponse(
            uuid = uuid,
            exp = exp
        )
    }
}