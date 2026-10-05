package com.teamharibo.goms.domain.member.service.impl

import com.teamharibo.goms.domain.auth.repository.redis.RefreshTokenRedisRepository
import com.teamharibo.goms.domain.auth.service.impl.ReissueServiceImpl
import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Platform
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.common.enums.Status
import com.teamharibo.goms.domain.discord.entity.DiscordAccountLink
import com.teamharibo.goms.domain.discord.repository.DiscordAccountLinkRepository
import com.teamharibo.goms.domain.late.entity.Late
import com.teamharibo.goms.domain.late.repository.LateRepository
import com.teamharibo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.member.exception.MemberWithdrawPasswordMismatchException
import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.notification.entity.DeviceToken
import com.teamharibo.goms.domain.notification.repository.DeviceTokenRepository
import com.teamharibo.goms.domain.outing.entity.Outing
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.domain.place.entity.Place
import com.teamharibo.goms.domain.place.repository.PlaceRecommendRepository
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.report.entity.ReviewReport
import com.teamharibo.goms.domain.report.repository.ReviewReportRepository
import com.teamharibo.goms.domain.review.entity.Review
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.global.exception.ErrorCode
import com.teamharibo.goms.global.exception.GlobalException
import com.teamharibo.goms.global.jwt.JwtProperties
import com.teamharibo.goms.global.jwt.JwtProvider
import com.teamharibo.goms.global.util.MemberUtil
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mariadb.MariaDBContainer
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

class RedisContainer : GenericContainer<RedisContainer>("redis:7-alpine")

class FailingRefreshTokenRedisRepository(
    redisTemplate: StringRedisTemplate
) : RefreshTokenRedisRepository(redisTemplate) {
    var failDelete = false

    override fun deleteByMemberId(memberId: Long) {
        if (failDelete) {
            throw IllegalStateException("test-only Redis cleanup failure")
        }
        super.deleteByMemberId(memberId)
    }
}

