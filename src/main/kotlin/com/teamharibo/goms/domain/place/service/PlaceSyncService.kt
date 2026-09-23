package com.teamharibo.goms.domain.place.service

import com.teamharibo.goms.domain.place.dto.response.PlaceSyncResult

interface PlaceSyncService {
    fun sync(): PlaceSyncResult
}