package com.example.team.haribo.goms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class GomsServerV3Application

fun main(args: Array<String>) {
    runApplication<GomsServerV3Application>(*args)
}
