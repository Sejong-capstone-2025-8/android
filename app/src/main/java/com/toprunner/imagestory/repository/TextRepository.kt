package com.toprunner.imagestory.repository

import android.content.Context
import android.util.Log
import com.toprunner.imagestory.data.dao.TextDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.TextEntity
import com.toprunner.imagestory.util.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TextRepository(private val context: Context) {
    private val textDao: TextDao = AppDatabase.getInstance(context).textDao()
    private val fileStorageManager = FileStorageManager()

    suspend fun saveText(
        context: Context,
        content: String
    ): Long = withContext(Dispatchers.IO) {
        // 텍스트 파일 저장
        val textPath = fileStorageManager.saveTextFile(context, content)

        // 데이터베이스에 저장
        val textEntity = TextEntity(
            text_path = textPath,
            created_at = System.currentTimeMillis()
        )

        // 데이터베이스에 삽입하고 ID 반환
        textDao.insertText(textEntity)
    }

    suspend fun getTextById(textId: Long): Pair<TextEntity, String>? = withContext(Dispatchers.IO) {
        val text = textDao.getTextById(textId) ?: return@withContext null

        // 텍스트 파일 읽기
        val content = fileStorageManager.readTextFile(text.text_path)
        Log.d("TextRepository", "Read text from file: ${text.text_path} \nContent: $content")
        Pair(text, content)
    }

    suspend fun getAllTexts(): List<TextEntity> = withContext(Dispatchers.IO) {
        textDao.getAllTexts()
    }

    suspend fun deleteText(textId: Long): Boolean = withContext(Dispatchers.IO) {
        val text = textDao.getTextById(textId) ?: return@withContext false

        // 텍스트 파일 삭제
        fileStorageManager.deleteFile(text.text_path)

        // 데이터베이스에서 삭제
        val result = textDao.deleteText(textId)
        result > 0
    }
}