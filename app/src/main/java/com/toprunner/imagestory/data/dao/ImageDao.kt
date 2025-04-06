package com.toprunner.imagestory.data.dao

import androidx.room.*
import com.toprunner.imagestory.data.entity.ImageEntity

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(imageEntity: ImageEntity): Long

    @Query("SELECT * FROM images WHERE image_id = :imageId")
    suspend fun getImageById(imageId: Long): ImageEntity?

    @Query("SELECT * FROM images")
    suspend fun getAllImages(): List<ImageEntity>

    @Query("DELETE FROM images WHERE image_id = :imageId")
    suspend fun deleteImage(imageId: Long): Int

    @Update
    suspend fun updateImage(imageEntity: ImageEntity): Int
}