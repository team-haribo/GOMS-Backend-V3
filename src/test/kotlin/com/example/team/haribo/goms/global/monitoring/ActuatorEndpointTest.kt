package com.example.team.haribo.goms.global.monitoring

import com.example.team.haribo.goms.global.log.RequestLoggingFilter
import com.example.team.haribo.goms.global.security.SecurityConfig
import com.example.team.haribo.goms.global.jwt.JwtProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ActuatorEndpointTestApplication::class],
    properties = [
        "spring.profiles.active=",
        "jwt.secret=actuator-test-secret-must-be-at-least-32-bytes",
        "jwt.access-exp-seconds=300",
        "jwt.refresh-exp-seconds=3600",
        "management.health.redis.enabled=false",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    ]
)
class ActuatorEndpointTest @Autowired constructor(
    @LocalServerPort private val port: Int,
    private val jwtProvider: JwtProvider
) {

    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun `actuator endpoints are accessible without authentication`() {
        val health = get("/actuator/health")
        val info = get("/actuator/info")
        val prometheus = get("/actuator/prometheus")

        assertEquals(200, health.statusCode())
        assertEquals(200, info.statusCode())
        assertEquals(200, prometheus.statusCode())
        assertTrue(prometheus.body().contains("# HELP"))
        assertTrue(prometheus.body().contains("application=\"goms-server-v3\""))
    }

    @Test
    fun `authenticated requests can access the exposed actuator endpoints`() {
        val accessToken = jwtProvider.createAccessToken(1L, "ROLE_STUDENT")

        assertEquals(200, get("/actuator/health", accessToken).statusCode())
        assertEquals(200, get("/actuator/info", accessToken).statusCode())
        assertEquals(200, get("/actuator/prometheus", accessToken).statusCode())
    }

    @Test
    fun `protected endpoints still require authentication`() {
        assertEquals(401, get("/api/v3/member/withdraw").statusCode())
        assertEquals(401, get("/actuator/beans").statusCode())
    }

    private fun get(path: String, accessToken: String? = null): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port$path"))
            .apply {
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
            }
            .GET()
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }
}

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(SecurityConfig::class, RequestLoggingFilter::class)
private class ActuatorEndpointTestApplication
