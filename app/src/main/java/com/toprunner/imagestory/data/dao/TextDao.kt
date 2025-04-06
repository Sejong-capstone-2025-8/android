package com.toprunner.imagestory.data.dao

import androidx.room.*
import com.toprunner.imagestory.data.entity.TextEntity

@Dao
interface TextDao {
    @Insert
    suspend fun insertText(textEntity: TextEntity): Long

    @Query("SELECT * FROM texts WHERE text_id = :textId")
    suspend fun getTextById(textId: Long): TextEntity?

    @Query("SELECT * FROM texts")
    suspend fun getAllTexts(): List<TextEntity>

    @Query("DELETE FROM texts WHERE text_id = :textId")
    suspend fun deleteText(textId: Long): Int

    @Update
    suspend fun updateText(textEntity: TextEntity): Int
}