package com.example.team.haribo.goms.domain.late.job

import com.example.team.haribo.goms.domain.common.enums.Status
import com.example.team.haribo.goms.domain.late.entity.Late
import com.example.team.haribo.goms.domain.late.repository.LateRepository
import com.example.team.haribo.goms.domain.outing.repository.OutingRepository
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class LateAutoCreateJob(
    private val outingRepository: OutingRepository,
    private val lateRepository: LateRepository
) {

    private val log = LoggerFactory.getLogger(LateAutoCreateJob::class.java)

    @Transactional
    @Scheduled(cron = "0 30 19 * * MON,WED", zone = "Asia/Seoul")
    fun createLatesForOutingMembers() {
        val start = System.currentTimeMillis()
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

        val activeOutings = outingRepository.findAllActiveWithOutingMember()
        val lates = activeOutings
            .filterNot { lateRepository.existsByOutingId(it.id!!) }
            .map {
                it.comingAt = now
                it.member.status = Status.COMING

                Late(
                    member = it.member,
                    outing = it,
                    comingAt = now,
                    lateCount = lateRepository.countByMemberId(it.member.id!!) + 1
                )
            }

        lateRepository.saveAll(lates)

        log.info(
            LogFormat.message(
                domain = "SCHEDULER",
                event = "late auto create completed",
                "targetCount" to activeOutings.size,
                "createdCount" to lates.size,
                "durationMs" to System.currentTimeMillis() - start
            )
        )
    }
}
