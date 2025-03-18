package com.devartall.psycho.bot.service.handlers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class KeyboardHelper {
    companion object {
        const val GET_AFFIRMATION_BUTTON = "🎯 Получить аффирмацию"
    }

    fun createReplyKeyboardMarkup(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.selective = true

        val rows = ArrayList<KeyboardRow>()
        val row = KeyboardRow()
        row.add(GET_AFFIRMATION_BUTTON)
        rows.add(row)
        
        keyboard.keyboard = rows
        return keyboard
    }
} 