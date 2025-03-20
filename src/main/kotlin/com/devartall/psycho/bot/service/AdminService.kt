package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.config.BotConfig
import com.devartall.psycho.bot.entity.Admin
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User

@Service
class AdminService(
    private val botConfig: BotConfig,
    private val adminCache: AdminCache
) {
    fun isAdmin(telegramId: Long): Boolean {
        return adminCache.isAdmin(telegramId)
    }

    fun addAdmin(user: User): Admin {
        return adminCache.save(
            Admin(
                telegramId = user.id,
                username = user.userName,
                firstName = user.firstName,
                lastName = user.lastName
            )
        )
    }

    fun removeAdmin(telegramId: Long) {
        adminCache.delete(telegramId)
    }

    fun checkPassword(password: String): Boolean {
        return password == botConfig.adminPassword
    }

    fun clearCache() {
        adminCache.clear()
    }
} 