@TestConfiguration(proxyBeanMethods = false)
private class MemberWithdrawIntegrationConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(4)

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory =
        LettuceConnectionFactory(
            MariaDbMemberWithdrawIntegrationTest.redis.host,
            MariaDbMemberWithdrawIntegrationTest.redis.getMappedPort(6379)
        )

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(connectionFactory)

    @Bean
    @Primary
    fun refreshTokenRedisRepository(redisTemplate: StringRedisTemplate): FailingRefreshTokenRedisRepository =
        FailingRefreshTokenRedisRepository(redisTemplate)

    @Bean
    fun jwtProvider(): JwtProvider = JwtProvider(
        JwtProperties(
            secret = "member-withdraw-integration-test-secret-key-1234567890",
            accessExpSeconds = 300,
            refreshExpSeconds = 300
        )
    )
}

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = ["spring.profiles.active=", "spring.jpa.hibernate.ddl-auto=create-drop"])
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker = true)
@Import(
    MemberUtil::class,
    MemberWithdrawServiceImpl::class,
    ReissueServiceImpl::class,
    MemberWithdrawIntegrationConfiguration::class
)
class MariaDbMemberWithdrawIntegrationTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val discordAccountLinkRepository: DiscordAccountLinkRepository,
    private val reviewReportRepository: ReviewReportRepository,
    private val lateRepository: LateRepository,
    private val reviewRepository: ReviewRepository,
    private val placeRecommendRepository: PlaceRecommendRepository,
    private val outingRepository: OutingRepository,
    private val placeRepository: PlaceRepository,
    private val withdrawService: MemberWithdrawServiceImpl,
    private val reissueService: ReissueServiceImpl,
    private val refreshTokenRedisRepository: FailingRefreshTokenRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    transactionManager: PlatformTransactionManager,
    private val jdbcTemplate: JdbcTemplate
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @BeforeEach
    fun cleanDatabase() {
        reviewReportRepository.deleteAllInBatch()
        lateRepository.deleteAllInBatch()
        reviewRepository.deleteAllInBatch()
        placeRecommendRepository.deleteAllInBatch()
        outingRepository.deleteAllInBatch()
        deviceTokenRepository.deleteAllInBatch()
        discordAccountLinkRepository.deleteAllInBatch()
        placeRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
        refreshTokenRedisRepository.failDelete = false
    }

    @AfterEach
    fun clearSecurityContext() {
        refreshTokenRedisRepository.failDelete = false
        SecurityContextHolder.clearContext()
    }

    @RepeatedTest(10)
    fun `회원 연관 데이터 유무와 관계없이 탈퇴하면 모든 연관 데이터와 리프레시 토큰이 정리된다`() {
        listOf(
            WithdrawalReferences(deviceToken = true, discordAccountLink = false),
            WithdrawalReferences(deviceToken = false, discordAccountLink = true),
            WithdrawalReferences(deviceToken = true, discordAccountLink = true),
            WithdrawalReferences(deviceToken = false, discordAccountLink = false)
        ).forEach { references ->
            val member = member("withdraw-${sequence.incrementAndGet()}")
            createReferences(member, references)
            val memberId = requireNotNull(member.id)
            val refreshToken = jwtProvider.createRefreshToken(memberId)
            refreshTokenRedisRepository.save(memberId, refreshToken, 300)

            asMember(memberId) {
                withdrawService.withdraw(MemberWithdrawRequest("password"))
            }

            assertFalse(memberRepository.existsById(memberId))
            assertTrue(deviceTokenRepository.findAllByMember_Id(memberId).isEmpty())
            assertTrue(discordAccountLinkRepository.findAllByDiscordUserIdIn(listOf("discord-$memberId")).isEmpty())
            assertFalse(reviewRepository.existsById(requireNotNull(member.reviewId)))
            assertFalse(outingRepository.existsById(requireNotNull(member.outingId)))
            assertNull(refreshTokenRedisRepository.findByMemberId(memberId))
        }
    }

    @Test
    fun `비밀번호가 틀리면 DB와 Redis 상태가 유지된다`() {
        val member = member("wrong-password")
        createReferences(member, WithdrawalReferences(deviceToken = true, discordAccountLink = true))
        val memberId = requireNotNull(member.id)
        val refreshToken = jwtProvider.createRefreshToken(memberId)
        refreshTokenRedisRepository.save(memberId, refreshToken, 300)

        assertThrows(MemberWithdrawPasswordMismatchException::class.java) {
            asMember(memberId) {
                withdrawService.withdraw(MemberWithdrawRequest("wrong"))
            }
        }

        assertTrue(memberRepository.existsById(memberId))
        assertEquals(1, deviceTokenRepository.findAllByMember_Id(memberId).size)
        assertEquals(1, discordAccountLinkRepository.findAllByDiscordUserIdIn(listOf("discord-$memberId")).size)
        assertTrue(reviewRepository.existsById(requireNotNull(member.reviewId)))
        refreshTokenRedisRepository.findByMemberId(memberId) shouldBe refreshToken
    }

    @Test
    fun `DB commit이 실패하면 리프레시 토큰은 유지된다`() {
        val member = member("commit-failure")
        val memberId = requireNotNull(member.id)
        val refreshToken = jwtProvider.createRefreshToken(memberId)
        refreshTokenRedisRepository.save(memberId, refreshToken, 300)

        assertThrows(DataIntegrityViolationException::class.java) {
            transactionTemplate.executeWithoutResult {
                asMember(memberId) {
                    withdrawService.withdraw(MemberWithdrawRequest("password"))
                }
                jdbcTemplate.update(
                    """
                    INSERT INTO device_token (member_id, device_id, fcm_token, platform, created_at, updated_at)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """.trimIndent(),
                    memberId,
                    "commit-failure-device",
                    "commit-failure-token",
                    Platform.ANDROID.name
                )
            }
        }

        assertTrue(memberRepository.existsById(memberId))
        refreshTokenRedisRepository.findByMemberId(memberId) shouldBe refreshToken
    }

    @Test
    fun `Redis cleanup이 실패해도 탈퇴는 커밋되고 stale refresh token은 재발급을 허용하지 않는다`() {
        val member = member("redis-failure")
        createReferences(member, WithdrawalReferences(deviceToken = true, discordAccountLink = true))
        val memberId = requireNotNull(member.id)
        val refreshToken = jwtProvider.createRefreshToken(memberId)
        refreshTokenRedisRepository.save(memberId, refreshToken, 300)
        refreshTokenRedisRepository.failDelete = true

        asMember(memberId) {
            withdrawService.withdraw(MemberWithdrawRequest("password"))
        }

        assertFalse(memberRepository.existsById(memberId))
        assertTrue(deviceTokenRepository.findAllByMember_Id(memberId).isEmpty())
        assertTrue(discordAccountLinkRepository.findAllByDiscordUserIdIn(listOf("discord-$memberId")).isEmpty())
        refreshTokenRedisRepository.findByMemberId(memberId) shouldBe refreshToken

        assertThrows(GlobalException::class.java) {
            reissueService.reissue("Bearer $refreshToken")
        }.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
    }

    private fun member(name: String): TestMember = memberRepository.saveAndFlush(
        Member(
            email = "$name@example.invalid",
            password = requireNotNull(passwordEncoder.encode("password")),
            name = name,
            grade = 1,
            department = Department.SW,
            gender = Gender.MALE,
            role = Role.ROLE_STUDENT,
            status = Status.COMING
        )
    ).let(::TestMember)

    private fun createReferences(member: TestMember, references: WithdrawalReferences) {
        val persistentMember = memberRepository.findById(requireNotNull(member.id)).orElseThrow()
        val place = placeRepository.saveAndFlush(
            Place(
                externalPlaceId = "withdraw-place-${member.id}",
                placeName = "테스트 장소",
                address = "테스트 주소",
                latitude = 35.0,
                longitude = 127.0,
                lastSyncedAt = LocalDateTime.now()
            )
        )
        val outing = outingRepository.saveAndFlush(Outing(member = persistentMember))
        lateRepository.saveAndFlush(Late(member = persistentMember, outing = outing, comingAt = LocalDateTime.now(), lateCount = 1))
        val review = reviewRepository.saveAndFlush(Review(place = place, member = persistentMember, content = "테스트 후기"))
        reviewReportRepository.saveAndFlush(ReviewReport(review = review, memberId = requireNotNull(member.id), content = "테스트 신고"))
        if (references.deviceToken) {
            deviceTokenRepository.saveAndFlush(
                DeviceToken(member = persistentMember, deviceId = "device-${member.id}", fcmToken = "fcm-${member.id}", platform = Platform.ANDROID)
            )
        }
        if (references.discordAccountLink) {
            discordAccountLinkRepository.saveAndFlush(
                DiscordAccountLink(persistentMember, "discord-${member.id}", "discord-user")
            )
        }
        member.reviewId = review.id
        member.outingId = outing.id
    }

    private fun asMember(memberId: Long, action: () -> Unit) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(memberId, null, emptyList())
        try {
            action()
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    private data class WithdrawalReferences(
        val deviceToken: Boolean,
        val discordAccountLink: Boolean
    )

    private data class TestMember(
        val member: Member,
        var reviewId: Long? = null,
        var outingId: Long? = null
    ) {
        val id: Long? get() = member.id
    }

    companion object {
        private val sequence = AtomicLong()

        @Container
        @JvmField
        val mariaDb = MariaDBContainer("mariadb:11.4")
            .withDatabaseName("goms_member_withdraw")
            .withUsername("goms_test")
            .withPassword("goms_test")

        @Container
        @JvmField
        val redis = RedisContainer().withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun configureContainers(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariaDb::getJdbcUrl)
            registry.add("spring.datasource.username", mariaDb::getUsername)
            registry.add("spring.datasource.password", mariaDb::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.mariadb.jdbc.Driver" }
        }
    }
}
