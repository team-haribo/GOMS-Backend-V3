package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import com.example.team.haribo.goms.domain.studentcouncil.service.StudentCouncilLateListService
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/student-council")
class StudentCouncilLateController(
    private val lateListService: StudentCouncilLateListService
) {

    @GetMapping("/late")
    fun listLate(
        @RequestParam("date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<LateStudentsListResponse> {
        return ResponseEntity.ok(lateListService.list(date))
    }
}