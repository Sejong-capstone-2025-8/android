package com.toprunner.imagestory.repository

import android.content.Context
import android.util.Log
import com.toprunner.imagestory.util.FileStorageManager
import com.toprunner.imagestory.data.dao.VoiceDao
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.util.VoiceFeaturesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class VoiceRepository(private val context: Context) {
    private val TAG = "VoiceRepository"
    private val voiceDao: VoiceDao = AppDatabase.getInstance(context).voiceDao()
    private val fileStorageManager = FileStorageManager()
    private val voiceFeaturesUtil = VoiceFeaturesUtil()

    suspend fun saveVoice(
        title: String,
        attributeJson: String,
        audioData: ByteArray,
        voiceFeatures: VoiceFeatures
    ): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving voice: $title")

            // 제목 검증
            require(title.isNotBlank()) { "음성 제목은 비어 있을 수 없습니다." }

            // 음성 파일 저장
            val voicePath = fileStorageManager.saveAudioFile(context, audioData)
            Log.d(TAG, "Voice audio saved at: $voicePath")

            // 음성 특성 파일 저장
            val voiceFeaturesPath = fileStorageManager.saveVoiceFeatures(context, voiceFeatures)
            Log.d(TAG, "Voice features saved at: $voiceFeaturesPath")

            // 데이터베이스에 저장
            val voiceEntity = VoiceEntity(
                title = title,
                voice_path = voicePath,
                attribute = attributeJson,
                created_at = System.currentTimeMillis()
            )

            // 데이터베이스에 삽입하고 ID 반환
            val voiceId = voiceDao.insertVoice(voiceEntity)
            Log.d(TAG, "Voice saved with ID: $voiceId")

            voiceId
        } catch (e: Exception) {
            Log.e(TAG, "Error saving voice: ${e.message}", e)
            throw e
        }
    }

    suspend fun getVoiceById(voiceId: Long): VoiceEntity? = withContext(Dispatchers.IO) {
        try {
            voiceDao.getVoiceById(voiceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting voice by ID $voiceId: ${e.message}", e)
            null
        }
    }

    suspend fun getVoiceFeatures(voiceId: Long): VoiceFeatures? = withContext(Dispatchers.IO) {
        try {
            val voice = voiceDao.getVoiceById(voiceId) ?: return@withContext null

            // JSON 속성에서 음성 특성 파일 경로 또는 직접 특성 추출
            try {
                val attributeJson = JSONObject(voice.attribute)
                val voiceType = attributeJson.optString("voiceType", "")

                // 기본 음성 모델의 경우 하드코딩된 특성 반환
                if (voiceType.isNotEmpty() && voiceType != "custom") {
                    return@withContext getDefaultVoiceFeatures(voiceType)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing attribute JSON: ${e.message}", e)
            }

            // 저장된 특성 파일 읽기 시도
            // 실제 구현시 특성 파일 경로를 attribute JSON에 포함시키는 것이 좋음
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting voice features for voice ID $voiceId: ${e.message}", e)
            null
        }
    }

    suspend fun getAllVoices(): List<VoiceEntity> = withContext(Dispatchers.IO) {
        try {
            voiceDao.getAllVoices()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all voices: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun deleteVoice(voiceId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val voice = voiceDao.getVoiceById(voiceId) ?: return@withContext false

            // 음성 파일 삭제
            fileStorageManager.deleteFile(voice.voice_path)

            // 데이터베이스에서 삭제
            val result = voiceDao.deleteVoice(voiceId)
            result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting voice with ID $voiceId: ${e.message}", e)
            false
        }
    }

    suspend fun recommendVoice(theme: String, targetFeatures: VoiceFeatures): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Recommending voice for theme: $theme")

            // 모든 음성 가져오기
            val allVoices = voiceDao.getAllVoices()

            // 음성이 없으면 0 반환
            if (allVoices.isEmpty()) {
                Log.d(TAG, "No voices available for recommendation")
                return@withContext 0L
            }

            // 테마 가중치 (어떤 테마에는 어떤 목소리가 더 어울림)
            val themeWeights = mapOf(
                "판타지" to mapOf("rachel" to 1.2, "elli" to 1.5, "bella" to 1.3, "antoni" to 0.8, "domi" to 1.0),
                "사랑" to mapOf("rachel" to 1.4, "elli" to 1.2, "bella" to 1.5, "antoni" to 0.7, "domi" to 1.0),
                "SF" to mapOf("rachel" to 0.8, "elli" to 1.0, "bella" to 0.9, "antoni" to 1.5, "domi" to 1.3),
                "공포" to mapOf("rachel" to 0.7, "elli" to 0.8, "bella" to 0.9, "antoni" to 1.4, "domi" to 1.5),
                "코미디" to mapOf("rachel" to 1.2, "elli" to 1.0, "bella" to 1.3, "antoni" to 1.4, "domi" to 1.5)
            )

            // 각 목소리별 점수 계산
            val voiceScores = mutableMapOf<Long, Double>()

            for (voice in allVoices) {
                try {
                    // 음성 유형 추출
                    val attributeJson = JSONObject(voice.attribute)
                    val voiceType = attributeJson.optString("voiceType", "custom")

                    // 테마 가중치 적용
                    val themeWeight = themeWeights[theme]?.get(voiceType) ?: 1.0

                    // 기본 점수 (나중에 음성 특성 유사도 등을 활용해 향상 가능)
                    var score = themeWeight

                    // 사용자 정의 음성인 경우 특성 유사도 고려
                    if (voiceType == "custom") {
                        // 기본 가중치보다 낮게 설정 (기본 음성 모델이 더 최적화되어 있다고 가정)
                        score = 0.8
                    }

                    voiceScores[voice.voice_id] = score

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing voice ${voice.voice_id}: ${e.message}")
                    // 오류 발생 시 기본 점수 적용
                    voiceScores[voice.voice_id] = 0.5
                }
            }

            // 최고 점수 음성 찾기
            val bestVoiceId = voiceScores.maxByOrNull { it.value }?.key ?: allVoices.first().voice_id

            Log.d(TAG, "Recommended voice ID: $bestVoiceId")
            bestVoiceId

        } catch (e: Exception) {
            Log.e(TAG, "Error recommending voice: ${e.message}", e)
            // 오류 발생 시 기본값 반환 (가능하면 첫 번째 음성, 없으면 0)
            val defaultVoice = try {
                voiceDao.getAllVoices().firstOrNull()?.voice_id
            } catch (e2: Exception) {
                null
            }
            defaultVoice ?: 0L
        }
    }

    private fun getDefaultVoiceFeatures(voiceType: String): VoiceFeatures {
        return when (voiceType) {
            "rachel" -> VoiceFeatures(
                averagePitch = 165.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
            "antoni" -> VoiceFeatures(
                averagePitch = 110.0,
                pitchStdDev = 12.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
            "domi" -> VoiceFeatures(
                averagePitch = 170.0,
                pitchStdDev = 18.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
            "bella" -> VoiceFeatures(
                averagePitch = 155.0,
                pitchStdDev = 14.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
            "elli" -> VoiceFeatures(
                averagePitch = 160.0,
                pitchStdDev = 16.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
            else -> VoiceFeatures(
                averagePitch = 140.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
        }
    }
}