package com.toprunner.imagestory.repository

import android.content.Context
import com.toprunner.imagestory.data.dao.FairyTaleDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.util.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FairyTaleRepository(private val context: Context) {
    private val fairyTaleDao: FairyTaleDao = AppDatabase.getInstance(context).fairyTaleDao()
    private val fileStorageManager = FileStorageManager()

    suspend fun saveFairyTale(
        title: String,
        voiceId: Long,
        imageId: Long,
        textId: Long,
        musicId: Long,
        theme: String,
        audioData: ByteArray
    ): Long = withContext(Dispatchers.IO) {
        // 음성 파일 저장 - 이미 VoiceRepository에서 저장된 음성을 사용할 것이지만
        // 동화별로 다른 오디오 파일이 필요할 수 있으므로 추가 저장
        val audioPath = fileStorageManager.saveAudioFile(context, audioData)

        // 테마 정보를 attribute 필드에 저장
        val attributeJson = "{\"theme\":\"$theme\", \"audioPath\":\"$audioPath\"}"

        // 데이터베이스에 저장
        val fairyTaleEntity = FairyTaleEntity(
            title = title,
            voice_id = voiceId,
            image_id = imageId,
            text_id = textId,
            music_id = musicId,
            attribute = attributeJson,
            created_at = System.currentTimeMillis()
        )

        // 데이터베이스에 삽입하고 ID 반환
        fairyTaleDao.insertFairyTale(fairyTaleEntity)
    }

    suspend fun getFairyTaleById(fairyTaleId: Long): Pair<FairyTaleEntity, String> = withContext(Dispatchers.IO) {
        val fairyTale = fairyTaleDao.getFairyTaleById(fairyTaleId)
            ?: throw IllegalArgumentException("동화를 찾을 수 없습니다: ID $fairyTaleId")

        // TextRepository를 통해 실제 텍스트 파일의 내용을 가져옴
        val textRepository = TextRepository(context)
        val textEntityPair = textRepository.getTextById(fairyTale.text_id)
        val textContent = textEntityPair?.second ?: "동화 내용이 없습니다."

        Pair(fairyTale, textContent)
    }

    suspend fun getAllFairyTales(): List<FairyTaleEntity> = withContext(Dispatchers.IO) {
        fairyTaleDao.getAllFairyTales()
    }

    suspend fun deleteFairyTale(fairyTaleId: Long): Boolean = withContext(Dispatchers.IO) {
        val fairyTale = fairyTaleDao.getFairyTaleById(fairyTaleId) ?: return@withContext false

        // attribute에서 오디오 파일 경로 추출
        try {
            val attributeJson = org.json.JSONObject(fairyTale.attribute)
            val audioPath = attributeJson.optString("audioPath", "")

            // 오디오 파일 삭제
            if (audioPath.isNotEmpty()) {
                fileStorageManager.deleteFile(audioPath)
            }
        } catch (e: Exception) {
            // JSON 파싱 오류 처리
        }

        // 데이터베이스에서 삭제
        val result = fairyTaleDao.deleteFairyTale(fairyTaleId)
        result > 0
    }

    suspend fun updateFairyTale(
        fairyTaleEntity: FairyTaleEntity,
        newContent: String? = null,
        newAudioData: ByteArray? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var updatedEntity = fairyTaleEntity

        // 새 오디오가 있으면 파일 업데이트
        if (newAudioData != null) {
            // 기존 오디오 파일 삭제 로직
            try {
                val attributeJson = org.json.JSONObject(fairyTaleEntity.attribute)
                val oldAudioPath = attributeJson.optString("audioPath", "")

                if (oldAudioPath.isNotEmpty()) {
                    fileStorageManager.deleteFile(oldAudioPath)
                }

                // 새 오디오 파일 저장
                val newAudioPath = fileStorageManager.saveAudioFile(context, newAudioData)

                // attribute 업데이트
                val theme = attributeJson.optString("theme", "")
                val newAttributeJson = "{\"theme\":\"$theme\", \"audioPath\":\"$newAudioPath\"}"
                updatedEntity = updatedEntity.copy(attribute = newAttributeJson)
            } catch (e: Exception) {
                // JSON 파싱 오류 처리
            }
        }

        // 데이터베이스 업데이트
        val result = fairyTaleDao.updateFairyTale(updatedEntity)
        result > 0
    }
}