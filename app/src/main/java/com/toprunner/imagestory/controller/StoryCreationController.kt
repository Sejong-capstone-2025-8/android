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

            // 테마 매핑 (한글 -> 영어)
            val englishTheme = when (theme) {
                "판타지" -> "fantasy"
                "사랑" -> "love"
                "SF" -> "sf"
                "공포" -> "horror"
                "코미디" -> "comedy"
                else -> "fantasy" // 기본값
            }

            // GPT API를 통해 동화 생성
            Log.d(TAG, "Generating story content using GPT API with theme: $englishTheme")
            val gptResponse = gptService.generateStory(image, englishTheme)
            Log.d(TAG, "GPT API returned response successfully")

            Log.d("GPT RESPONSE", "gpt response: $gptResponse")

            // 동화 내용 처리
            val storyData = processGPTResponse(gptResponse)
            val title = storyData.first
            val storyText = storyData.second

            Log.d(TAG, "Processed story with title: $title and text length: ${storyText.length}")

            // 음성 특성 추출
            val voiceFeatures = extractVoiceFeatures(gptResponse)
            Log.d(TAG, "Extracted voice features")

            // 동화에 적합한 음성 추천
            val voiceId = voiceRepository.recommendVoice(englishTheme, voiceFeatures)
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

            // 임시 기본값 음악 ID (실제 구현에서는 적절한 음악 선택 필요)
            val defaultMusicId = 1L

            // 동화 저장 및 ID 반환
            val fairyTaleId = fairyTaleRepository.saveFairyTale(
                title = title,
                voiceId = voiceId,
                imageId = imageId,
                textId = textId,
                musicId = defaultMusicId, // 기본 음악 ID
                theme = englishTheme,
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

        // 더 복잡한 이미지 검증 로직 추가 가능
        return true
    }

    private fun processGPTResponse(responseData: String): Pair<String, String> {
        try {
            Log.d(TAG, "Processing GPT response")
            // 전체 JSON 파싱 (예: API 응답 전체)
            val jsonResponse = JSONObject(responseData)
            // choices 배열 내의 첫 번째 객체 추출
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.optString("content", "")
                // 여기서 content 안에 제목과 본문이 포함된 경우,
                // 예를 들어, "**동화: 제목**\n\n본문" 형식이라면 이를 분리할 수 있습니다.
                // 아래는 예시: 제목이 "**동화: "로 시작하고 "**"로 끝나는 경우.
                if (content.startsWith("**동화:") && content.contains("**")) {
                    // 제목과 본문 분리
                    val splitContent = content.split("**")
                    if (splitContent.size >= 3) {
                        val title = splitContent[1].trim().removePrefix("동화:").trim()
                        // 나머지 부분을 본문으로 간주 (줄바꿈 기준 분리 가능)
                        val text = splitContent.subList(2, splitContent.size).joinToString(" ").trim()
                        return Pair(title, text)
                    }
                }
                // 특별한 포맷이 없다면 기본 타이틀과 함께 전체 content를 본문으로 사용
                return Pair("자동 생성된 동화", content)
            } else {
                Log.d(TAG, "No choices found in GPT response")
                return Pair("자동 생성된 동화", "동화 내용이 없습니다.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing GPT response: ${e.message}", e)
            return Pair("자동 생성된 동화", "동화를 생성하는 중 오류가 발생했습니다.")
        }
    }


    private fun extractVoiceFeatures(gptResponse: String): VoiceFeatures {
        try {
            // GPT 응답에서 JSON 추출
            val jsonStartIndex = gptResponse.indexOf("{")
            val jsonEndIndex = gptResponse.lastIndexOf("}")

            if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
                val jsonText = gptResponse.substring(jsonStartIndex, jsonEndIndex + 1)
                val jsonResponse = JSONObject(jsonText)

                // 음성 특성 추출
                val averagePitch = jsonResponse.optDouble("averagePitch", 120.0)
                val pitchStdDev = jsonResponse.optDouble("pitchStdDev", 15.0)

                // MFCC 값 추출
                val mfccValues = mutableListOf<DoubleArray>()

                if (jsonResponse.has("mfccValues")) {
                    try {
                        val mfccArray = jsonResponse.getJSONArray("mfccValues")
                        for (i in 0 until mfccArray.length()) {
                            val coeffArray = mfccArray.getJSONArray(i)
                            val coeffs = DoubleArray(coeffArray.length())
                            for (j in 0 until coeffArray.length()) {
                                coeffs[j] = coeffArray.getDouble(j)
                            }
                            mfccValues.add(coeffs)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing MFCC values: ${e.message}", e)
                        // 오류 시 기본 MFCC 값 사용
                        mfccValues.add(DoubleArray(13) { 0.0 })
                    }
                } else {
                    // 기본 MFCC 값
                    mfccValues.add(DoubleArray(13) { 0.0 })
                }

                return VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
            } else {
                // JSON 형식이 아닌 경우 기본값 반환
                return VoiceFeatures(
                    averagePitch = 120.0,
                    pitchStdDev = 15.0,
                    mfccValues = listOf(DoubleArray(13) { 0.0 })
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting voice features: ${e.message}", e)
            // 오류 시 기본값 반환
            return VoiceFeatures(
                averagePitch = 120.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            )
        }
    }

    suspend fun generateAudio(storyText: String, voiceId: Long): ByteArray {
        Log.d(TAG, "Generating audio for text length: ${storyText.length} with voice ID: $voiceId")

        // 오류 핸들링 개선
        try {
            val audioData = ttsService.generateVoice(storyText, voiceId)

            // 빈 오디오 데이터 처리
            if (audioData.isEmpty()) {
                Log.w(TAG, "TTS returned empty audio data, using dummy audio")
                return ByteArray(1000) // 임시 더미 데이터
            }

            return audioData
        } catch (e: Exception) {
            Log.e(TAG, "Error generating audio: ${e.message}", e)
            // 오류 발생 시 더미 오디오 데이터 반환
            return ByteArray(1000)
        }
    }
}