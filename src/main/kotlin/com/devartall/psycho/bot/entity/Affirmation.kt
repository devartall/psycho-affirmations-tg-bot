package com.devartall.psycho.bot.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "affirmations")
data class Affirmation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val text: String,

    val authorId: Long,

    val authorUsername: String?,

    val createdAt: LocalDateTime = LocalDateTime.now()
) 