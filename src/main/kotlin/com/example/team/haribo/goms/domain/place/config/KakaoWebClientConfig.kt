package com.example.team.haribo.goms.domain.place.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class KakaoWebClientConfig {

    @Bean
    fun kakaoWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
            .responseTimeout(Duration.ofSeconds(60))
            .secure()
            .doOnConnected { connection ->
                connection
                    .addHandlerLast(ReadTimeoutHandler(60, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(60, TimeUnit.SECONDS))
            }

        return WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}