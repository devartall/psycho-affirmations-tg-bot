package com.devartall.psycho.bot.service.handlers

import com.devartall.psycho.bot.service.AdminService
import com.devartall.psycho.bot.service.MusicTrackService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class AudioHandler(
    private val adminService: AdminService,
    private val musicTrackService: MusicTrackService,
) {
    fun handleAudio(message: Message): SendMessage? {
        if (!adminService.isAdmin(message.from.id)) {
            return null
        }

        val audio = message.audio
        musicTrackService.addMusicTrack(
            fileId = audio.fileId,
            authorId = message.from.id,
            authorUsername = message.from.userName,
            artistName = audio.performer,
            trackTitle = audio.title
        )

        return SendMessage().apply {
            text = "Музыкальный трек успешно добавлен"
        }
    }
}