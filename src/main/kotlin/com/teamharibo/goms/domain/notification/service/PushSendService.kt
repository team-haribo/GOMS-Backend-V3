package com.teamharibo.goms.domain.notification.service

interface PushSendService {
    fun send(tokens: List<String>, title: String, body: String)
}