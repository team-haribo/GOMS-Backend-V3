package com.teamharibo.goms.domain.late.job

import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.late.entity.Late
import com.teamharibo.goms.domain.late.repository.LateRepository
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class LateAutoCreateJob(
    private val outingRepository: OutingRepository,
    private val lateRepository: LateRepository,
    private val memberRepository: MemberRepository
) {

    private val log = LoggerFactory.getLogger(LateAutoCreateJob::class.java)

    @Transactional
    @Scheduled(cron = "0 30 19 * * MON,WED", zone = "Asia/Seoul")
    fun createLatesForOutingMembers() {
        val start = System.currentTimeMillis()
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

        // 먼저 ID만 조회해, 락 획득 전에 오래된 Entity가 영속성 컨텍스트에 들어가지 않도록 합니다.
        val candidateMemberIds = outingRepository.findAllActiveMemberIds().distinct().sorted()
        val lockedMemberIds = if (candidateMemberIds.isEmpty()) {
            emptyList()
        } else {
            // 대화형 상태 변경과 같은 잠금 순서를 사용합니다. Member를 ID 오름차순으로 먼저 잠근 뒤 현재 Outing을 잠급니다.
            memberRepository.findAllByIdForUpdate(candidateMemberIds).mapNotNull { it.id }.sorted()
        }
        val activeOutings = if (lockedMemberIds.isEmpty()) {
            emptyList()
        } else {
            outingRepository.findAllActiveByMemberIdInForUpdate(lockedMemberIds)
        }
        val outingIds = activeOutings.map { it.id!! }
        val existingLateOutingIds = if (outingIds.isEmpty()) {
            emptySet()
        } else {
            lateRepository.findAllOutingIdsIn(outingIds).toSet()
        }
        val targetOutings = activeOutings
            .filterNot { existingLateOutingIds.contains(it.id!!) }
        val memberIds = targetOutings.map { it.member.id!! }.distinct()
        val lateCountByMemberId = if (memberIds.isEmpty()) {
            emptyMap()
        } else {
            lateRepository.countByMemberIds(memberIds)
                .associate { it.memberId to it.lateCount }
        }

        val lates = targetOutings.map {
            it.comingAt = now
            it.member.status = Status.CANNOT_OUTING

            Late(
                member = it.member,
                outing = it,
                comingAt = now,
                lateCount = (lateCountByMemberId[it.member.id!!] ?: 0L) + 1
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
