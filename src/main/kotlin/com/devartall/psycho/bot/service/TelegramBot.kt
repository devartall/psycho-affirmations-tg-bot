package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.config.BotConfig
import com.devartall.psycho.bot.service.handlers.AudioHandler
import com.devartall.psycho.bot.service.handlers.CommandHandler
import com.devartall.psycho.bot.service.handlers.KeyboardHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class TelegramBot(
    config: BotConfig,
    val commandHandler: CommandHandler,
    val audioHandler: AudioHandler,
    private val keyboardHandler: KeyboardHandler,
) : TelegramLongPollingBot(config.token) {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun getBotUsername(): String = "PsychoBot"

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return

        val message = update.message
        val response = when {
            message.isCommand -> commandHandler.handleCommand(message)
            message.hasAudio() -> audioHandler.handleAudio(message)
            message.hasText() -> handleTextMessage(message)
            else -> null
        }

        sendResponse(response, message)
    }

    private fun sendResponse(response: SendMessage?, message: Message) {
        val chatIdString = message.chatId.toString()
        val userId = message.from.id

        val commands = SetMyCommands.builder()
            .commands(commandHandler.getUserCommands(userId))
            .scope(BotCommandScopeChat(chatIdString))
            .build()
        execute(commands)

        response?.let {
            it.enableMarkdown(true)
            it.replyMarkup = keyboardHandler.createReplyKeyboardMarkup()
            it.chatId = chatIdString

            sendMessage(it)
        }
    }

    private fun handleTextMessage(message: Message): SendMessage? {
        return when (message.text) {
            KeyboardHandler.GET_AFFIRMATION_BUTTON -> keyboardHandler.handleAffirmationButton()
            KeyboardHandler.GET_MUSIC_BUTTON -> {
                val audio = keyboardHandler.handleMusicButton(message)
                    ?: return SendMessage().apply {
                        text = "Нет доступных музыкальных треков"
                    }

                sendAudio(audio)
                return null
            }

            else -> SendMessage().apply {
                text = "Используй команду ${CommandHandler.START_COMMAND} для просмотра инструкций"
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

    private fun sendAudio(audio: SendAudio) {
        try {
            execute(audio)
        } catch (e: TelegramApiException) {
            log.error("Ошибка при отправке аудио", e)
        }
    }
} 