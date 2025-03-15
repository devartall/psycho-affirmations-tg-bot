package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.config.BotConfig
import com.devartall.psycho.bot.service.handlers.CommandHandler
import com.devartall.psycho.bot.service.handlers.KeyboardHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import jakarta.annotation.PostConstruct

@Component
class TelegramBot(
    config: BotConfig,
    private val commandHandler: CommandHandler,
    private val keyboardHelper: KeyboardHelper,
    private val affirmationService: AffirmationService
) : TelegramLongPollingBot(config.token) {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    @PostConstruct
    fun init() {
        log.info("Бот инициализирован, команды сброшены и установлены заново")
    }

    override fun getBotUsername(): String = "PsychoBot"

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return

        val message = update.message
        val response = when {
            message.isCommand -> commandHandler.handleCommand(message)
            message.hasText() -> handleTextMessage(message)
            else -> null
        }

        val updateCommands = commandHandler.updateCommands(message)
        execute(updateCommands)

        response?.let { sendMessage(it) }
    }

    private fun handleTextMessage(message: Message): SendMessage {
        return SendMessage().apply {
            chatId = message.chatId.toString()
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            text = when (message.text) {
                KeyboardHelper.GET_AFFIRMATION_BUTTON -> {
                    affirmationService.getRandomAffirmation()?.let { affirmation ->
                        "🎯 ${affirmation.text}"
                    } ?: "К сожалению, сейчас нет доступных аффирмаций"
                }
                else -> "Используйте кнопку \"${KeyboardHelper.GET_AFFIRMATION_BUTTON}\" для получения аффирмации или команду /start для просмотра инструкций"
            }
            enableMarkdown(true)
        }
    }

    private fun sendMessage(message: SendMessage) {
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            log.error("Ошибка при отправке сообщения", e)
        }
    }
} 