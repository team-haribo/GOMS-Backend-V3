package com.example.team.haribo.goms.domain.place.client

import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceSearchResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoPlaceClient(
    @Value("\${kakao.rest-api-key}") private val restApiKey: String
) {
    private val webClient = WebClient.builder()
        .baseUrl("https://dapi.kakao.com")
        .defaultHeader("Authorization", "KakaoAK $restApiKey")
        .build()

    fun searchByCategory(
        categoryGroupCode: String,
        x: String,
        y: String,
        radius: Int,
        page: Int,
        size: Int
    ): KakaoPlaceSearchResponse {
        return webClient.get()
            .uri {
                it.path("/v2/local/search/category.json")
                    .queryParam("category_group_code", categoryGroupCode)
                    .queryParam("x", x)
                    .queryParam("y", y)
                    .queryParam("radius", radius)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build()
            }
            .retrieve()
            .bodyToMono(KakaoPlaceSearchResponse::class.java)
            .block()!!
    }

    fun searchByKeyword(
        keyword: String,
        x: String,
        y: String,
        radius: Int,
        page: Int,
        size: Int
    ): KakaoPlaceSearchResponse {
        return webClient.get()
            .uri {
                it.path("/v2/local/search/keyword.json")
                    .queryParam("query", keyword)
                    .queryParam("x", x)
                    .queryParam("y", y)
                    .queryParam("radius", radius)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("sort", "distance")
                    .build()
            }
            .retrieve()
            .bodyToMono(KakaoPlaceSearchResponse::class.java)
            .block()!!
    }
}