package com.toprunner.imagestory.data.dao

import androidx.room.*
import com.toprunner.imagestory.data.entity.FairyTaleEntity

@Dao
interface FairyTaleDao {
    @Insert
    suspend fun insertFairyTale(fairyTaleEntity: FairyTaleEntity): Long

    @Query("SELECT * FROM fairy_tales WHERE fairy_tales_id = :fairyTaleId")
    suspend fun getFairyTaleById(fairyTaleId: Long): FairyTaleEntity?

    @Query("SELECT * FROM fairy_tales ORDER BY created_at DESC")
    suspend fun getAllFairyTales(): List<FairyTaleEntity>

    @Query("DELETE FROM fairy_tales WHERE fairy_tales_id = :fairyTaleId")
    suspend fun deleteFairyTale(fairyTaleId: Long): Int

    @Update
    suspend fun updateFairyTale(fairyTaleEntity: FairyTaleEntity): Int
}