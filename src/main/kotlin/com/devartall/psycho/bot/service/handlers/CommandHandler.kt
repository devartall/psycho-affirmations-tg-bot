package com.devartall.psycho.bot.service.handlers

import com.devartall.psycho.bot.service.AdminService
import com.devartall.psycho.bot.service.AffirmationService
import com.devartall.psycho.bot.service.MusicTrackService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand

@Component
class CommandHandler(
    private val adminService: AdminService,
    private val affirmationService: AffirmationService,
    private val musicTrackService: MusicTrackService,
) {
    companion object {
        // Команды
        const val START_COMMAND = "/start"
        const val AUTH_COMMAND = "/auth"
        const val LOGOUT_COMMAND = "/logout"
        const val ADD_COMMAND = "/add"
        const val LIST_COMMAND = "/list"
        const val DELETE_ALL_COMMAND = "/delete"
        const val DELETE_TRACK_COMMAND = "/deletetrack"
        const val LIST_TRACKS_COMMAND = "/listtracks"

        // Описания команд
        private const val START_DESCRIPTION = "Начать работу с ботом и получить инструкции"
        private const val AUTH_DESCRIPTION = "Авторизоваться как администратор"
        private const val LOGOUT_DESCRIPTION = "Выйти из режима администратора"
        private const val ADD_DESCRIPTION = "Добавить новую аффирмацию"
        private const val LIST_DESCRIPTION = "Показать список всех аффирмаций"
        private const val DELETE_ALL_DESCRIPTION = "Удалить все аффирмации"
        private const val LIST_TRACKS_DESCRIPTION = "Показать список всех музыкальных треков"
        private const val DELETE_TRACK_DESCRIPTION = "Удалить музыкальный трек"
    }

    fun getDefaultCommands(): List<BotCommand> = listOf(
        BotCommand(START_COMMAND.removePrefix("/"), START_DESCRIPTION),
        BotCommand(AUTH_COMMAND.removePrefix("/"), AUTH_DESCRIPTION)
    )

    fun getAdminCommands(): List<BotCommand> = listOf(
        BotCommand(START_COMMAND.removePrefix("/"), START_DESCRIPTION),
        BotCommand(LOGOUT_COMMAND.removePrefix("/"), LOGOUT_DESCRIPTION),
        BotCommand(ADD_COMMAND.removePrefix("/"), ADD_DESCRIPTION),
        BotCommand(LIST_COMMAND.removePrefix("/"), LIST_DESCRIPTION),
        BotCommand(DELETE_ALL_COMMAND.removePrefix("/"), DELETE_ALL_DESCRIPTION),
        BotCommand(DELETE_TRACK_COMMAND.removePrefix("/"), DELETE_TRACK_DESCRIPTION),
        BotCommand(LIST_TRACKS_COMMAND.removePrefix("/"), LIST_TRACKS_DESCRIPTION)
    )

    fun handleCommand(message: Message): SendMessage {
        val command = message.text.split(" ")[0]

        if (command != START_COMMAND &&
            isAdminCommand(command) &&
            !adminService.isAdmin(message.from.id)
        ) {
            return SendMessage().apply {
                text = "Эта команда доступна только администраторам"
            }
        }

        return when (command) {
            START_COMMAND -> handleStartCommand(message)
            AUTH_COMMAND -> handleAuthCommand(message)
            LOGOUT_COMMAND -> handleLogoutCommand(message)
            ADD_COMMAND -> handleAddCommand(message)
            LIST_COMMAND -> handleListCommand()
            DELETE_ALL_COMMAND -> handleDeleteAllCommand()
            DELETE_TRACK_COMMAND -> handleDeleteTrackCommand(message)
            LIST_TRACKS_COMMAND -> handleListTracksCommand()
            else -> SendMessage().apply {
                text = "Неизвестная команда. Используйте $START_COMMAND для получения инструкций"
            }
        }
    }

    private fun handleListTracksCommand(): SendMessage {
        val tracks = musicTrackService.getAllMusicTracks()
        return SendMessage().apply {
            text = if (tracks.isEmpty()) {
                "Список музыкальных треков пуст"
            } else {
                buildString {
                    append("🎵 Список всех музыкальных треков:\n\n")
                    tracks.forEach { track ->
                        append("ID: ${track.id}, Исполнитель: ${track.artistName}, Композиция: ${track.trackTitle}, Добавил: @${track.authorUsername}\n")
                    }
                }
            }
        }
    }

    private fun isAdminCommand(command: String): Boolean {
        return getAdminCommands().map { "/${it.command}" }.contains(command)
    }

    private fun handleStartCommand(message: Message): SendMessage {
        val helpText = buildString {
            append("👋 Привет! Я бот для работы с аффирмациями и музыкальными треками.\n\n")
            append("🔍 Нажмите на кнопку \"${KeyboardHandler.GET_AFFIRMATION_BUTTON}\" на клавиатуре, чтобы получить случайную аффирмацию.\n\n")
            append("🎵 Нажмите на кнопку \"${KeyboardHandler.GET_MUSIC_BUTTON}\" на клавиатуре, чтобы получить случайный музыкальный трек.\n\n")

            if (adminService.isAdmin(message.from.id)) {
                append("👑 Вы являетесь администратором. Доступные команды:\n")
                append("• $ADD_COMMAND <текст> - добавить новую аффирмацию\n")
                append("• $LIST_COMMAND - показать список всех аффирмаций\n")
                append("• $DELETE_ALL_COMMAND - удалить все аффирмации\n")
                append("• $LIST_TRACKS_COMMAND - получить список музыкальных треков\n")
                append("• $DELETE_TRACK_COMMAND <id> - удалить музыкальный трек\n")
                append("• $LOGOUT_COMMAND - выйти из режима администратора\n")
            } else {
                append("🔐 Если вы администратор, используйте команду:\n")
                append("• $AUTH_COMMAND <пароль> - авторизоваться как администратор\n")
            }
        }

        return SendMessage().apply {
            text = helpText
        }
    }

    private fun handleAuthCommand(message: Message): SendMessage {
        val response = SendMessage().apply {
            text = when {
                adminService.isAdmin(message.from.id) ->
                    "Вы уже являетесь администратором"

                message.text.split(" ").size != 2 ->
                    "Для авторизации используйте команду в формате: $AUTH_COMMAND <пароль>"

                else -> {
                    val password = message.text.split(" ")[1]
                    if (adminService.checkPassword(password)) {
                        adminService.addAdmin(message.from)
                        "Вы успешно авторизованы как администратор"
                    } else {
                        "Неверный пароль"
                    }
                }
            }
        }
        return response
    }

    private fun handleLogoutCommand(message: Message): SendMessage {
        adminService.removeAdmin(message.from.id)
        val response = SendMessage().apply {
            text = "Вы успешно вышли из режима администратора"
        }
        return response
    }

    private fun handleAddCommand(message: Message): SendMessage {
        return SendMessage().apply {
            text = when {
                message.text.substringAfter(ADD_COMMAND).trim().isEmpty() ->
                    "Пожалуйста, укажите текст аффирмации после команды $ADD_COMMAND"

                else -> {
                    val text = message.text.substringAfter(ADD_COMMAND).trim()
                    affirmationService.addAffirmation(text, message.from)
                    "Аффирмация успешно добавлена"
                }
            }
        }
    }

    private fun handleListCommand(): SendMessage {
        val affirmations = affirmationService.getAllAffirmations()
        return SendMessage().apply {
            text = if (affirmations.isEmpty()) {
                "Список аффирмаций пуст"
            } else {
                buildString {
                    append("📝 Список всех аффирмаций:\n\n")
                    affirmations.forEachIndexed { index, affirmation ->
                        append("${index + 1}. ${affirmation.text}\n")
                        append("   _Добавил: @${affirmation.authorUsername}_\n\n")
                    }
                }
            }
        }
    }

    private fun handleDeleteAllCommand(): SendMessage {
        val count = affirmationService.deleteAllAffirmations()
        return SendMessage().apply {
            text = "Успешно удалено $count аффирмаций"
        }
    }

    private fun handleDeleteTrackCommand(message: Message): SendMessage {
        val id = message.text.substringAfter(DELETE_TRACK_COMMAND).trim()
        if (id.isEmpty()) {
            return SendMessage().apply {
                text = "Пожалуйста, укажите id трека после команды $DELETE_TRACK_COMMAND"
            }
        }

        val deleteResult = musicTrackService.deleteMusicTrack(id)
        return SendMessage().apply {
            text = when (deleteResult) {
                MusicTrackService.DeleteResult.WRONG_ID_FORMAT ->
                    "Некорректный формат id"

                MusicTrackService.DeleteResult.TRACK_NOT_FOUND ->
                    "Не найден трек с id=$id"

                MusicTrackService.DeleteResult.TRACK_DELETED ->
                    "Трек успешно удален"
            }
        }
    }

    fun clearAdminCache() {
        adminService.clearCache()
    }

    fun getUserCommands(userId: Long): List<BotCommand> {
        return if (adminService.isAdmin(userId)) {
            getAdminCommands()
        } else {
            getDefaultCommands()
        }
    }
} 