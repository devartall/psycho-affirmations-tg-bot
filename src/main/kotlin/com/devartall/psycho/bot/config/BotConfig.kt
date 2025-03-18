package com.devartall.psycho.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bot")
data class BotConfig(
    val token: String,
    val adminPassword: String
) 