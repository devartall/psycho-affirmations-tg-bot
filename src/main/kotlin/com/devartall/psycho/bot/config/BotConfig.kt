package com.devartall.psycho.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("bot")
data class BotConfig(
    val token: String,
    val adminPassword: String,
    val adminCacheRefreshInterval: Duration
) 