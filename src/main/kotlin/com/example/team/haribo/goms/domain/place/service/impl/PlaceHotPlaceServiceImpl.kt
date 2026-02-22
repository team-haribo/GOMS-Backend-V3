package com.example.team.haribo.goms.domain.place.service.impl

import com.example.team.haribo.goms.domain.place.dto.response.PlaceSummaryResponse
import com.example.team.haribo.goms.domain.place.dto.response.PlacesResponse
import com.example.team.haribo.goms.domain.place.repository.PlaceRecommendRepository
import com.example.team.haribo.goms.domain.place.service.PlaceHotPlaceService
import com.example.team.haribo.goms.global.exception.ErrorCode
import com.example.team.haribo.goms.global.exception.GlobalException
import com.example.team.haribo.goms.global.util.MemberUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceHotPlaceServiceImpl(
    private val recommendRepository: PlaceRecommendRepository,
    private val memberUtil: MemberUtil
) : PlaceHotPlaceService {

    @Transactional(readOnly = true)
    override fun getHotPlaces(days: Long?): PlacesResponse {
        val d = (days ?: 3L).also {
            if (it <= 0L || it > 30L) throw GlobalException(ErrorCode.INVALID_REQUEST)
        }

        val memberId = memberUtil.currentMemberId()
        val recommendedIds = recommendRepository.findRecommendedPlaceIds(memberId).toSet()

        val since = LocalDateTime.now().minusDays(d)

        val hotIds = recommendRepository.findHotPlaceIdsSince(
            since,
            PageRequest.of(0, 3)
        )

        if (hotIds.isEmpty()) {
            return PlacesResponse(places = emptyList())
        }

        val recommendCountMap = recommendRepository
            .countRecommendedByPlaceIdsSince(hotIds, since)
            .associate { it.placeId to it.recommendCount }

        return PlacesResponse(
            places = hotIds.map { placeId ->
                PlaceSummaryResponse(
                    placeId = placeId,
                    reviewCount = 0,
                    recommendCount = recommendCountMap[placeId] ?: 0L,
                    recommended = recommendedIds.contains(placeId)
                )
            }
        )
    }
}
