package com.teamharibo.goms.domain.studentcouncil.service

import com.teamharibo.goms.domain.common.enums.Status

interface StudentCouncilOutingAllowedService {
    /**
     * 학생회가 회원의 외출 가능 여부(COMING, CANNOT_OUTING)를 변경한다.
     * OUTING 상태는 QR 외출로만 만들 수 있고, 외출 중인 회원의 상태는 변경할 수 없다.
     * 상태 확인과 변경 사이의 경합을 막기 위해 트랜잭션 커밋 전까지 회원 row를 잠근다.
     *
     * @throws GlobalException 요청 상태가 OUTING이면 INVALID_REQUEST
     * @throws NotFoundMemberException 회원이 없는 경우
     * @throws StatusConflictException 회원이 외출 중이거나 이미 요청한 상태인 경우
     */
    fun update(memberId: Long, status: Status)
}
