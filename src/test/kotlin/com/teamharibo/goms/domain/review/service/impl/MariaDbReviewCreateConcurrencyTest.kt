package com.teamharibo.goms.domain.review.service.impl

import com.teamharibo.goms.domain.member.repository.MemberRepository
import com.teamharibo.goms.domain.place.repository.PlaceRepository
import com.teamharibo.goms.domain.review.repository.ReviewRepository
import com.teamharibo.goms.global.util.MemberUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mariadb.MariaDBContainer

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = ["spring.profiles.active=", "spring.jpa.hibernate.ddl-auto=create-drop"])
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker = true)
@Import(MemberUtil::class, ReviewCreateServiceImpl::class)
class MariaDbReviewCreateConcurrencyTest @Autowired constructor(
    memberRepository: MemberRepository,
    placeRepository: PlaceRepository,
    reviewRepository: ReviewRepository,
    reviewCreateService: ReviewCreateServiceImpl
) : ReviewCreateConcurrencyTest(
    memberRepository,
    placeRepository,
    reviewRepository,
    reviewCreateService
) {

    companion object {
        @Container
        @JvmField
        val mariaDb = MariaDBContainer("mariadb:11.4")
            .withDatabaseName("goms_test")
            .withUsername("goms_test")
            .withPassword("goms_test")

        @DynamicPropertySource
        @JvmStatic
        fun configureMariaDb(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariaDb::getJdbcUrl)
            registry.add("spring.datasource.username", mariaDb::getUsername)
            registry.add("spring.datasource.password", mariaDb::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.mariadb.jdbc.Driver" }
        }
    }
}
