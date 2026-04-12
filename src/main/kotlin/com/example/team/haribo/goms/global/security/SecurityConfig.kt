package com.example.team.haribo.goms.global.security

import com.example.team.haribo.goms.global.jwt.JwtProperties
import com.example.team.haribo.goms.global.jwt.JwtProvider
import com.example.team.haribo.goms.global.log.RequestLoggingFilter
import jakarta.servlet.Filter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import tools.jackson.databind.ObjectMapper

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
    fun jwtAuthenticationFilterRegistration(
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration = FilterRegistrationBean(jwtAuthenticationFilter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun requestLoggingFilterRegistration(
        requestLoggingFilter: RequestLoggingFilter
    ): FilterRegistrationBean<RequestLoggingFilter> {
        val registration = FilterRegistrationBean(requestLoggingFilter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        requestLoggingFilter: RequestLoggingFilter,
        objectMapper: ObjectMapper
    ): SecurityFilterChain {
        return http
            .cors {  }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(JwtAuthenticationEntryPoint(objectMapper))
                it.accessDeniedHandler(JwtAccessDeniedHandler(objectMapper))
            }
            .authorizeHttpRequests {
                // COMMON
                it.requestMatchers("/error").permitAll()
                it.requestMatchers("/v3/api-docs/**").permitAll()
                it.requestMatchers("/swagger-ui.html").permitAll()
                it.requestMatchers("/swagger-ui/**").permitAll()

                // AUTH
                it.requestMatchers("/api/v3/auth/email-verifications/send").permitAll()
                it.requestMatchers("/api/v3/auth/email-verifications/confirm").permitAll()
                it.requestMatchers("/api/v3/auth/signup").permitAll()
                it.requestMatchers("/api/v3/auth/signin").permitAll()
                it.requestMatchers("/api/v3/auth/signout").permitAll()
                it.requestMatchers("/api/v3/auth/reissue").permitAll()
                it.requestMatchers("/api/v3/auth/password").permitAll()

                // HEALTH CHECK
                it.requestMatchers("/api/v3/health").permitAll()

                // MEMBER
                it.requestMatchers("/api/v3/member/withdraw").authenticated()
                it.requestMatchers("/api/v3/member/myrole").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // OUTING
                it.requestMatchers("/api/v3/outing/status").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/outing/in").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/outing/out").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/outing/list").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/outing/count").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/outing/search").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // LATE-RANK
                it.requestMatchers("/api/v3/late/rank").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // PLACE
                it.requestMatchers("/api/v3/place/hot-place").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/search").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/recommend/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/recommended").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/recommended/count").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/review/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/review/count").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/sync").hasAnyRole("STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/place/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")


                // REVIEW
                it.requestMatchers("/api/v3/review/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // REPORT
                it.requestMatchers("/api/v3/report/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")
                it.requestMatchers("/api/v3/report/my").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // DEVICE TOKEN
                it.requestMatchers("/api/v3/notification/token/**").hasAnyRole("STUDENT", "STUDENT_COUNCIL")

                // STUDENT-COUNCIL
                it.requestMatchers("/api/v3/student-council/**").hasAnyRole("STUDENT_COUNCIL")
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(requestLoggingFilter, JwtAuthenticationFilter::class.java)
            .build()
    }
}