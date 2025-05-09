package com.toprunner.imagestory.repository

import android.content.Context
import android.util.Log
import com.toprunner.imagestory.SimpleAudioAnalyzer
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

    suspend fun getVoiceFeatures(voiceId: Long): VoiceFeatures = withContext(Dispatchers.IO) {
        try {
            // 1. 음성 엔티티 조회
            val voice = voiceDao.getVoiceById(voiceId) ?: return@withContext getDefaultVoiceFeatures()

            // 2. 속성 JSON 파싱
            val attributeJson = JSONObject(voice.attribute)

            // 3. 음성 특성 파일 경로 또는 직접 저장된 특성값 확인
            if (attributeJson.has("voiceFeaturesPath")) {
                // 3-1. 특성 파일 경로가 있는 경우
                val featuresPath = attributeJson.getString("voiceFeaturesPath")
                return@withContext fileStorageManager.readVoiceFeatures(featuresPath) ?: getDefaultVoiceFeatures()
            } else if (attributeJson.has("averagePitch") && attributeJson.has("pitchStdDev")) {
                // 3-2. 속성 JSON에 직접 저장된 경우
                val averagePitch = attributeJson.getDouble("averagePitch")
                val pitchStdDev = attributeJson.getDouble("pitchStdDev")

                // MFCC 값이 있는지 확인
                val mfccValues = if (attributeJson.has("mfccValues")) {
                    val mfccArray = attributeJson.getJSONArray("mfccValues")
                    val result = mutableListOf<DoubleArray>()

                    for (i in 0 until mfccArray.length()) {
                        val coeffArray = mfccArray.getJSONArray(i)
                        val coeffs = DoubleArray(coeffArray.length())
                        for (j in 0 until coeffArray.length()) {
                            coeffs[j] = coeffArray.getDouble(j)
                        }
                        result.add(coeffs)
                    }

                    result
                } else {
                    // 기본 MFCC 값
                    listOf(DoubleArray(13) { 0.0 })
                }

                return@withContext VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
            } else {
                // 3-3. 낭독용 음성인 경우 원본 음성의 특성 가져오기 시도
                if (attributeJson.optBoolean("isClone", false) && attributeJson.has("originalVoicePath")) {
                    // 원본 음성 파일 경로가 있으면 SimpleAudioAnalyzer로 분석
                    val originalPath = attributeJson.getString("originalVoicePath")
                    val simpleAnalyzer = SimpleAudioAnalyzer(context)
                    return@withContext simpleAnalyzer.analyzeAudio(originalPath)
                } else if (attributeJson.has("elevenlabsVoiceId")) {
                    // ElevenLabs ID가 있으면 타입별 기본값 제공
                    val voiceId = attributeJson.getString("elevenlabsVoiceId")
                    return@withContext getVoiceFeaturesForElevenlabsId(voiceId)
                }
            }

            // 4. 특성 정보를 찾을 수 없는 경우 음성 파일 자체 분석
            val simpleAnalyzer = SimpleAudioAnalyzer(context)
            return@withContext simpleAnalyzer.analyzeAudio(voice.voice_path)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting voice features: ${e.message}", e)
            return@withContext getDefaultVoiceFeatures()
        }
    }

    // ElevenLabs 음성 ID별 특성 기본값 (각 음성별 특성을 다르게 부여)
    private fun getVoiceFeaturesForElevenlabsId(voiceId: String): VoiceFeatures {
        return when (voiceId) {
            "21m00Tcm4TlvDq8ikWAM" -> { // Rachel
                VoiceFeatures(
                    averagePitch = 165.0,
                    pitchStdDev = 15.0,
                    mfccValues = listOf(DoubleArray(13) { if (it % 2 == 0) 1.0 else -1.0 })
                )
            }
            "AZnzlk1XvdvUeBnXmlld" -> { // Domi
                VoiceFeatures(
                    averagePitch = 170.0,
                    pitchStdDev = 18.0,
                    mfccValues = listOf(DoubleArray(13) { if (it % 2 == 0) 0.8 else -0.8 })
                )
            }
            "EXAVITQu4vr4xnSDxMaL" -> { // Bella
                VoiceFeatures(
                    averagePitch = 155.0,
                    pitchStdDev = 14.0,
                    mfccValues = listOf(DoubleArray(13) { if (it % 2 == 0) 0.7 else -0.7 })
                )
            }
            "ErXwobaYiN019PkySvjV" -> { // Antoni
                VoiceFeatures(
                    averagePitch = 110.0,
                    pitchStdDev = 12.0,
                    mfccValues = listOf(DoubleArray(13) { if (it % 2 == 0) 0.5 else -0.5 })
                )
            }
            "MF3mGyEYCl7XYWbV9V6O" -> { // Elli
                VoiceFeatures(
                    averagePitch = 160.0,
                    pitchStdDev = 16.0,
                    mfccValues = listOf(DoubleArray(13) { if (it % 2 == 0) 0.9 else -0.9 })
                )
            }
            else -> {
                VoiceFeatures(
                    averagePitch = 140.0 + (Math.random() * 30 - 15), // 125-155 범위의 랜덤값
                    pitchStdDev = 15.0 + (Math.random() * 10 - 5),    // 10-20 범위의 랜덤값
                    mfccValues = listOf(DoubleArray(13) { (Math.random() * 2 - 1) }) // -1에서 1 사이 랜덤값
                )
            }
        }
    }

    // 기본 VoiceFeatures 객체 반환
    private fun getDefaultVoiceFeatures(): VoiceFeatures {
        return VoiceFeatures(
            averagePitch = 140.0 + (Math.random() * 20 - 10), // 130-150 범위의 랜덤값
            pitchStdDev = 15.0 + (Math.random() * 4 - 2),    // 13-17 범위의 랜덤값
            mfccValues = listOf(DoubleArray(13) { (Math.random() * 2 - 1) }) // -1에서 1 사이 랜덤값
        )
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

            // 1. 원하는 ElevenLabs 기본 목소리 ID 지정
            // 예: "21m00Tcm4TlvDq8ikWAM"는 Rachel 목소리
            val defaultElevenLabsVoiceId = "21m00Tcm4TlvDq8ikWAM" // Rachel

            // 2. 이 ElevenLabs 목소리 ID에 해당하는 로컬 Voice 엔티티를 찾거나 생성
            var defaultVoiceId: Long = 0

            // 3. 기존 데이터베이스에서 해당 ElevenLabs ID를 가진 Voice 찾기
            val allVoices = getAllVoices()
            for (voice in allVoices) {
                try {
                    val attributeJson = JSONObject(voice.attribute)
                    val elevenLabsId = attributeJson.optString("elevenlabsVoiceId", "")
                    if (elevenLabsId == defaultElevenLabsVoiceId) {
                        defaultVoiceId = voice.voice_id
                        Log.d(TAG, "Found existing voice with ElevenLabs ID: $defaultVoiceId")
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // 4. 없으면 해당 ElevenLabs 목소리를 가진 Voice 엔티티 생성 (옵션)
            if (defaultVoiceId == 0L && allVoices.isNotEmpty()) {
                // 없으면 첫 번째 음성 사용
                defaultVoiceId = allVoices.first().voice_id
                Log.d(TAG, "Using first available voice: $defaultVoiceId")
            }

            return@withContext defaultVoiceId
        } catch (e: Exception) {
            Log.e(TAG, "Error recommending voice: ${e.message}", e)
            // 오류 발생 시 기본값 반환
            return@withContext 0L
        }
    }
    suspend fun updateVoiceTitle(voiceId: Long, newTitle: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating voice title for ID: $voiceId, new title: $newTitle")

            // 기존 음성 정보 가져오기
            val voice = voiceDao.getVoiceById(voiceId) ?: return@withContext false

            // 새 제목으로 엔티티 복사
            val updatedVoice = voice.copy(title = newTitle)

            // 데이터베이스 업데이트
            val result = voiceDao.updateVoice(updatedVoice)
            val success = result > 0

            if (success) {
                Log.d(TAG, "Voice title updated successfully")
            } else {
                Log.d(TAG, "Voice title update failed")
            }

            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating voice title: ${e.message}", e)
            return@withContext false
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