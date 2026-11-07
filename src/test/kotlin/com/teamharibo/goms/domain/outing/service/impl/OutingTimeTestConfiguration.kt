package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.outing.util.OutingTimeValidator
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.ZoneId

/**
 * 동시성 테스트는 상태 경합만 검증하므로, 실행 시각과 회원 기수에 따라 결과가 달라지지 않도록
 * 외출 가능 시간 검증을 통과시키는 OutingTimeValidator와 Asia/Seoul Clock을 제공한다.
 * 외출 가능 시간 검증 자체는 OutingTimeValidatorTest에서 다룬다.
 */
@TestConfiguration(proxyBeanMethods = false)
class OutingTimeTestConfiguration {

    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Asia/Seoul"))

    @Bean
    fun outingTimeValidator(): OutingTimeValidator = mockk(relaxed = true)
}
