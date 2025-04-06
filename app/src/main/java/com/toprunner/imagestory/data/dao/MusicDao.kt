package com.toprunner.imagestory.data.dao

import androidx.room.*
import com.toprunner.imagestory.data.entity.MusicEntity

@Dao
interface MusicDao {
    @Insert
    suspend fun insertMusic(musicEntity: MusicEntity): Long

    @Query("SELECT * FROM musics WHERE music_id = :musicId")
    suspend fun getMusicById(musicId: Long): MusicEntity?

    @Query("SELECT * FROM musics")
    suspend fun getAllMusic(): List<MusicEntity>

    @Query("SELECT * FROM musics WHERE attribute = :genre")
    suspend fun getMusicByGenre(genre: String): List<MusicEntity>

    @Query("DELETE FROM musics WHERE music_id = :musicId")
    suspend fun deleteMusic(musicId: Long): Int

    @Update
    suspend fun updateMusic(musicEntity: MusicEntity): Int
}