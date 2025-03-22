package com.devartall.psycho.bot.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "music_tracks")
class MusicTrack(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val fileId: String,
    val authorId: Long,
    val authorUsername: String,
    val artistName: String,
    val trackTitle: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) 