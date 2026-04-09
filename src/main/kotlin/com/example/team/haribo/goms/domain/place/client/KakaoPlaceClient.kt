package com.example.team.haribo.goms.domain.place.client

import com.example.team.haribo.goms.domain.place.dto.response.KakaoPlaceSearchResponse
import reactor.core.publisher.Mono
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoPlaceClient(
    @Qualifier("kakaoWebClient")
    private val kakaoWebClient: WebClient,
    @Value("\${kakao.rest-api-key}")
    private val restApiKey: String
) {

    fun searchByCategory(
        categoryGroupCode: String,
        x: String,
        y: String,
        radius: Int,
        page: Int,
        size: Int
    ): KakaoPlaceSearchResponse {
        return kakaoWebClient.get()
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
            .header("Authorization", "KakaoAK $restApiKey")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("No response body")
                    .flatMap { Mono.error(IllegalStateException("Kakao category api failed: ${response.statusCode()} - $it")) }
            }
            .bodyToMono(KakaoPlaceSearchResponse::class.java)
            .block() ?: throw IllegalStateException("Kakao category api response is null")
    }

    fun searchByKeyword(
        keyword: String,
        x: String,
        y: String,
        radius: Int,
        page: Int,
        size: Int
    ): KakaoPlaceSearchResponse {
        return kakaoWebClient.get()
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
            .header("Authorization", "KakaoAK $restApiKey")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("No response body")
                    .flatMap { Mono.error(IllegalStateException("Kakao keyword api failed: ${response.statusCode()} - $it")) }
            }
            .bodyToMono(KakaoPlaceSearchResponse::class.java)
            .block() ?: throw IllegalStateException("Kakao keyword api response is null")
    }
}