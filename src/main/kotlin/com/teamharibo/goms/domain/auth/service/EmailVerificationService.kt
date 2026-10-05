package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.request.EmailVerificationConfirmRequest
import com.teamharibo.goms.domain.auth.dto.request.EmailVerificationSendRequest
import com.teamharibo.goms.domain.auth.dto.response.EmailVerificationConfirmResponse

interface EmailVerificationService {

    /**
     * 요청 목적에 맞는 이메일인지 확인한 뒤 인증 코드를 메일로 발송한다.
     * 코드는 이메일·목적별로 Redis에 5분간 저장되고, 같은 이메일·목적은 60초 동안 다시 요청할 수 없다.
     *
     * @throws EmailAlreadyExistsException SIGNUP 목적인데 이미 가입된 이메일인 경우
     * @throws NotFoundUserException PASSWORD_CHANGE 목적인데 가입되지 않은 이메일인 경우
     * @throws TooManyRequestsException 재요청 제한 시간 안에 다시 요청한 경우
     */
    fun send(request: EmailVerificationSendRequest)

    /**
     * 인증 코드를 확인하고, 성공하면 가입·비밀번호 변경에 사용할 verified token을 10분 TTL로 발급한다.
     * 성공 시 원본 코드와 실패 횟수를 삭제하므로 같은 코드로 다시 확인할 수 없다.
     * 코드가 5회 틀리면 원본 코드를 삭제하므로 새 코드를 다시 발송받아야 한다.
     *
     * @throws VerificationCodeExpiredException 저장된 코드가 없거나 만료된 경우
     * @throws VerificationCodeMismatchException 코드가 일치하지 않는 경우
     * @throws TooManyRequestsException 실패 횟수가 5회에 도달한 경우
     */
    fun confirm(request: EmailVerificationConfirmRequest): EmailVerificationConfirmResponse
}
