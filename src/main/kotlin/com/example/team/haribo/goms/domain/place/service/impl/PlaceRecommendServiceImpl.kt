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
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceRecommendServiceImpl(
    private val placeRepository: PlaceRepository,
    private val recommendRepository: PlaceRecommendRepository,
    private val memberUtil: MemberUtil
) : PlaceRecommendService {

    @Transactional
    override fun recommend(placeId: Long): RecommendResponse {
        val member = memberUtil.currentMember()
        val place = placeRepository.findById(placeId).orElseThrow { NotFoundPlaceException() }

        val existing = recommendRepository.findByPlaceIdAndMemberId(placeId, member.id!!).orElse(null)

        if (existing == null) {
            recommendRepository.save(
                PlaceRecommend(
                    place = place,
                    member = member,
                    recommended = true
                )
            )
            return RecommendResponse(true)
        }

        if (existing.recommended) throw AlreadyRecommendedPlaceException()

        existing.recommended = true
        recommendRepository.save(existing)

        return RecommendResponse(true)
    }

    @Transactional
    override fun unrecommend(placeId: Long): RecommendResponse {
        val memberId = memberUtil.currentMemberId()

        if (!placeRepository.existsById(placeId)) throw NotFoundPlaceException()

        val existing = recommendRepository.findByPlaceIdAndMemberId(placeId, memberId)
            .orElseThrow { AlreadyUnrecommendedPlaceException() }

        if (!existing.recommended) throw AlreadyUnrecommendedPlaceException()

        existing.recommended = false
        recommendRepository.save(existing)

        return RecommendResponse(false)
    }

    @Transactional(readOnly = true)
    override fun getRecommendedPlaces(): PlacesResponse {
        val memberId = memberUtil.currentMemberId()
        val recommends = recommendRepository.findAllByMemberIdAndRecommendedTrue(memberId)

        if (recommends.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val placeIds = recommends.mapNotNull { it.place.id }
        val recommendCountMap = recommendRepository.countRecommendedByPlaceIds(placeIds)
            .associate { it.placeId to it.recommendCount }

        return PlacesResponse(
            places = recommends.map { pr ->
                val placeId = pr.place.id!!
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
        return RecommendCountResponse(recommendRepository.countByMemberIdAndRecommendedTrue(memberId))
    }
}
