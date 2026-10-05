package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse

interface DiscordStudentCouncilApplyService {

    /**
     * 전달받은 Discord user ID 목록으로 임시 학생회 권한을 전체 교체한다.
     * 고정 학생회가 아닌 모든 ROLE_STUDENT_COUNCIL을 ROLE_STUDENT로 초기화한 뒤 목록의 연동 회원에게만 다시 부여한다.
     * ID는 공백 제거와 중복 제거 후 처리하며, 연동 계정이 없는 ID는 예외 없이 failedUsers로 응답한다.
     *
     * @throws InvalidInternalSecretException 내부 secret이 일치하지 않는 경우
     */
    fun apply(
        internalSecret: String?,
        discordUserIds: List<String>
    ): DiscordStudentCouncilApplyResponse
}