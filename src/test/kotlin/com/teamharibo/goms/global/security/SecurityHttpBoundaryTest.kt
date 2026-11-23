package com.teamharibo.goms.global.security

import com.teamharibo.goms.domain.auth.controller.AuthController
import com.teamharibo.goms.domain.auth.dto.response.TokenResponse
import com.teamharibo.goms.domain.auth.service.EmailVerificationService
import com.teamharibo.goms.domain.auth.service.PasswordChangeService
import com.teamharibo.goms.domain.auth.service.ReissueService
import com.teamharibo.goms.domain.auth.service.SigninService
import com.teamharibo.goms.domain.auth.service.SignoutService
import com.teamharibo.goms.domain.auth.service.SignupService
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.outing.controller.OutingController
import com.teamharibo.goms.domain.outing.dto.response.MyOutingStatusResponse
import com.teamharibo.goms.domain.outing.service.MyOutingStatusService
import com.teamharibo.goms.domain.outing.service.OutingCountService
import com.teamharibo.goms.domain.outing.service.OutingStudentListService
import com.teamharibo.goms.domain.outing.service.OutingStudentSearchService
import com.teamharibo.goms.domain.outing.service.QrComingService
import com.teamharibo.goms.domain.outing.service.QrOutingService
import com.teamharibo.goms.domain.studentcouncil.controller.StudentCouncilLateController
import com.teamharibo.goms.domain.studentcouncil.dto.response.LateStudentsListResponse
import com.teamharibo.goms.domain.studentcouncil.service.StudentCouncilLateListService
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.exception.GlobalExceptionHandler
import com.teamharibo.goms.global.jwt.JwtProperties
import com.teamharibo.goms.global.jwt.JwtProvider
import com.teamharibo.goms.global.log.RequestLoggingFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.time.LocalDateTime

