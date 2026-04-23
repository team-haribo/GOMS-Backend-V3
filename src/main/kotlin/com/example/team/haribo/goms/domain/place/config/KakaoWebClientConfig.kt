package com.example.team.haribo.goms.domain.place.config

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.Http11SslContextSpec
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class KakaoWebClientConfig {

    @Bean
    @Qualifier("kakaoWebClient")
    fun kakaoWebClient(): WebClient {
        val sslProvider = SslProvider.builder()
            .sslContext(Http11SslContextSpec.forClient())
            .handshakeTimeoutMillis(60_000)
            .closeNotifyFlushTimeoutMillis(10_000)
            .closeNotifyReadTimeoutMillis(10_000)
            .handlerConfigurator { sslHandler: SslHandler ->
                sslHandler.setHandshakeTimeoutMillis(60_000)
            }
            .build()

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60_000)
            .responseTimeout(Duration.ofSeconds(60))
            .secure(sslProvider)
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