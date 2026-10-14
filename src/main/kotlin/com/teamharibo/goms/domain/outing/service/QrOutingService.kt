package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.dto.response.QrOutingResponse

interface QrOutingService {
    /**
     * 현재 회원을 OUTING 상태로 바꾸고 새 외출 기록을 만든다.
     * 동시 QR 요청으로 외출 기록이 중복 생성되지 않도록 트랜잭션 커밋 전까지 회원 row를 잠근다.
     *
     * @throws QrExpiredException QR의 exp가 지난 경우
     * @throws CannotOutingException CANNOT_OUTING 상태인 경우
     * @throws AlreadyOutingException 이미 OUTING 상태인 경우
     */
    fun outing(request: QrToggleRequest): QrOutingResponse
}
