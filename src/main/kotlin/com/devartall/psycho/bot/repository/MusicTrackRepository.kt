package com.devartall.psycho.bot.repository

import com.devartall.psycho.bot.entity.MusicTrack
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MusicTrackRepository : JpaRepository<MusicTrack, Long>