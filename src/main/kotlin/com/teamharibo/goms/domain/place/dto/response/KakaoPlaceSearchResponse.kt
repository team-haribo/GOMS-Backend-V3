package com.teamharibo.goms.domain.place.dto.response

data class KakaoPlaceSearchResponse(
    val meta: KakaoPlaceMeta,
    val documents: List<KakaoPlaceDocument>
)