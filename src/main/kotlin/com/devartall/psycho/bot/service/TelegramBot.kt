package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.config.BotConfig
import com.devartall.psycho.bot.service.handlers.CommandHandler
import com.devartall.psycho.bot.service.handlers.KeyboardHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class TelegramBot(
    config: BotConfig,
    val commandHandler: CommandHandler,
    private val keyboardHelper: KeyboardHelper,
    private val affirmationService: AffirmationService
) : TelegramLongPollingBot(config.token) {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun getBotUsername(): String = "PsychoBot"

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return

        val message = update.message
        val response = when {
            message.isCommand -> commandHandler.handleCommand(message)
            message.hasText() -> handleTextMessage(message)
            else -> null
        }

        if (response != null) {
            sendResponse(response, message)
        }
    }

    private fun sendResponse(response: SendMessage, message: Message) {
        val chatIdString = message.chatId.toString()
        val userId = message.from.id

        val commands = SetMyCommands.builder()
            .commands(commandHandler.getUserCommands(userId))
            .scope(BotCommandScopeChat(chatIdString))
            .build()
        execute(commands)

        response.apply {
            enableMarkdown(true)
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            chatId = chatIdString
        }
        sendMessage(response)
    }

    private fun handleTextMessage(message: Message): SendMessage {
        return SendMessage().apply {
            text = when (message.text) {
                KeyboardHelper.GET_AFFIRMATION_BUTTON -> {
                    affirmationService.getRandomAffirmation()?.let { affirmation ->
                        "${KeyboardHelper.GET_AFFIRMATION_PREFIX} ${affirmation.text}"
                    } ?: "К сожалению, сейчас нет доступных аффирмаций"
                }

                else -> "Используйте кнопку \"${KeyboardHelper.GET_AFFIRMATION_BUTTON}\" для получения аффирмации или команду /start для просмотра инструкций"
            }
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