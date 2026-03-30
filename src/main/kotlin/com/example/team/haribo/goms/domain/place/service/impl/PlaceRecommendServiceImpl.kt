package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendCountResponse
import com.example.team.haribo.goms.domain.place.dto.response.RecommendResponse
import com.example.team.haribo.goms.domain.place.entity.PlaceRecommend
import com.example.team.haribo.goms.domain.place.exception.AlreadyRecommendedPlaceException
import com.example.team.haribo.goms.domain.place.exception.AlreadyUnrecommendedPlaceException
import com.example.team.haribo.goms.domain.place.exception.NotFoundPlaceException
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.repository.PlaceRepository
import com.example.team.haribo.goms.domain.place.service.PlaceRecommendService
import com.example.team.haribo.goms.global.log.LogFormat
import com.example.team.haribo.goms.global.util.MemberUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceRecommendServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val memberUtil: MemberUtil
) : PlaceRecommendService {

    private val log = LoggerFactory.getLogger(PlaceRecommendServiceImpl::class.java)

    @Transactional
    override fun recommend(placeId: Long): RecommendResponse {
        val member = memberUtil.currentMember()

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 추천 시도",
                "memberId" to member.id,
                "placeId" to placeId
            )
        )

        val place = placeRepository.findById(placeId).orElseThrow {
            log.warn(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 추천 실패",
                    "memberId" to member.id,
                    "placeId" to placeId,
                    "reason" to "존재하지 않는 장소"
                )
            )
            NotFoundPlaceException()
        }

        val existing = recommendRepository.findByPlaceIdAndMemberId(placeId, member.id!!).orElse(null)

        if (existing == null) {
            recommendRepository.save(
                PlaceRecommend(
                    place = place,
                    member = member,
                    recommended = true,
                    createdAt = LocalDateTime.now()
                )
            )

            log.info(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 추천 완료",
                    "memberId" to member.id,
                    "placeId" to placeId
                )
            )

            return RecommendResponse(true)
        }

        if (existing.recommended) {
            log.warn(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 추천 실패",
                    "memberId" to member.id,
                    "placeId" to placeId,
                    "reason" to "이미 추천한 장소"
                )
            )
            throw AlreadyRecommendedPlaceException()
        }

        existing.recommended = true
        existing.createdAt = LocalDateTime.now()
        recommendRepository.save(existing)

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 추천 완료",
                "memberId" to member.id,
                "placeId" to placeId
            )
        )

        return RecommendResponse(true)
    }

    @Transactional
    override fun unrecommend(placeId: Long): RecommendResponse {
        val memberId = memberUtil.currentMemberId()

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 추천 취소 시도",
                "memberId" to memberId,
                "placeId" to placeId
            )
        )

        if (!placeRepository.existsById(placeId)) {
            log.warn(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 추천 취소 실패",
                    "memberId" to memberId,
                    "placeId" to placeId,
                    "reason" to "존재하지 않는 장소"
                )
            )
            throw NotFoundPlaceException()
        }

        val existing = recommendRepository.findByPlaceIdAndMemberId(placeId, memberId)
            .orElseThrow {
                log.warn(
                    LogFormat.message(
                        domain = "PLACE",
                        event = "장소 추천 취소 실패",
                        "memberId" to memberId,
                        "placeId" to placeId,
                        "reason" to "추천하지 않은 장소"
                    )
                )
                AlreadyUnrecommendedPlaceException()
            }

        if (!existing.recommended) {
            log.warn(
                LogFormat.message(
                    domain = "PLACE",
                    event = "장소 추천 취소 실패",
                    "memberId" to memberId,
                    "placeId" to placeId,
                    "reason" to "이미 추천 취소된 장소"
                )
            )
            throw AlreadyUnrecommendedPlaceException()
        }

        existing.recommended = false
        recommendRepository.save(existing)

        log.info(
            LogFormat.message(
                domain = "PLACE",
                event = "장소 추천 취소 완료",
                "memberId" to memberId,
                "placeId" to placeId
            )
        )

        return RecommendResponse(false)
    }

    @Transactional(readOnly = true)
    override fun getRecommendedPlaces(): PlacesResponse {
        val memberId = memberUtil.currentMemberId()
        val recommends = recommendRepository.findAllByMemberIdAndRecommendedTrue(memberId)

        if (recommends.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val placeIds = recommends.mapNotNull { it.place.id }.distinct()

        val recommendCountMap = recommendRepository.countRecommendedByPlaceIds(placeIds)
            .associate { it.placeId to it.recommendCount }

        return PlacesResponse(
            places = placeIds.map { placeId ->
                PlaceSummaryResponse(
                    placeId = placeId,
                    reviewCount = 0,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = true
                )
            }
        )
    }

    @Transactional(readOnly = true)
    override fun getRecommendedCount(): RecommendCountResponse {
        val memberId = memberUtil.currentMemberId()
        val count = recommendRepository.countByMemberIdAndRecommendedTrue(memberId)
        return RecommendCountResponse(recommendCount = count)
    }
}