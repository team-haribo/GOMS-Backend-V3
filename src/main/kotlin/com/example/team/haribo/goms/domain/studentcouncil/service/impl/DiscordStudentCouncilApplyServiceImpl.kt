package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.discord.repository.DiscordAccountLinkRepository
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse
import com.example.team.haribo.goms.domain.studentcouncil.exception.InvalidInternalSecretException
import com.example.team.haribo.goms.domain.studentcouncil.service.DiscordStudentCouncilApplyService
import com.example.team.haribo.goms.global.log.LogFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class DiscordStudentCouncilApplyServiceImpl(
    private val memberRepository: MemberRepository,
    private val discordAccountLinkRepository: DiscordAccountLinkRepository,
    @Value("\${internal.discord.secret}")
    private val expectedInternalSecret: String
) : DiscordStudentCouncilApplyService {

    private val log = LoggerFactory.getLogger(DiscordStudentCouncilApplyServiceImpl::class.java)
    private val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H시 m분")

    @Transactional
    override fun apply(
        internalSecret: String?,
        discordUserIds: List<String>
    ): DiscordStudentCouncilApplyResponse {
        val startTime = System.currentTimeMillis()

        if (internalSecret != expectedInternalSecret) {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "디스코드 외출제 관리 인원 동기화 실패",
                    "reason" to "내부 secret 불일치"
                )
            )
            throw InvalidInternalSecretException()
        }

        val distinctDiscordUserIds = discordUserIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        log.info(
            LogFormat.message(
                domain = "STUDENT_COUNCIL",
                event = "디스코드 외출제 관리 인원 동기화 시작",
                "requestCount" to discordUserIds.size,
                "distinctCount" to distinctDiscordUserIds.size
            )
        )

        memberRepository.resetTemporaryStudentCouncilRole()

        val links = discordAccountLinkRepository.findAllByDiscordUserIdIn(distinctDiscordUserIds)
        val linkMap = links.associateBy { it.discordUserId }

        val syncedUsers = mutableListOf<String>()
        val failedUsers = mutableListOf<String>()

        distinctDiscordUserIds.forEach { discordUserId ->
            val link = linkMap[discordUserId]

            if (link == null) {
                failedUsers.add(discordUserId)
                return@forEach
            }

            val member = link.member

            if (member.isFixedStudentCouncil) {
                syncedUsers.add(member.name)
                return@forEach
            }

            member.role = Role.ROLE_STUDENT_COUNCIL
            syncedUsers.add(member.name)
        }

        val durationMs = System.currentTimeMillis() - startTime
        val appliedAt = LocalDateTime
            .now(ZoneId.of("Asia/Seoul"))
            .format(formatter)
        val success = failedUsers.isEmpty()

        if (success) {
            log.info(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "디스코드 외출제 관리 인원 동기화 완료",
                    "success" to true,
                    "syncedCount" to syncedUsers.size,
                    "appliedAt" to appliedAt,
                    "durationMs" to durationMs
                )
            )
        } else {
            log.warn(
                LogFormat.message(
                    domain = "STUDENT_COUNCIL",
                    event = "디스코드 외출제 관리 인원 동기화 일부 실패",
                    "success" to false,
                    "syncedCount" to syncedUsers.size,
                    "failedCount" to failedUsers.size,
                    "failedUsers" to failedUsers,
                    "appliedAt" to appliedAt,
                    "durationMs" to durationMs
                )
            )
        }

        return DiscordStudentCouncilApplyResponse(
            success = success,
            syncedCount = syncedUsers.size,
            syncedUsers = syncedUsers,
            failedCount = failedUsers.size,
            failedUsers = failedUsers,
            appliedAt = appliedAt,
            durationMs = durationMs
        )
    }
}