package com.teamharibo.goms.domain.auth.service

import com.teamharibo.goms.domain.auth.dto.response.TokenResponse

interface ReissueService {
    /**
     * `Bearer ` prefix를 제거한 refresh token으로 access token과 refresh token을 함께 재발급한다.
     * Redis에 저장된 회원의 refresh token과 일치해야 하며, 성공하면 새 refresh token으로 교체해 기존 token은 다시 사용할 수 없다.
     *
     * @throws GlobalException REFRESH 타입이 아니거나 저장된 token이 없거나 다르면 INVALID_REFRESH_TOKEN, 회원이 없으면 NOT_FOUND_MEMBER
     */
    fun reissue(refreshTokenHeader: String): TokenResponse
}
