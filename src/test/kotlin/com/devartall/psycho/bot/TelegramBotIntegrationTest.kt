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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User

class TelegramBotIntegrationTest(@Autowired bot: TelegramBot) : AbstractIntegrationTest() {

    private val spyBot: TelegramBot = spy(bot).also {
        doAnswer { null }.`when`(it).execute(any(SetMyCommands::class.java))
        doAnswer { null }.`when`(it).execute(any(SendMessage::class.java))
    }

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Autowired
    private lateinit var affirmationRepository: AffirmationRepository

    private val defaultUser: User = User().apply {
        id = 100L
        userName = "test_user_name"
    }
    private val defaultChat: Chat = Chat().apply { id = 10000L }

    @BeforeEach
    fun setUp() {
        adminRepository.deleteAll()
        affirmationRepository.deleteAll()
    }

    @Test
    fun `should handle general message and respond with start command`() {
        val update = createUpdateMessage("Hello, bot!")

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).contains(CommandHandler.START_COMMAND)
    }

    @Test
    fun `should handle get affirmation button without affirmations`() {
        val update = createUpdateMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

        spyBot.onUpdateReceived(update)
        verifySetDefaultCommands()

        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(spyBot, times(1)).execute(sendMessageCaptor.capture())
        assertThat(sendMessageCaptor.value.text).isEqualTo("К сожалению, сейчас нет доступных аффирмаций")
    }

    @Test
    fun `should handle get affirmation button with one affirmation`() {
        val update = createUpdateMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

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
        val update = createUpdateMessage(KeyboardHelper.GET_AFFIRMATION_BUTTON)

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

    private fun createUpdateMessage(text: String): Update {
        return Update().apply {
            message = Message().apply {
                this.text = text
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