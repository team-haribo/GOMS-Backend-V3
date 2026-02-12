package com.example.team.haribo.goms.global.security

import com.example.team.haribo.goms.global.jwt.JwtProperties
import com.example.team.haribo.goms.global.jwt.JwtProvider
import tools.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig {

    @Bean
    fun jwtProvider(jwtProperties: JwtProperties): JwtProvider {
        return JwtProvider(jwtProperties)
    }

    @Bean
    fun jwtAuthenticationFilter(
        jwtProvider: JwtProvider,
        objectMapper: ObjectMapper
    ): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtProvider, objectMapper)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        objectMapper: ObjectMapper
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(JwtAuthenticationEntryPoint(objectMapper))
                it.accessDeniedHandler(JwtAccessDeniedHandler(objectMapper))
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/",
                    "/health",
                    "/healthz",
                    "/actuator/health",
                    "/actuator/info",
                    "/error"
                ).permitAll()

                it.requestMatchers("/api/v3/auth/email-verifications/send").permitAll()
                it.requestMatchers("/api/v3/auth/email-verifications/confirm").permitAll()
                it.requestMatchers("/api/v3/auth/signup").permitAll()
                it.requestMatchers("/api/v3/auth/signin").permitAll()
                it.requestMatchers("/api/v3/auth/signout").permitAll()
                it.requestMatchers("/api/v3/auth/reissue").permitAll()
                it.requestMatchers("/api/v3/auth/password").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
