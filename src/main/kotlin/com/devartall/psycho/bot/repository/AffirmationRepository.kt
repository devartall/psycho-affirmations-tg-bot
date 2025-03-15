package com.devartall.psycho.bot.repository

import com.devartall.psycho.bot.entity.Affirmation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AffirmationRepository : JpaRepository<Affirmation, Long>