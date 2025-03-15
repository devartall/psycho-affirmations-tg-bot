package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.entity.Admin
import com.devartall.psycho.bot.repository.AdminRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AdminCache(
    private val adminRepository: AdminRepository,
) {
    private val adminStatusCache = ConcurrentHashMap<Long, Boolean>()

    fun isAdmin(userId: Long): Boolean {
        return adminStatusCache.computeIfAbsent(userId) { id ->
            adminRepository.existsByTelegramId(id)
        }
    }

    fun delete(telegramId: Long) {
        adminStatusCache[telegramId] = false
        adminRepository.deleteByTelegramId(telegramId)
    }

    fun save(admin: Admin): Admin {
        val savedAdmin = adminRepository.save(admin)
        adminStatusCache[savedAdmin.telegramId] = true
        return savedAdmin
    }
} 