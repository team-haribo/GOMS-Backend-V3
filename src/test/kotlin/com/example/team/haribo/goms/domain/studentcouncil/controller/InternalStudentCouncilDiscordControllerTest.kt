package com.example.team.haribo.goms.domain.studentcouncil.controller

import com.example.team.haribo.goms.domain.studentcouncil.dto.request.DiscordStudentCouncilApplyRequest
import com.example.team.haribo.goms.domain.studentcouncil.dto.response.DiscordStudentCouncilApplyResponse
import com.example.team.haribo.goms.domain.studentcouncil.exception.InvalidInternalSecretException
import com.example.team.haribo.goms.domain.studentcouncil.service.DiscordStudentCouncilApplyService
import com.example.team.haribo.goms.global.exception.GlobalExceptionHandler
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

class InternalStudentCouncilDiscordControllerTest : DescribeSpec({

    val service = mockk<DiscordStudentCouncilApplyService>()
    // Boot 4 = Jackson 3(tools.jackson). Kotlin data class 역직렬화를 위해 Kotlin 모듈 포함 매퍼 사용
    val objectMapper: JsonMapper = jacksonMapperBuilder().build()

    // Spring 컨텍스트/Security 없이 컨트롤러만 올린다.
    // - Kotlin data class 역직렬화를 위해 JacksonJsonHttpMessageConverter(JsonMapper) 주입
    // - 400/403 에러 본문(ErrorResponse) 검증을 위해 GlobalExceptionHandler 를 advice 로 등록
    val mockMvc = MockMvcBuilders
        .standaloneSetup(InternalStudentCouncilDiscordController(service))
        .setControllerAdvice(GlobalExceptionHandler())
        .setMessageConverters(JacksonJsonHttpMessageConverter(objectMapper))
        .build()

    val url = "/api/v3/internal/student-council/discord/apply"

    describe("POST $url") {

        context("정상 요청") {
            it("200 OK 와 함께 응답 필드가 그대로 반환되고, 헤더/본문이 Service로 전달된다") {
                val response = DiscordStudentCouncilApplyResponse(
                    success = true,
                    syncedCount = 1,
                    syncedUsers = listOf("홍길동"),
                    failedCount = 0,
                    failedUsers = emptyList(),
                    appliedAt = "2026년 9월 14일 15시 0분",
                    durationMs = 12L
                )
                every { service.apply("secret", listOf("d1")) } returns response

                mockMvc.perform(
                    post(url)
                        .header("X-Internal-Secret", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DiscordStudentCouncilApplyRequest(listOf("d1"))))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.syncedCount").value(1))
                    .andExpect(jsonPath("$.syncedUsers[0]").value("홍길동"))
                    .andExpect(jsonPath("$.failedCount").value(0))
                    .andExpect(jsonPath("$.failedUsers").isEmpty)
                    .andExpect(jsonPath("$.appliedAt").value("2026년 9월 14일 15시 0분"))
                    .andExpect(jsonPath("$.durationMs").value(12))

                // Controller → Service 전달 계약 검증
                verify(exactly = 1) { service.apply("secret", listOf("d1")) }
            }
        }

        context("부분 실패") {
            it("일부 사용자 연결 실패여도 200 OK 와 success=false, 실패 목록/카운트를 반환한다") {
                val response = DiscordStudentCouncilApplyResponse(
                    success = false,
                    syncedCount = 1,
                    syncedUsers = listOf("홍길동"),
                    failedCount = 1,
                    failedUsers = listOf("missing"),
                    appliedAt = "2026년 9월 14일 15시 0분",
                    durationMs = 8L
                )
                every { service.apply("secret", listOf("d1", "missing")) } returns response

                mockMvc.perform(
                    post(url)
                        .header("X-Internal-Secret", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DiscordStudentCouncilApplyRequest(listOf("d1", "missing"))))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.failedCount").value(1))
                    .andExpect(jsonPath("$.failedUsers[0]").value("missing"))
            }
        }

        context("Validation 실패 - 빈 discordUserIds") {
            it("@NotEmpty 위반으로 400 과 INVALID_REQUEST 오류 본문을 반환한다") {
                mockMvc.perform(
                    post(url)
                        .header("X-Internal-Secret", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DiscordStudentCouncilApplyRequest(emptyList())))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("요청 정보가 유효하지 않습니다."))
            }
        }

        context("잘못된 JSON 본문") {
            it("파싱 불가한 본문은 400 과 INVALID_REQUEST 오류 본문을 반환한다") {
                mockMvc.perform(
                    post(url)
                        .header("X-Internal-Secret", "secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json }")
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("요청 정보가 유효하지 않습니다."))
            }
        }

        context("잘못된 Secret") {
            it("Service가 InvalidInternalSecretException을 던지면 403 과 전역 ErrorResponse를 반환한다") {
                every { service.apply("wrong-secret", listOf("d1")) } throws InvalidInternalSecretException()

                mockMvc.perform(
                    post(url)
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DiscordStudentCouncilApplyRequest(listOf("d1"))))
                )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
            }
        }
    }
})
