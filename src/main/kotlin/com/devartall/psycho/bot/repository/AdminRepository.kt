package com.devartall.psycho.bot.repository

import com.devartall.psycho.bot.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminRepository : JpaRepository<Admin, Long> {
    fun existsByTelegramId(telegramId: Long): Boolean
    fun deleteByTelegramId(telegramId: Long)
} 