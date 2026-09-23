package com.teamharibo.goms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GomsApplication

fun main(args: Array<String>) {
    runApplication<GomsApplication>(*args)
}