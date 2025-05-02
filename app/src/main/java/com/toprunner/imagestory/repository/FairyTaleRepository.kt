package com.toprunner.imagestory.repository

import android.content.Context
import android.util.Log
import com.toprunner.imagestory.data.dao.FairyTaleDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.util.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FairyTaleRepository(private val context: Context) {
    private val TAG = "FairyTaleRepository"
    private val fairyTaleDao: FairyTaleDao = AppDatabase.getInstance(context).fairyTaleDao()
    private val fileStorageManager = FileStorageManager()

    /**
     * 새로운 동화 저장
     */
    suspend fun saveFairyTale(
        title: String,
        voiceId: Long,
        imageId: Long,
        textId: Long,
        musicId: Long,
        theme: String,
        audioData: ByteArray,
        voiceFeatures: VoiceFeatures
    ): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving fairy tale: $title with voice ID: $voiceId")

            // 오디오 데이터 저장
            val audioPath = fileStorageManager.saveAudioFile(context, audioData)
            Log.d(TAG, "Audio file saved at: $audioPath")

            // 테마 정보와 오디오 경로를 attribute 필드에 저장
            val attributeJson = JSONObject().apply {
                put("theme", theme)
                put("audioPath", audioPath)
                put("averagePitch", voiceFeatures.averagePitch)
                put("pitchStdDev", voiceFeatures.pitchStdDev)

                // MFCC 값 저장
                val mfccJsonArray = JSONArray()
                for (mfccFrame in voiceFeatures.mfccValues) {
                    val frameArray = JSONArray()
                    for (value in mfccFrame) {
                        frameArray.put(value)
                    }
                    mfccJsonArray.put(frameArray)
                }
                put("mfccValues", mfccJsonArray)
            }.toString()

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
            val newId = fairyTaleDao.insertFairyTale(fairyTaleEntity)
            Log.d(TAG, "Fairy tale saved with ID: $newId")
            return@withContext newId

        } catch (e: Exception) {
            Log.e(TAG, "Error saving fairy tale: ${e.message}", e)
            throw e
        }
    }

    /**
     * 추천된 음성으로 동화를 복제합니다.
     * 기존 동화의 정보를 유지하되 제목과 음성만 변경합니다.
     */
    suspend fun createStoryWithRecommendedVoice(
        originalStoryId: Long,
        recommendedVoiceId: Long,
        newTitle: String,
        audioData: ByteArray
    ): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating new story with recommended voice. Original ID: $originalStoryId, Voice ID: $recommendedVoiceId")

            // 원본 동화 가져오기
            val originalStory = fairyTaleDao.getFairyTaleById(originalStoryId)
                ?: throw IllegalArgumentException("Original story not found with ID: $originalStoryId")

            // 원본 동화의 attribute JSON 파싱
            val originalAttrJson = JSONObject(originalStory.attribute)

            // 새 오디오 경로 저장
            val newAudioPath = fileStorageManager.saveAudioFile(
                context,
                audioData,
                "recommended_audio_${System.currentTimeMillis()}.wav"
            )
            Log.d(TAG, "New audio saved at: $newAudioPath")

            // 새로운 attribute JSON 생성 (기존 값 유지하고 audioPath만 변경)
            val newAttrJson = JSONObject(originalStory.attribute).apply {
                put("audioPath", newAudioPath)
                put("isRecommendedVoiceVersion", true)
                put("originalStoryId", originalStoryId)

                // 중요: 음성 ID 바뀌었음을 명확히 표시
                put("voiceIdChanged", true)
                put("previousVoiceId", originalStory.voice_id)
            }

            // 새 동화 엔티티 생성
            val newStory = FairyTaleEntity(
                title = newTitle,
                voice_id = recommendedVoiceId,  // 추천된 음성 ID로 변경
                image_id = originalStory.image_id,
                text_id = originalStory.text_id,
                music_id = originalStory.music_id,
                attribute = newAttrJson.toString(),
                created_at = System.currentTimeMillis()
            )

            // 데이터베이스에 삽입하고 ID 반환
            val newId = fairyTaleDao.insertFairyTale(newStory)
            Log.d(TAG, "Created new story with ID: $newId")
            return@withContext newId

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create story with recommended voice: ${e.message}", e)
            throw Exception("Failed to create story with recommended voice: ${e.message}", e)
        }
    }

    suspend fun insertFairyTale(fairyTale: FairyTaleEntity): Long {
        return fairyTaleDao.insertFairyTale(fairyTale)
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
        try {
            val fairyTale = fairyTaleDao.getFairyTaleById(fairyTaleId) ?: return@withContext false

            // attribute에서 오디오 파일 경로 추출
            try {
                val attributeJson = JSONObject(fairyTale.attribute)
                val audioPath = attributeJson.optString("audioPath", "")

                // 오디오 파일 삭제
                if (audioPath.isNotEmpty()) {
                    fileStorageManager.deleteFile(audioPath)
                }
            } catch (e: Exception) {
                // JSON 파싱 오류 처리
                Log.e(TAG, "Error parsing attribute JSON: ${e.message}")
            }

            // 데이터베이스에서 삭제
            val result = fairyTaleDao.deleteFairyTale(fairyTaleId)
            return@withContext result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fairy tale: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun updateFairyTale(
        fairyTaleEntity: FairyTaleEntity,
        newContent: String? = null,
        newAudioData: ByteArray? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            var updatedEntity = fairyTaleEntity

            // 새 오디오가 있으면 파일 업데이트
            if (newAudioData != null) {
                // 기존 오디오 파일 삭제 로직
                try {
                    val attributeJson = JSONObject(fairyTaleEntity.attribute)
                    val oldAudioPath = attributeJson.optString("audioPath", "")

                    if (oldAudioPath.isNotEmpty()) {
                        fileStorageManager.deleteFile(oldAudioPath)
                    }

                    // 새 오디오 파일 저장
                    val newAudioPath = fileStorageManager.saveAudioFile(context, newAudioData)

                    // attribute 업데이트
                    attributeJson.put("audioPath", newAudioPath)
                    updatedEntity = updatedEntity.copy(attribute = attributeJson.toString())
                } catch (e: Exception) {
                    // JSON 파싱 오류 처리
                    Log.e(TAG, "Error updating fairy tale: ${e.message}")
                    return@withContext false
                }
            }

            // 데이터베이스 업데이트
            val result = fairyTaleDao.updateFairyTale(updatedEntity)
            return@withContext result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fairy tale: ${e.message}", e)
            return@withContext false
        }
    }
}