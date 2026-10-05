package com.teamharibo.goms.domain.outing.service

import com.teamharibo.goms.domain.outing.dto.request.QrToggleRequest
import com.teamharibo.goms.domain.outing.dto.response.QrComingResponse

interface QrComingService {
    /**
     * 현재 회원의 진행 중인 외출을 종료하고 COMING 상태로 바꾼다.
     * 지각 처리는 하지 않으며, 지각은 LateAutoCreateJob이 별도로 생성한다.
     * 동시 QR 요청과 지각 scheduler와의 경합을 막기 위해 트랜잭션 커밋 전까지 회원 row를 잠근다.
     *
     * @throws QrExpiredException QR의 exp가 지난 경우
     * @throws NotOutingException 진행 중인 외출이 없는 경우
     */
    fun coming(request: QrToggleRequest): QrComingResponse
}
