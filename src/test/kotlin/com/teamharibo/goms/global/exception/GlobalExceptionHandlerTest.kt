package com.teamharibo.goms.global.exception

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.teamharibo.goms.global.log.RequestLogConstants
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.resource.NoResourceFoundException
import tools.jackson.module.kotlin.jacksonMapperBuilder

class GlobalExceptionHandlerTest : DescribeSpec({

    val fcmTokenPath = "/fake-fcm-registration-token:APA91bTestTokenValue"

    // Spring 컨텍스트 없이 테스트 컨트롤러와 GlobalExceptionHandler만 올린다.
    val mockMvc = MockMvcBuilders
        .standaloneSetup(ClientErrorTestController(fcmTokenPath))
        .setControllerAdvice(GlobalExceptionHandler())
        .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder().build()))
        .build()

    val handlerLogger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java) as Logger
    val logAppender = ListAppender<ILoggingEvent>()

    beforeEach {
        logAppender.list.clear()
        logAppender.start()
        handlerLogger.addAppender(logAppender)
    }

    afterEach {
        handlerLogger.detachAppender(logAppender)
        logAppender.stop()
    }

    describe("Spring MVC 클라이언트 오류 응답") {

        context("NoResourceFoundException이 발생하면") {
            it("404와 NOT_FOUND_RESOURCE ErrorResponse를 반환한다") {
                mockMvc.perform(post("/api/v3/test/no-resource"))
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_RESOURCE.message))
                    .andExpect(request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.NOT_FOUND_RESOURCE.name))
                    .andExpect(request().attribute(RequestLogConstants.EXCEPTION_TYPE, "NoResourceFoundException"))
            }

            it("요청 경로를 남기지 않고 예외 타입만 warn 로그로 남긴다") {
                mockMvc.perform(post("/api/v3/test/no-resource"))

                logAppender.list shouldHaveSize 1
                val event = logAppender.list.first()
                event.level shouldBe Level.WARN
                event.throwableProxy shouldBe null
                event.formattedMessage shouldContain "type=NoResourceFoundException"
                event.formattedMessage shouldNotContain fcmTokenPath
            }
        }

        context("매핑되지 않은 경로를 요청하면") {
            it("404와 NOT_FOUND_RESOURCE ErrorResponse를 반환한다") {
                mockMvc.perform(post("/api/v3/test/unmapped"))
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value(ErrorCode.NOT_FOUND_RESOURCE.message))
                    .andExpect(request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.NOT_FOUND_RESOURCE.name))

                logAppender.list.first().level shouldBe Level.WARN
                logAppender.list.first().formattedMessage shouldNotContain "/api/v3/test/unmapped"
            }
        }

        context("지원하지 않는 HTTP 메서드로 요청하면") {
            it("405와 METHOD_NOT_ALLOWED ErrorResponse를 반환한다") {
                mockMvc.perform(get("/api/v3/test/json"))
                    .andExpect(status().isMethodNotAllowed)
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.message").value(ErrorCode.METHOD_NOT_ALLOWED.message))
                    .andExpect(request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.METHOD_NOT_ALLOWED.name))
                    .andExpect(
                        request().attribute(RequestLogConstants.EXCEPTION_TYPE, "HttpRequestMethodNotSupportedException")
                    )

                logAppender.list.first().level shouldBe Level.WARN
            }
        }

        context("지원하지 않는 Content-Type으로 요청하면") {
            it("415와 UNSUPPORTED_MEDIA_TYPE ErrorResponse를 반환한다") {
                mockMvc.perform(
                    post("/api/v3/test/json")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text")
                )
                    .andExpect(status().isUnsupportedMediaType)
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.message").value(ErrorCode.UNSUPPORTED_MEDIA_TYPE.message))
                    .andExpect(
                        request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.UNSUPPORTED_MEDIA_TYPE.name)
                    )
                    .andExpect(
                        request().attribute(RequestLogConstants.EXCEPTION_TYPE, "HttpMediaTypeNotSupportedException")
                    )

                logAppender.list.first().level shouldBe Level.WARN
            }
        }

        context("multipart 업로드 크기를 초과하면") {
            it("413과 PAYLOAD_TOO_LARGE ErrorResponse를 반환한다") {
                mockMvc.perform(post("/api/v3/test/upload"))
                    .andExpect(status().isContentTooLarge)
                    .andExpect(jsonPath("$.status").value(413))
                    .andExpect(jsonPath("$.message").value(ErrorCode.PAYLOAD_TOO_LARGE.message))
                    .andExpect(request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.PAYLOAD_TOO_LARGE.name))
                    .andExpect(request().attribute(RequestLogConstants.EXCEPTION_TYPE, "MaxUploadSizeExceededException"))

                logAppender.list.first().level shouldBe Level.WARN
            }
        }

        context("처리되지 않은 예외가 발생하면") {
            it("기존처럼 500과 INTERNAL_SERVER_ERROR ErrorResponse를 반환한다") {
                mockMvc.perform(get("/api/v3/test/unexpected"))
                    .andExpect(status().isInternalServerError)
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.message))
                    .andExpect(
                        request().attribute(RequestLogConstants.FAILURE_REASON, ErrorCode.INTERNAL_SERVER_ERROR.name)
                    )

                logAppender.list.first().level shouldBe Level.ERROR
            }
        }
    }
})

@RestController
private class ClientErrorTestController(
    private val fcmTokenPath: String
) {

    @PostMapping("/api/v3/test/no-resource")
    fun noResource(): ResponseEntity<Void> {
        throw NoResourceFoundException(HttpMethod.POST, fcmTokenPath, fcmTokenPath.removePrefix("/"))
    }

    @PostMapping("/api/v3/test/json", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun json(@RequestBody body: Map<String, Any>): ResponseEntity<Void> {
        return ResponseEntity.ok().build()
    }

    @PostMapping("/api/v3/test/upload")
    fun upload(): ResponseEntity<Void> {
        throw MaxUploadSizeExceededException(1024L)
    }

    @GetMapping("/api/v3/test/unexpected")
    fun unexpected(): ResponseEntity<Void> {
        throw IllegalStateException("unexpected")
    }
}
