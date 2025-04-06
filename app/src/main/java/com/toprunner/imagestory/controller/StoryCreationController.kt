package com.toprunner.imagestory.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.service.GPTService
import com.toprunner.imagestory.service.TTSService
import com.toprunner.imagestory.util.VoiceFeaturesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StoryCreationController(private val context: Context) {
    private val TAG = "StoryCreationController"
    private val gptService = GPTService()
    private val ttsService = TTSService(context)
    private val fairyTaleRepository = FairyTaleRepository(context)
    private val imageRepository = ImageRepository(context)
    private val textRepository = TextRepository(context)
    private val voiceRepository = VoiceRepository(context)
    private val voiceFeaturesUtil = VoiceFeaturesUtil()

    suspend fun createStory(image: Bitmap, theme: String): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting story creation with theme: $theme")

            // 이미지 검증
            if (!validateImage(image)) {
                throw IllegalArgumentException("이미지가 동화 생성에 적합하지 않습니다.")
            }

            // GPT API를 통해 동화 생성
            Log.d(TAG, "Generating story content using GPT API")
            val gptResponse = gptService.generateStory(image, theme)
            Log.d(TAG, "GPT API returned response successfully")

            // 동화 내용 처리
            val processedResponse = processGPTResponse(gptResponse)
            val title = processedResponse.first
            val storyText = processedResponse.second

            // 음성 특성 추출
            val voiceFeatures = extractVoiceFeatures(gptResponse)

            // 동화에 적합한 음성 추천
            val voiceId = voiceRepository.recommendVoice(theme, voiceFeatures)
            Log.d(TAG, "Recommended voice ID: $voiceId")

            // 추천된 음성으로 오디오 생성
            Log.d(TAG, "Generating audio for story")
            val audioData = generateAudio(storyText, voiceId)
            Log.d(TAG, "Audio generated successfully, size: ${audioData.size} bytes")

            // 이미지 저장 및 ID 획득
            val imageId = imageRepository.saveImage(context, title, image)
            Log.d(TAG, "Image saved with ID: $imageId")

            // 텍스트 저장 및 ID 획득
            val textId = textRepository.saveText(context, storyText)
            Log.d(TAG, "Text saved with ID: $textId")

            // 동화 저장 및 ID 반환
            val fairyTaleId = fairyTaleRepository.saveFairyTale(
                title = title,
                voiceId = voiceId,
                imageId = imageId,
                textId = textId,
                musicId = 0, // 기본 음악 ID 또는 사용자가 나중에 선택
                theme = theme,
                audioData = audioData
            )

            Log.d(TAG, "Fairy tale saved with ID: $fairyTaleId")
            fairyTaleId
        } catch (e: Exception) {
            Log.e(TAG, "Error in story creation: ${e.message}", e)
            throw e
        }
    }

    private fun validateImage(bitmap: Bitmap): Boolean {
        // 이미지 크기 검증 (최소 크기 검사)
        if (bitmap.width < 100 || bitmap.height < 100) {
            return false
        }

        // 더 복잡한 이미지 검증 로직을 구현할 수 있음
        // 예: 이미지 선명도, 밝기 등 검사

        return true
    }

    private fun processGPTResponse(responseData: String): Pair<String, String> {
        try {
            Log.d(TAG, "Processing GPT response")
            // GPT API 응답에서 JSON 추출
            val jsonResponse = JSONObject(responseData)

            val title = jsonResponse.getString("title")
            val text = jsonResponse.getString("text")

            Log.d(TAG, "Extracted title: $title")
            Log.d(TAG, "Extracted text length: ${text.length}")

            return Pair(title, text)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing GPT response: ${e.message}", e)
            // JSON 파싱 오류 시 기본값 반환
            return Pair("자동 생성된 제목", responseData.trim())
        }
    }

    private fun extractVoiceFeatures(gptResponse: String): VoiceFeatures {
        try {
            val jsonResponse = JSONObject(gptResponse)

            // GPT 응답에서 음성 특성 추출
            val averagePitch = jsonResponse.optDouble("averagePitch", 120.0)
            val pitchStdDev = jsonResponse.optDouble("pitchStdDev", 15.0)

            // MFCC 값 추출
            val mfccValues = mutableListOf<DoubleArray>()

            if (jsonResponse.has("mfccValues")) {
                val mfccArray = jsonResponse.getJSONArray("mfccValues")

                for (i in 0 until mfccArray.length()) {
                    val coeffArray = mfccArray.getJSONArray(i)
                    val coeffs = DoubleArray(coeffArray.length())

                    for (j in 0 until coeffArray.length()) {
                        coeffs[j] = coeffArray.getDouble(j)
                    }

                    mfccValues.add(coeffs)
                }
            } else {
                // 기본 MFCC 값 (비어있는 값)
                mfccValues.add(DoubleArray(13) { 0.0 })
            }

            return VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting voice features: ${e.message}", e)
            // 파싱 오류 시 기본값 반환
            return VoiceFeatures(
                averagePitch = 120.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
        }
    }

    suspend fun generateAudio(storyText: String, voiceId: Long): ByteArray {
        Log.d(TAG, "Generating audio for text length: ${storyText.length} with voice ID: $voiceId")
        return ttsService.generateVoice(storyText, voiceId)
    }
}