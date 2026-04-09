package com.example.team.haribo.goms.domain.place.service

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSyncResult

interface PlaceSyncService {
    fun sync(): PlaceSyncResult
}