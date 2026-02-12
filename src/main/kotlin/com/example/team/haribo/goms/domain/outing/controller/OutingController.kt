package com.example.team.haribo.goms.domain.outing.controller

import com.example.team.haribo.goms.domain.outing.dto.request.QrToggleRequest
import com.example.team.haribo.goms.domain.outing.dto.response.MyOutingStatusResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingCountResponse
import com.example.team.haribo.goms.domain.outing.dto.response.OutingStudentListResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrComingResponse
import com.example.team.haribo.goms.domain.outing.dto.response.QrOutingResponse
import com.example.team.haribo.goms.domain.outing.service.MyOutingStatusService
import com.example.team.haribo.goms.domain.outing.service.OutingCountService
import com.example.team.haribo.goms.domain.outing.service.OutingStudentListService
import com.example.team.haribo.goms.domain.outing.service.OutingStudentSearchService
import com.example.team.haribo.goms.domain.outing.service.QrComingService
import com.example.team.haribo.goms.domain.outing.service.QrOutingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v3/outing")
class OutingController(
    private val myOutingStatusService: MyOutingStatusService,
    private val qrOutingService: QrOutingService,
    private val qrComingService: QrComingService,
    private val outingStudentListService: OutingStudentListService,
    private val outingCountService: OutingCountService,
    private val outingStudentSearchService: OutingStudentSearchService
) {

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<MyOutingStatusResponse> {
        return ResponseEntity.ok(myOutingStatusService.getStatus())
    }

    @PostMapping("/in")
    fun outing(@RequestBody request: QrToggleRequest): ResponseEntity<QrOutingResponse> {
        return ResponseEntity.ok(qrOutingService.outing(request))
    }

    @PostMapping("/out")
    fun coming(@RequestBody request: QrToggleRequest): ResponseEntity<QrComingResponse> {
        return ResponseEntity.ok(qrComingService.coming(request))
    }

    @GetMapping("/list")
    fun getOutingStudents(): ResponseEntity<OutingStudentListResponse> {
        return ResponseEntity.ok(outingStudentListService.getList())
    }

    @GetMapping("/count")
    fun getOutingCount(): ResponseEntity<OutingCountResponse> {
        return ResponseEntity.ok(outingCountService.getCount())
    }

    @GetMapping("/search")
    fun searchOutingStudents(@RequestParam("name") name: String?): ResponseEntity<OutingStudentListResponse> {
        return ResponseEntity.ok(outingStudentSearchService.search(name))
    }
}
