package com.devartall.psycho.bot.service.handlers

import com.devartall.psycho.bot.service.AffirmationService
import com.devartall.psycho.bot.service.MusicTrackService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class KeyboardHandler(
    private val affirmationService: AffirmationService,
    private val musicTrackService: MusicTrackService,
) {
    companion object {
        const val GET_AFFIRMATION_PREFIX = "\uD83E\uDD0D"
        const val GET_AFFIRMATION_BUTTON = "$GET_AFFIRMATION_PREFIX Получить аффирмацию"
        const val GET_MUSIC_PREFIX = "🎵"
        const val GET_MUSIC_BUTTON = "$GET_MUSIC_PREFIX Получить музыкальный трек"
    }

    fun handleAffirmationButton(): SendMessage {
        return SendMessage().apply {
            text = affirmationService.getRandomAffirmation()?.let { affirmation ->
                "$GET_AFFIRMATION_PREFIX ${affirmation.text}"
            } ?: "К сожалению, сейчас нет доступных аффирмаций"
        }
    }

    fun handleMusicButton(message: Message): SendAudio? {
        val randomTrack = musicTrackService.getRandomMusicTrack() ?: return null

        return SendAudio().apply {
            chatId = message.chat.id.toString()
            audio = InputFile(randomTrack.fileId)
        }
    }

    fun createReplyKeyboardMarkup(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.selective = true

        val rows = ArrayList<KeyboardRow>()
        val row1 = KeyboardRow()
        row1.add(GET_AFFIRMATION_BUTTON)
        val row2 = KeyboardRow()
        row2.add(GET_MUSIC_BUTTON)
        rows.add(row1)
        rows.add(row2)

        keyboard.keyboard = rows
        return keyboard
    }
} 