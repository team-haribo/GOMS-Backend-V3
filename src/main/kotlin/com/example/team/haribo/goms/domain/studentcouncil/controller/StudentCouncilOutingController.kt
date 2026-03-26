package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceInService
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilForceOutService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilOutingController(
    private val forceOutService: StudentCouncilForceOutService,
    private val forceInService: StudentCouncilForceInService
) {

    @PostMapping("/status/out/{memberId}")
    fun forceOut(@PathVariable memberId: Long): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(forceOutService.out(memberId))
    }

    @PostMapping("/status/in/{memberId}")
    fun forceIn(@PathVariable memberId: Long): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(forceInService.`in`(memberId))
    }
}