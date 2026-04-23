package com.example.team.haribo.goms.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@EnableScheduling
@Configuration
class SchedulerConfig(
    @Value("\${scheduler.pool-size:10}")
    private val poolSize: Int,
    @Value("\${scheduler.thread-name-prefix:goms-scheduler-}")
    private val threadNamePrefix: String
) {

    @Bean
    fun taskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = poolSize
        scheduler.setThreadNamePrefix(threadNamePrefix)
        scheduler.initialize()
        return scheduler
    }
}