/**
 * 실제 SecurityFilterChain(SecurityConfig + JwtAuthenticationFilter + EntryPoint/AccessDeniedHandler)과
 * 실제 JwtProvider 서명·만료 검증, GlobalExceptionHandler를 거치는 HTTP 경계 계약 테스트.
 * Controller 하위 서비스만 MockK로 대체하며 DB·Redis·Firebase 없이 실행된다.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [SecurityHttpBoundaryTestApplication::class],
    properties = [
        "spring.profiles.active=",
        "jwt.secret=$TEST_JWT_SECRET",
        "jwt.access-exp-seconds=300",
        "jwt.refresh-exp-seconds=3600",
        "management.health.redis.enabled=false",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    ]
)
@AutoConfigureMockMvc
class SecurityHttpBoundaryTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jwtProvider: JwtProvider,
    private val myOutingStatusService: MyOutingStatusService,
    private val lateListService: StudentCouncilLateListService,
    private val reissueService: ReissueService
) {

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { myOutingStatusService.getStatus() } returns MyOutingStatusResponse(
            memberId = 1L,
            status = Status.COMING,
            name = "홍길동",
            grade = 2,
            department = "SW_DEVELOPMENT",
            lateCount = 0L,
            profileImageUrl = null
        )
        every { lateListService.list(any()) } returns LateStudentsListResponse(students = emptyList())
    }

    // ---------- 인증 Header 누락/형식 ----------

    @Test
    fun `Authorization Header 없이 보호된 엔드포인트를 호출하면 401 UNAUTHORIZED를 반환한다`() {
        mockMvc.get(OUTING_STATUS)
            .andExpectError(ErrorCode.UNAUTHORIZED)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    @Test
    fun `Bearer 접두사가 없는 Authorization Header는 무시되어 401 UNAUTHORIZED를 반환한다`() {
        val accessToken = jwtProvider.createAccessToken(1L, "ROLE_STUDENT")

        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, accessToken)
        }.andExpectError(ErrorCode.UNAUTHORIZED)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    // ---------- JWT 검증 실패 ----------

    @Test
    fun `만료된 ACCESS 토큰으로 호출하면 401 EXPIRED_TOKEN을 반환한다`() {
        val expiredProvider = JwtProvider(
            JwtProperties(secret = TEST_JWT_SECRET, accessExpSeconds = -60, refreshExpSeconds = 3600)
        )
        val expiredToken = expiredProvider.createAccessToken(1L, "ROLE_STUDENT")

        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, "$BEARER$expiredToken")
        }.andExpectError(ErrorCode.EXPIRED_TOKEN)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    @Test
    fun `REFRESH 토큰을 Authorization Header에 넣으면 401 INVALID_TOKEN을 반환한다`() {
        val refreshToken = jwtProvider.createRefreshToken(1L)

        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, "$BEARER$refreshToken")
        }.andExpectError(ErrorCode.INVALID_TOKEN)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    @Test
    fun `다른 키로 서명된 토큰으로 호출하면 401 INVALID_TOKEN을 반환한다`() {
        val foreignProvider = JwtProvider(
            JwtProperties(
                secret = "another-security-test-secret-at-least-32-bytes",
                accessExpSeconds = 300,
                refreshExpSeconds = 3600
            )
        )
        val forgedToken = foreignProvider.createAccessToken(1L, "ROLE_STUDENT_COUNCIL")

        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, "$BEARER$forgedToken")
        }.andExpectError(ErrorCode.INVALID_TOKEN)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    @Test
    fun `형식이 깨진 토큰으로 호출하면 401 INVALID_TOKEN을 반환한다`() {
        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, "${BEARER}not-a-jwt")
        }.andExpectError(ErrorCode.INVALID_TOKEN)

        verify(exactly = 0) { myOutingStatusService.getStatus() }
    }

    // ---------- 인가 ----------

    @Test
    fun `STUDENT 권한으로 학생회 전용 엔드포인트를 호출하면 403 FORBIDDEN을 반환한다`() {
        val accessToken = jwtProvider.createAccessToken(1L, "ROLE_STUDENT")

        mockMvc.get(STUDENT_COUNCIL_LATE) {
            header(AUTHORIZATION, "$BEARER$accessToken")
        }.andExpectError(ErrorCode.FORBIDDEN)

        verify(exactly = 0) { lateListService.list(any()) }
    }

    @Test
    fun `STUDENT_COUNCIL 권한으로 학생회 전용 엔드포인트를 호출하면 Controller에 도달한다`() {
        val accessToken = jwtProvider.createAccessToken(2L, "ROLE_STUDENT_COUNCIL")

        mockMvc.get(STUDENT_COUNCIL_LATE) {
            header(AUTHORIZATION, "$BEARER$accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.students") { isArray() }
        }

        verify(exactly = 1) { lateListService.list(null) }
    }

    @Test
    fun `유효한 ACCESS 토큰으로 보호된 엔드포인트를 호출하면 Controller 응답을 반환한다`() {
        val accessToken = jwtProvider.createAccessToken(1L, "ROLE_STUDENT")

        mockMvc.get(OUTING_STATUS) {
            header(AUTHORIZATION, "$BEARER$accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.memberId") { value(1) }
            jsonPath("$.status") { value("COMING") }
        }

        verify(exactly = 1) { myOutingStatusService.getStatus() }
    }

    // ---------- 재발급 (permitAll) ----------

    @Test
    fun `RefreshToken Header로 재발급을 요청하면 Security를 통과해 서비스 결과를 반환한다`() {
        val refreshToken = jwtProvider.createRefreshToken(1L)
        val header = "$BEARER$refreshToken"
        val now = LocalDateTime.of(2026, 1, 1, 0, 0)
        every { reissueService.reissue(header) } returns TokenResponse(
            accessToken = "new-access",
            refreshToken = "new-refresh",
            accessTokenExpiresIn = now.plusMinutes(5),
            refreshTokenExpiresIn = now.plusDays(1)
        )

        mockMvc.patch(REISSUE) {
            header(REFRESH_TOKEN, header)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("new-access") }
            jsonPath("$.refreshToken") { value("new-refresh") }
        }

        verify(exactly = 1) { reissueService.reissue(header) }
    }

    @Test
    fun `재발급 서비스가 INVALID_REFRESH_TOKEN을 던지면 401 오류 본문을 반환한다`() {
        val header = "${BEARER}revoked-refresh-token"
        every { reissueService.reissue(header) } throws GlobalException(ErrorCode.INVALID_REFRESH_TOKEN)

        mockMvc.patch(REISSUE) {
            header(REFRESH_TOKEN, header)
        }.andExpectError(ErrorCode.INVALID_REFRESH_TOKEN)
    }

    private fun ResultActionsDsl.andExpectError(errorCode: ErrorCode) {
        andExpect {
            status { isEqualTo(errorCode.status) }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(errorCode.status) }
            jsonPath("$.message") { value(errorCode.message) }
        }
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val REFRESH_TOKEN = "RefreshToken"
        private const val BEARER = "Bearer "
        private const val OUTING_STATUS = "/api/v3/outing/status"
        private const val STUDENT_COUNCIL_LATE = "/api/v3/student-council/late"
        private const val REISSUE = "/api/v3/auth/reissue"
    }
}

private const val TEST_JWT_SECRET = "security-boundary-test-secret-at-least-32-bytes"

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(
    SecurityConfig::class,
    RequestLoggingFilter::class,
    GlobalExceptionHandler::class,
    AuthController::class,
    OutingController::class,
    StudentCouncilLateController::class
)
private class SecurityHttpBoundaryTestApplication {

    // AuthController
    @Bean fun signupService(): SignupService = mockk()
    @Bean fun signinService(): SigninService = mockk()
    @Bean fun reissueService(): ReissueService = mockk()
    @Bean fun passwordChangeService(): PasswordChangeService = mockk()
    @Bean fun signoutService(): SignoutService = mockk()
    @Bean fun emailVerificationService(): EmailVerificationService = mockk()

    // OutingController
    @Bean fun myOutingStatusService(): MyOutingStatusService = mockk()
    @Bean fun qrOutingService(): QrOutingService = mockk()
    @Bean fun qrComingService(): QrComingService = mockk()
    @Bean fun outingStudentListService(): OutingStudentListService = mockk()
    @Bean fun outingCountService(): OutingCountService = mockk()
    @Bean fun outingStudentSearchService(): OutingStudentSearchService = mockk()

    // StudentCouncilLateController
    @Bean fun studentCouncilLateListService(): StudentCouncilLateListService = mockk()
}
