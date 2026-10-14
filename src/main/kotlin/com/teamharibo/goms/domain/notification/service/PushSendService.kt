package com.teamharibo.goms.domain.notification.service

interface PushSendService {
    /**
     * FCM multicast로 푸시를 발송한다. 대상 token이 없으면 발송하지 않는다.
     * Firebase 발송 오류는 예외로 전달하지 않고 로그만 남기므로 호출자는 발송 성공 여부를 알 수 없다.
     * 개별 발송에 실패한 token은 오류 종류와 관계없이 DeviceToken에서 삭제한다.
     */
    fun send(tokens: List<String>, title: String, body: String)
}