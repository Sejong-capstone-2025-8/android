package com.toprunner.imagestory.repository

import android.content.Context
import com.toprunner.imagestory.data.dao.MusicDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.MusicEntity
import com.toprunner.imagestory.util.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {
    private val musicDao: MusicDao = AppDatabase.getInstance(context).musicDao()
    private val fileStorageManager = FileStorageManager()

    suspend fun saveMusic(
        title: String,
        genre: String,
        musicData: ByteArray
    ): Long = withContext(Dispatchers.IO) {
        // 음악 파일 저장
        val musicPath = fileStorageManager.saveAudioFile(
            context,
            musicData,
            "music_${System.currentTimeMillis()}.mp3"
        )

        // 음악 정보 저장
        val duration = calculateDuration(musicData) // 실제 구현에서는 음악 파일의 길이를 계산

        // 데이터베이스에 저장
        val musicEntity = MusicEntity(
            title = title,
            music_path = musicPath,
            attribute = genre, // 장르를 attribute 필드에 저장
            created_at = System.currentTimeMillis()
        )

        // 데이터베이스에 삽입하고 ID 반환
        musicDao.insertMusic(musicEntity)
    }

    private fun calculateDuration(musicData: ByteArray): Long {
        // 실제 구현에서는 음악 파일의 메타데이터를 분석하여 길이를 계산
        // 현재는 임시 구현
        return 0L
    }

    suspend fun getMusicById(musicId: Long): MusicEntity? = withContext(Dispatchers.IO) {
        musicDao.getMusicById(musicId)
    }

    suspend fun getAllMusic(): List<MusicEntity> = withContext(Dispatchers.IO) {
        musicDao.getAllMusic()
    }

    suspend fun getMusicByGenre(genre: String): List<MusicEntity> = withContext(Dispatchers.IO) {
        musicDao.getMusicByGenre(genre)
    }

    suspend fun deleteMusic(musicId: Long): Boolean = withContext(Dispatchers.IO) {
        val music = musicDao.getMusicById(musicId) ?: return@withContext false

        // 음악 파일 삭제
        fileStorageManager.deleteFile(music.music_path)

        // 데이터베이스에서 삭제
        val result = musicDao.deleteMusic(musicId)
        result > 0
    }
}