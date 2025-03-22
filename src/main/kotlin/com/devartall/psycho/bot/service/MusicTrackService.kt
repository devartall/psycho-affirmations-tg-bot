package com.devartall.psycho.bot.service

import com.devartall.psycho.bot.entity.MusicTrack
import com.devartall.psycho.bot.repository.MusicTrackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class MusicTrackService(private val musicTrackRepository: MusicTrackRepository) {

    enum class DeleteResult {
        WRONG_ID_FORMAT,
        TRACK_NOT_FOUND,
        TRACK_DELETED
    }

    @Transactional
    fun addMusicTrack(fileId: String, authorId: Long, authorUsername: String, artistName: String, trackTitle: String) {
        val musicTrack = MusicTrack(
            fileId = fileId,
            authorId = authorId,
            authorUsername = authorUsername,
            artistName = artistName,
            trackTitle = trackTitle
        )
        musicTrackRepository.save(musicTrack)
    }

    @Transactional
    fun deleteMusicTrack(id: String): DeleteResult {
        val idLong = id.toLongOrNull() ?: return DeleteResult.WRONG_ID_FORMAT

        if (!musicTrackRepository.existsById(idLong)) {
            return DeleteResult.TRACK_NOT_FOUND
        }

        musicTrackRepository.deleteById(idLong)
        return DeleteResult.TRACK_DELETED
    }

    fun getRandomMusicTrack(): MusicTrack? {
        val allTracks = musicTrackRepository.findAll()
        return if (allTracks.isNotEmpty()) {
            allTracks[Random.nextInt(allTracks.size)]
        } else {
            null
        }
    }

    fun getAllMusicTracks(): List<MusicTrack> {
        return musicTrackRepository.findAll()
    }
} 