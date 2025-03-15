package com.devartall.psycho.bot.entity

import jakarta.persistence.*

@Entity
@Table(name = "admins")
data class Admin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val telegramId: Long,
    
    val username: String?,
    
    val firstName: String,
    
    val lastName: String?
) 