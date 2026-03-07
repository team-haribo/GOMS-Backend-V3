package com.example.team.haribo.goms.domain.health.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/health")
class HealthController {

    @GetMapping
    fun health(): ResponseEntity<Void> {
        return ResponseEntity.ok().build()
    }
}