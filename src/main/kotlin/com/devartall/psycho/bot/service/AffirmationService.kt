package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.entity.Affirmation
import com.devartall.psycho.bot.repository.AffirmationRepository
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User

@Service
class AffirmationService(
    private val affirmationRepository: AffirmationRepository
) {
    fun addAffirmation(text: String, user: User): Affirmation {
        return affirmationRepository.save(
            Affirmation(
                text = text,
                authorId = user.id,
                authorUsername = user.userName
            )
        )
    }

    fun getAllAffirmations(): List<Affirmation> {
        return affirmationRepository.findAll()
            .sortedBy { it.createdAt }
    }

    fun getRandomAffirmation(): Affirmation? {
        val affirmations = affirmationRepository.findAll()
        return affirmations.randomOrNull()
    }

    fun deleteAllAffirmations(): Long {
        val count = affirmationRepository.count()
        affirmationRepository.deleteAll()
        return count
    }
} 