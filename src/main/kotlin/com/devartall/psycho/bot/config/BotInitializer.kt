package com.devartall.psycho.bot.config

import com.devartall.psycho.bot.service.TelegramBot
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class BotInitializer {
    private val log = LoggerFactory.getLogger(BotInitializer::class.java)

    private fun maskToken(token: String): String {
        if (token.length <= 8) return "*".repeat(token.length)
        return "${token.take(4)}${"*".repeat(token.length - 8)}${token.takeLast(4)}"
    }

    @Bean(name = ["telegramBotInitializer"])
    @ConditionalOnProperty(name = ["telegram.bot.initializer.enabled"])
    fun initializeBot(bot: TelegramBot, botConfig: BotConfig) = ApplicationRunner {
        try {
            log.info("Инициализация бота с токеном: {}", maskToken(botConfig.token))
            
            // Проверяем подключение к API Telegram
            val botInfo = bot.execute(GetMe())
            log.info("Бот успешно подключен: @{}", botInfo.userName)
            
            // Регистрируем бота
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(bot)
            
            log.info("Бот успешно запущен и готов к работе")
        } catch (e: TelegramApiException) {
            val errorMessage = "Ошибка при инициализации бота: ${e.message}"
            log.error(errorMessage)
            throw RuntimeException(errorMessage, e)
        }
    }
} 