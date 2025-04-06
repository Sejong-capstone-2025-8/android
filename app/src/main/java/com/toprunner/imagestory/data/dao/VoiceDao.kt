package com.toprunner.imagestory.data.dao

import androidx.room.*
import com.toprunner.imagestory.data.entity.VoiceEntity

@Dao
interface VoiceDao {
    @Insert
    suspend fun insertVoice(voiceEntity: VoiceEntity): Long

    @Query("SELECT * FROM voices WHERE voice_id = :voiceId")
    suspend fun getVoiceById(voiceId: Long): VoiceEntity?

    @Query("SELECT * FROM voices")
    suspend fun getAllVoices(): List<VoiceEntity>

    @Query("DELETE FROM voices WHERE voice_id = :voiceId")
    suspend fun deleteVoice(voiceId: Long): Int

    @Update
    suspend fun updateVoice(voiceEntity: VoiceEntity): Int
}