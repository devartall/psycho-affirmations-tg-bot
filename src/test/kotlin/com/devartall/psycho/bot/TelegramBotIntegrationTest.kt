package com.devartall.psycho.bot

import com.devartall.psycho.bot.entity.Affirmation
import com.devartall.psycho.bot.repository.AdminRepository
import com.devartall.psycho.bot.repository.AffirmationRepository
import com.devartall.psycho.bot.service.TelegramBot
import com.devartall.psycho.bot.service.handlers.CommandHandler
import com.devartall.psycho.bot.service.handlers.KeyboardHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.*
import kotlin.test.assertTrue


class TelegramBotIntegrationTest(@Autowired bot: TelegramBot) : AbstractIntegrationTest() {

    private val spyBot: TelegramBot = spy(bot).also {
        doAnswer { null }.`when`(it).execute(any(SetMyCommands::class.java))
        doAnswer { null }.`when`(it).execute(any(SendMessage::class.java))
    }

    @Value("\${bot.admin-password}")
    private lateinit var adminPassword: String

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Autowired
    private lateinit var affirmationRepository: AffirmationRepository


    private val defaultUser: User = User().apply {
        id = 100L
        userName = "test_user_name"
        firstName = "test_first_name"
        lastName = "test_last_name"
    }
    private val defaultChat: Chat = Chat().apply { id = 10000L }

    @BeforeEach
    fun setUp() {
        spyBot.commandHandler.clearAdminCache()
        adminRepository.deleteAll()
        affirmationRepository.deleteAll()
    }

    @Test
    fun `should handle general message and respond with start command`() {
        val update = createMessage("Hello, bot!")

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).contains(CommandHandler.START_COMMAND)
    }

    @Test
    fun `should handle get affirmation button without affirmations`() {
        val update = createMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).isEqualTo("К сожалению, сейчас нет доступных аффирмаций")
    }

    @Test
    fun `should handle get affirmation button with one affirmation`() {
        val update = createMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

        val affirmationText1 = "affirmationText1"
        affirmationRepository.save(
            Affirmation(
                text = affirmationText1,
                authorId = defaultUser.id,
                authorUsername = defaultUser.userName
            )
        )

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).isEqualTo("${KeyboardHelper.GET_AFFIRMATION_PREFIX} $affirmationText1")
    }

    @Test
    fun `should handle get affirmation button with several affirmations`() {
        val update = createMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

        val affirmationText1 = "affirmationText1"
        affirmationRepository.save(
            Affirmation(
                text = affirmationText1,
                authorId = defaultUser.id,
                authorUsername = defaultUser.userName
            )
        )

        val affirmationText2 = "affirmationText2"
        affirmationRepository.save(
            Affirmation(
                text = affirmationText2,
                authorId = defaultUser.id,
                authorUsername = defaultUser.userName
            )
        )

        val affirmationText3 = "affirmationText3"
        affirmationRepository.save(
            Affirmation(
                text = affirmationText3,
                authorId = defaultUser.id,
                authorUsername = defaultUser.userName
            )
        )

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).containsAnyOf(
            "${KeyboardHelper.GET_AFFIRMATION_PREFIX} $affirmationText1",
            "${KeyboardHelper.GET_AFFIRMATION_PREFIX} $affirmationText2",
            "${KeyboardHelper.GET_AFFIRMATION_PREFIX} $affirmationText3"
        )
    }

    @Test
    fun `should deny admin commands when user is not admin`() {
        val adminAccessDeniedMessage = "Эта команда доступна только администраторам"
        val adminCommands = spyBot.commandHandler.getAdminCommands()

        for (command in adminCommands) {
            val commandText = "/${command.command}"
            if (commandText == CommandHandler.START_COMMAND) continue

            val update = createCommand(commandText)

            spyBot.onUpdateReceived(update)
            verifySetDefaultCommands()

            val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
            verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
            assertThat(sendMessageCaptor.value.text)
                .withFailMessage { "For command: $commandText \nActual text: ${sendMessageCaptor.value.text} \nExpected: $adminAccessDeniedMessage" }
                .isEqualTo(adminAccessDeniedMessage)
            clearInvocations(spyBot)
        }
    }

    @Test
    fun `should handle auth command without password`() {
        val update = createCommand(CommandHandler.AUTH_COMMAND)

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).contains("Для авторизации используйте команду в формате")
    }

    @Test
    fun `should handle auth command with wrong password`() {
        val update = createCommand("${CommandHandler.AUTH_COMMAND} wrong_password")

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).isEqualTo("Неверный пароль")
    }

    @Test
    fun `should handle auth command with right password`() {
        val update = createCommand("${CommandHandler.AUTH_COMMAND} $adminPassword")

        spyBot.onUpdateReceived(update)
        verifySetAdminCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).isEqualTo("Вы успешно авторизованы как администратор")
        assertTrue(adminRepository.existsByTelegramId(defaultUser.id))
    }

    private fun createMessage(text: String): Update {
        return Update().apply {
            message = Message().apply {
                this.text = text
                from = defaultUser
                chat = defaultChat
            }
        }
    }

    private fun createCommand(command: String): Update {
        return Update().apply {
            message = Message().apply {
                this.text = command
                entities = listOf(MessageEntity().apply {
                    this.type = "bot_command"
                    this.offset = 0
                })
                from = defaultUser
                chat = defaultChat
            }
        }
    }

    private fun verifySetDefaultCommands() {
        val setMyCommandsCaptor = ArgumentCaptor.forClass(SetMyCommands::class.java)
        verify(spyBot, times(1)).execute(setMyCommandsCaptor.capture())
        assertThat(setMyCommandsCaptor.value.commands).isEqualTo(spyBot.commandHandler.getDefaultCommands())
    }

    private fun verifySetAdminCommands() {
        val setMyCommandsCaptor = ArgumentCaptor.forClass(SetMyCommands::class.java)
        verify(spyBot, times(1)).execute(setMyCommandsCaptor.capture())
        assertThat(setMyCommandsCaptor.value.commands).isEqualTo(spyBot.commandHandler.getAdminCommands())
    }
}