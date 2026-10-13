package com.teamharibo.goms.domain.place.service.impl

import com.teamharibo.goms.domain.common.enums.Department
import com.teamharibo.goms.domain.common.enums.Gender
import com.teamharibo.goms.domain.common.enums.Role
import com.teamharibo.goms.domain.member.entity.Member
import com.teamharibo.goms.domain.place.entity.Place
import com.teamharibo.goms.domain.place.entity.PlaceRecommend
import com.teamharibo.goms.domain.review.entity.Review
import com.teamharibo.goms.global.util.MemberUtil
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.hibernate.SessionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime

/**
 * PlaceRecommendServiceImpl.getRecommendedPlaces()가 실행하는 SELECT 수를 실제 JPA로 측정한다.
 *
 * MockK 단위 테스트는 LAZY 관계(PlaceRecommend.place)의 추가 SELECT를 검출하지 못하므로,
 * 데이터를 저장한 뒤 영속성 컨텍스트를 비우고 Hibernate Statistics로 SQL 수를 센다.
 * 활성 Place를 목록 조회 한 번에 가져오므로 SELECT 수는 장소 수와 무관하게
 * 목록 1 + 집계 2로 고정되어야 한다(#82).
 */
@DataJpaTest
@TestPropertySource(
    properties = [
        "spring.profiles.active=",
        "spring.jpa.properties.hibernate.generate_statistics=true"
    ]
)
@Import(PlaceRecommendServiceImpl::class, MemberUtil::class)
class PlaceRecommendQueryCountTest @Autowired constructor(
    private val service: PlaceRecommendServiceImpl,
    private val entityManager: EntityManager
) {

    private var sequence = 0

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `추천한 활성 장소가 1개이면 SELECT는 3번 실행된다`() {
        assertRecommendedPlacesQueryCount(placeCount = 1)
    }

    @Test
    fun `추천한 활성 장소가 10개여도 SELECT는 3번 실행된다`() {
        assertRecommendedPlacesQueryCount(placeCount = 10)
    }

    @Test
    fun `추천한 장소가 없으면 추천 목록 SELECT 1번만 실행된다`() {
        val member = saveMember()
        clearPersistenceContext()

        val (selectCount, responseSize) = measure(member.id!!)

        responseSize shouldBe 0
        selectCount shouldBe 1L
    }

    @Test
    fun `추천한 장소가 모두 비활성이면 목록 SELECT 1번만 실행된다`() {
        val member = saveMember()
        repeat(3) { saveRecommend(member, savePlace(isActive = false)) }
        clearPersistenceContext()

        val (selectCount, responseSize) = measure(member.id!!)

        responseSize shouldBe 0
        selectCount shouldBe 1L
    }

    @Test
    fun `소유자·추천 상태·활성 조건을 보존하고 추천 수와 리뷰 수를 그대로 반환한다`() {
        val me = saveMember()
        val other = saveMember()

        val recommendedByBoth = savePlace(isActive = true)
        saveRecommend(me, recommendedByBoth)
        saveRecommend(other, recommendedByBoth)
        saveReview(me, recommendedByBoth)
        saveReview(other, recommendedByBoth, deleted = true)

        val withoutReview = savePlace(isActive = true)
        saveRecommend(me, withoutReview)

        val unrecommended = savePlace(isActive = true)
        saveRecommend(me, unrecommended, recommended = false)

        val inactive = savePlace(isActive = false)
        saveRecommend(me, inactive)

        val recommendedByOtherOnly = savePlace(isActive = true)
        saveRecommend(other, recommendedByOtherOnly)
        clearPersistenceContext()

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(me.id!!, null, emptyList())

        val places = service.getRecommendedPlaces().places.associateBy { it.placeId }

        places.keys shouldBe setOf(recommendedByBoth.id!!, withoutReview.id!!)

        val both = places.getValue(recommendedByBoth.id!!)
        both.placeName shouldBe recommendedByBoth.placeName
        both.recommendCount shouldBe 2L
        both.reviewCount shouldBe 1L
        both.recommended shouldBe true

        val noReview = places.getValue(withoutReview.id!!)
        noReview.recommendCount shouldBe 1L
        noReview.reviewCount shouldBe 0L
        noReview.recommended shouldBe true
    }

    private fun assertRecommendedPlacesQueryCount(placeCount: Int) {
        val member = saveMember()
        repeat(placeCount) {
            val place = savePlace(isActive = true)
            saveRecommend(member, place)
            saveReview(member, place)
        }
        clearPersistenceContext()

        val (selectCount, responseSize) = measure(member.id!!)

        responseSize shouldBe placeCount
        selectCount shouldBe 3L
    }

    private fun measure(memberId: Long): Pair<Long, Int> {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(memberId, null, emptyList())

        val statistics = entityManager.entityManagerFactory
            .unwrap(SessionFactory::class.java)
            .statistics
        statistics.clear()

        val response = service.getRecommendedPlaces()

        return statistics.prepareStatementCount to response.places.size
    }

    private fun clearPersistenceContext() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun saveMember(): Member {
        val member = Member(
            email = "query-count-${++sequence}@gsm.hs.kr",
            password = "encoded_password",
            name = "홍길동",
            grade = 1,
            department = Department.SW,
            gender = Gender.MALE,
            role = Role.ROLE_STUDENT
        )
        entityManager.persist(member)
        return member
    }

    private fun savePlace(isActive: Boolean): Place {
        val place = Place(
            externalPlaceId = "query-count-place-${++sequence}",
            placeName = "테스트 장소 $sequence",
            address = "광주",
            latitude = 35.1,
            longitude = 126.1,
            categoryGroupName = "음식점",
            categoryName = "한식",
            isActive = isActive,
            lastSyncedAt = LocalDateTime.now()
        )
        entityManager.persist(place)
        return place
    }

    private fun saveRecommend(member: Member, place: Place, recommended: Boolean = true) {
        val now = LocalDateTime.now()
        entityManager.persist(
            PlaceRecommend(
                place = place,
                member = member,
                recommended = recommended,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun saveReview(member: Member, place: Place, deleted: Boolean = false) {
        val review = Review(place = place, member = member, content = "테스트 후기")
        if (deleted) {
            review.softDelete(deletedBy = "test")
        }
        entityManager.persist(review)
    }
}
