package com.devartall.psycho.bot.service.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat
import com.devartall.psycho.bot.service.AdminService
import com.devartall.psycho.bot.service.AffirmationService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands

@Component
class CommandHandler(
    private val adminService: AdminService,
    private val affirmationService: AffirmationService,
    private val keyboardHelper: KeyboardHelper,
) {
    companion object {
        // Команды
        const val START_COMMAND = "/start"
        const val AUTH_COMMAND = "/auth"
        const val LOGOUT_COMMAND = "/logout"
        const val ADD_COMMAND = "/add"
        const val LIST_COMMAND = "/list"
        const val DELETE_ALL_COMMAND = "/delete"

        // Описания команд
        private const val START_DESCRIPTION = "Начать работу с ботом и получить инструкции"
        private const val AUTH_DESCRIPTION = "Авторизоваться как администратор"
        private const val LOGOUT_DESCRIPTION = "Выйти из режима администратора"
        private const val ADD_DESCRIPTION = "Добавить новую аффирмацию"
        private const val LIST_DESCRIPTION = "Показать список всех аффирмаций"
        private const val DELETE_ALL_DESCRIPTION = "Удалить все аффирмации"
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
        BotCommand(DELETE_ALL_COMMAND.removePrefix("/"), DELETE_ALL_DESCRIPTION)
    )

    fun setDefaultCommands(chatId: Long): SetMyCommands {
        val commandScope = BotCommandScopeChat(chatId.toString())

        val setMyCommands = SetMyCommands.builder()
            .commands(getDefaultCommands())
            .scope(commandScope)
            .build()

        return setMyCommands
    }

    fun setAdminCommands(chatId: Long): SetMyCommands {
        val commandScope = BotCommandScopeChat(chatId.toString())

        val setMyCommands = SetMyCommands.builder()
            .commands(getAdminCommands())
            .scope(commandScope)
            .build()

        return setMyCommands
    }

    fun handleCommand(message: Message): SendMessage {
        val command = message.text.split(" ")[0]
        return when (command) {
            START_COMMAND -> handleStartCommand(message)
            AUTH_COMMAND -> handleAuthCommand(message)
            LOGOUT_COMMAND -> handleLogoutCommand(message)
            ADD_COMMAND -> handleAddCommand(message)
            LIST_COMMAND -> handleListCommand(message)
            DELETE_ALL_COMMAND -> handleDeleteAllCommand(message)
            else -> SendMessage().apply {
                chatId = message.chatId.toString()
                text = "Неизвестная команда. Используйте $START_COMMAND для получения инструкций."
                replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            }
        }
    }

    private fun handleStartCommand(message: Message): SendMessage {
        val helpText = buildString {
            append("👋 Привет! Я бот для работы с аффирмациями.\n\n")
            append("🔍 Нажмите на кнопку \"${KeyboardHelper.GET_AFFIRMATION_BUTTON}\" на клавиатуре, чтобы получить случайную аффирмацию.\n\n")

            if (adminService.isAdmin(message.from.id)) {
                append("👑 Вы являетесь администратором. Доступные команды:\n")
                append("• $ADD_COMMAND <текст> - добавить новую аффирмацию\n")
                append("• $LIST_COMMAND - показать список всех аффирмаций\n")
                append("• $DELETE_ALL_COMMAND - удалить все аффирмации\n")
                append("• $LOGOUT_COMMAND - выйти из режима администратора\n")
            } else {
                append("🔐 Если вы администратор, используйте команду:\n")
                append("• $AUTH_COMMAND <пароль> - авторизоваться как администратор\n")
            }
        }

        return SendMessage().apply {
            chatId = message.chatId.toString()
            text = helpText
            enableMarkdown(true)
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
        }
    }

    private fun handleAuthCommand(message: Message): SendMessage {
        val chatId = message.chatId.toString()
        val response = SendMessage().apply {
            this.chatId = chatId
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            text = when {
                adminService.isAdmin(message.from.id) ->
                    "Вы уже являетесь администратором"

                message.text.split(" ").size != 2 ->
                    "Для авторизации используйте команду в формате: $AUTH_COMMAND <пароль>"

                else -> {
                    val password = message.text.split(" ")[1]
                    if (adminService.checkPassword(password)) {
                        adminService.addAdmin(message.from)
                        updateCommands(message)
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
        val chatId = message.chatId.toString()
        val response = SendMessage().apply {
            this.chatId = chatId
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            text = if (adminService.isAdmin(message.from.id)) {
                adminService.removeAdmin(message.from.id)
                updateCommands(message)
                "Вы успешно вышли из режима администратора"
            } else {
                "Вы не являетесь администратором"
            }
        }
        return response
    }

    private fun handleAddCommand(message: Message): SendMessage {
        return SendMessage().apply {
            chatId = message.chatId.toString()
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            text = when {
                !adminService.isAdmin(message.from.id) ->
                    "Эта команда доступна только администраторам"

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

    private fun handleListCommand(message: Message): SendMessage {
        return SendMessage().apply {
            chatId = message.chatId.toString()
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            enableMarkdown(true)
            text = when {
                !adminService.isAdmin(message.from.id) ->
                    "Эта команда доступна только администраторам"

                else -> {
                    val affirmations = affirmationService.getAllAffirmations()
                    if (affirmations.isEmpty()) {
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
        }
    }

    private fun handleDeleteAllCommand(message: Message): SendMessage {
        return SendMessage().apply {
            chatId = message.chatId.toString()
            replyMarkup = keyboardHelper.createReplyKeyboardMarkup()
            text = when {
                !adminService.isAdmin(message.from.id) ->
                    "Эта команда доступна только администраторам"

                else -> {
                    val count = affirmationService.deleteAllAffirmations()
                    "Успешно удалено $count аффирмаций"
                }
            }
        }
    }

    fun updateCommands(message: Message): SetMyCommands {
        val chatId = message.chatId
        val isAdmin = adminService.isAdmin(message.from.id)

        return if (isAdmin) {
            setAdminCommands(chatId)
        } else {
            setDefaultCommands(chatId)
        }
    }
} 