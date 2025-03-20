package com.devartall.psycho.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PsychoApplication

fun main(args: Array<String>) {
    runApplication<PsychoApplication>(*args)
}
