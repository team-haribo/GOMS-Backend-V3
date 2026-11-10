package com.teamharibo.goms.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OutingTimeProperties::class)
class OutingTimeConfig
