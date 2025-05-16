package com.toprunner.imagestory.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.toprunner.imagestory.model.Story
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.service.GPTService
import com.toprunner.imagestory.service.TTSService
import com.toprunner.imagestory.util.ImageUtil
import com.toprunner.imagestory.util.VoiceFeaturesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import com.toprunner.imagestory.controller.StoryGenerationException
import com.toprunner.imagestory.controller.VoiceGenerationException
import org.json.JSONArray

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

            // 안전한 이미지 처리를 위한 코드 추가
            val optimizedImage = try {
                // 이미지 최적화
                val imageUtil = ImageUtil()
                imageUtil.compressImage(image)
            } catch (e: Exception) {
                Log.e(TAG, "Error optimizing image: ${e.message}", e)
                image // 원본 이미지를 폴백으로 사용
            }
            // 이미지 검증 - 확장된 검증
            if (!validateImage(optimizedImage)) {
                throw IllegalArgumentException("이미지가 동화 생성에 적합하지 않습니다.")
            }

            // 테마 매핑 (한글 -> 영어)
            val englishTheme = when (theme) {
                "판타지" -> "fantasy"
                "사랑" -> "love"
                "SF" -> "sf"
                "공포" -> "horror"
                "코미디" -> "comedy"
                "비극" -> "tragedy"
                else -> "fantasy" // 기본값
            }

            // GPT API를 통해 동화 생성
            Log.d(TAG, "Generating story content using GPT API with theme: $englishTheme")
            val storyData = try {
                val gptResponse = gptService.generateStory(image, englishTheme)
                parseStoryResponse(gptResponse)
            } catch (e: StoryGenerationException) {
                // API 키 에러나 서버 에러 등, GPTService에서 던진 것은 그대로 올려보내기
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "GPT 오류(기타): ${e.message}", e)
                throw StoryGenerationException("동화 생성 중 오류가 발생했습니다: ${e.message}")
            }
//            val gptResponse = gptService.generateStory(image, englishTheme)
            Log.d(TAG, "GPT API returned response successfully")

            // 동화 내용 처리
            //val storyData = parseStoryResponse(gptResponse)

            // 이후 storyData 사용
            val title = storyData.title
            val storyText = storyData.text
            val voiceFeatures = VoiceFeatures(
                averagePitch = storyData.averagePitch,
                pitchStdDev = storyData.pitchStdDev,
                mfccValues = storyData.mfccValues.map { it.toDoubleArray() } // 타입 맞추기
            )
            Log.d(TAG, "Processed story with title: $title and text length: ${storyText.length}")
            Log.d(TAG, "Extracted voice features: $voiceFeatures")

            // 동화에 적합한 음성 추천
            val voiceId = voiceRepository.recommendVoice(englishTheme, voiceFeatures)
            Log.d(TAG, "Recommended voice ID: $voiceId")

            // 추천된 음성으로 오디오 생성
            Log.d(TAG, "Generating audio for story")
            val audioData = try {
                generateAudio(storyData.text, voiceRepository.recommendVoice(englishTheme, voiceFeatures))
            } catch (e: Exception) {
                Log.e(TAG, "TTS 오류: ${e.message}", e)
                // 이 부분에서 “목소리 생성 중 오류”라는 메시지를 담아서 던집니다.
                throw VoiceGenerationException("목소리 생성 중 오류가 발생했습니다.")
            }
//            val audioData = generateAudio(storyText, voiceId)
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
                audioData = audioData,
                voiceFeatures = voiceFeatures
            )

            Log.d(TAG, "Fairy tale saved with ID: $fairyTaleId")
            fairyTaleId
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "메모리 부족 오류: ${e.message}", e)
            throw IllegalStateException("이미지 처리 중 메모리 부족 오류가 발생했습니다. 더 작은 이미지를 사용해보세요.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in story creation: ${e.message}", e)
            throw e
        }
    }

    private fun validateImage(bitmap: Bitmap): Boolean {
        // 이미지 크기 검증 (최소/최대 크기 검사)
        if (bitmap.width < 100 || bitmap.height < 100) {
            Log.e(TAG, "Image too small: ${bitmap.width}x${bitmap.height}")
            return false
        }

        if (bitmap.width > 4096 || bitmap.height > 4096) {
            Log.e(TAG, "Image too large: ${bitmap.width}x${bitmap.height}")
            return false
        }

        // 메모리 사용량 확인
        val byteCount = bitmap.allocationByteCount
        if (byteCount > 20 * 1024 * 1024) { // 20MB 이상
            Log.e(TAG, "Image requires too much memory: ${byteCount / (1024 * 1024)}MB")
            return false
        }

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
                Log.w(TAG, "TTS returned empty audio data, retrying with default voice")
                // 기본 음성으로 다시 시도 (-1L)
                return ttsService.generateVoice(storyText, -1L)
            }

            return audioData
        } catch (e: Exception) {
            Log.e(TAG, "Error generating audio: ${e.message}", e)
            // 기본 음성으로 다시 시도
            try {
                Log.d(TAG, "Retrying with default voice")
                return ttsService.generateVoice(storyText, -1L)
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Fallback to default voice also failed: ${fallbackError.message}")
                // 오류 발생 시 더미 오디오 데이터 반환
                return ByteArray(1000)
            }
        }
    }

    private fun parseStoryResponse(responseData: String): Story {
        try {
            val jsonResponse = JSONObject(responseData)
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                var content = message.optString("content", "")

                // JSON 코드블럭 제거
                if (content.contains("```json")) {
                    content = content.substringAfter("```json").substringBefore("```").trim()
                } else if (content.contains("```")) {
                    content = content.substringAfter("```").substringBefore("```").trim()
                }

                try {
                    // 일반 JSON 파싱 시도
                    val storyJson = JSONObject(content)
                    return Story(
                        title = storyJson.optString("title", "자동 생성된 동화"),
                        theme = storyJson.optString("theme", "fantasy"),
                        text = storyJson.optString("text", "동화 내용이 없습니다."),
                        averagePitch = storyJson.optDouble("averagePitch", 120.0),
                        pitchStdDev = storyJson.optDouble("pitchStdDev", 15.0),
                        mfccValues = try {
                            val mfccArray = storyJson.optJSONArray("mfccValues") ?: JSONArray()
                            (0 until mfccArray.length()).map { i ->
                                mfccArray.getJSONArray(i).let { innerArray ->
                                    List(innerArray.length()) { innerArray.getDouble(it) }
                                }
                            }
                        } catch (e: Exception) {
                            // 기본 MFCC 값 제공
                            listOf(List(13) { 0.0 })
                        }
                    )
                } catch (e: Exception) {
                    // JSON 파싱 실패 시 텍스트 내에서 정보 추출 시도
                    Log.e("GPTService", "JSON 파싱 실패, 텍스트에서 추출 시도: ${e.message}")

                    // 기본 값 설정
                    val title = extractTitle(content) ?: "자동 생성된 동화"
                    val text = content.replace("```", "").trim()

                    return Story(
                        title = title,
                        theme = "fantasy", // 기본 테마
                        text = text,
                        averagePitch = 120.0, // 기본값
                        pitchStdDev = 15.0,  // 기본값
                        mfccValues = listOf(List(13) { 0.0 }) // 기본 MFCC 값
                    )
                }
            } else {
                throw JSONException("No choices found in GPT response")
            }
        } catch (e: JSONException) {
            Log.e("GPTService", "Error parsing GPT response: ${e.message}", e)
            throw e
        }
    }

    // 텍스트에서 제목 추출 시도하는 헬퍼 함수
    private fun extractTitle(content: String): String? {
        // 제목 패턴 찾기 시도 (여러 가능한 포맷)
        val titlePatterns = listOf(
            "제목:\\s*(.+?)\\s*\\n",
            "title:\\s*\"?(.+?)\"?\\s*\\n",
            "# (.+?)\\s*\\n",
            "(.+?)\\s*\\n" // 첫 줄을 제목으로 간주
        )

        for (pattern in titlePatterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(content)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return null
    }

    // StoryCreationController.kt에 추가
    suspend fun createStoryWithFineTunedModel(theme: String): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting story creation with fine-tuned model, theme: $theme")

            // 테마 매핑 (한글 -> 영어)
            val englishTheme = when (theme) {
                "판타지" -> "fantasy"
                "사랑" -> "love"
                "SF" -> "sf"
                "공포" -> "horror"
                "코미디" -> "comedy"
                "비극" -> "tragedy"
                else -> "fantasy" // 기본값
            }

            val storyData = try {
                val gptResponse = gptService.generateStoryWithFineTunedModel(englishTheme)
                try {
                    parseStoryResponse(gptResponse)
                } catch (e: Exception) {
                    Log.e(TAG, "응답 파싱 오류, 기본 동화 사용: ${e.message}", e)
                    // 파싱 실패 시 기본 동화 객체 생성
                    Story(
                        title = "파인튜닝 모델 동화",
                        theme = englishTheme,
                        text = "옛날 옛적에 작은 마을에 착한 아이가 살았습니다. 그 아이는 항상 다른 사람들을 도와주려고 했고, 모두에게 친절했습니다. 어느 날, 아이는 숲속을 걷다가 길을 잃은 작은 새를 발견했습니다. 아이는 새를 집으로 데려와 정성껏 돌봐주었습니다. 며칠 후, 새의 날개가 나아 하늘로 날아갔습니다. 그리고 매년 봄이 되면, 그 새는 아이를 찾아와 감사의 노래를 불러주었답니다.",
                        averagePitch = 150.0,
                        pitchStdDev = 15.0,
                        mfccValues = listOf(List(13) { 0.0 })
                    )
                }
            } catch (e: StoryGenerationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "GPT 오류(기타): ${e.message}", e)
                throw StoryGenerationException("동화 생성 중 오류가 발생했습니다: ${e.message}")
            }

            // 동화 내용 처리
            val title = storyData.title
            val storyText = storyData.text
            val voiceFeatures = VoiceFeatures(
                averagePitch = storyData.averagePitch,
                pitchStdDev = storyData.pitchStdDev,
                mfccValues = storyData.mfccValues.map { it.toDoubleArray() }
            )

            Log.d(TAG, "Processed story with title: $title and text length: ${storyText.length}")
            Log.d(TAG, "Extracted voice features: $voiceFeatures")

            // 동화에 적합한 음성 추천
            val voiceId = voiceRepository.recommendVoice(englishTheme, voiceFeatures)
            Log.d(TAG, "Recommended voice ID: $voiceId")

            // 추천된 음성으로 오디오 생성
            Log.d(TAG, "Generating audio for story")
            val audioData = try {
                generateAudio(storyData.text, voiceId)
            } catch (e: Exception) {
                Log.e(TAG, "TTS 오류: ${e.message}", e)
                throw VoiceGenerationException("목소리 생성 중 오류가 발생했습니다.")
            }

            Log.d(TAG, "Audio generated successfully, size: ${audioData.size} bytes")

            // 기본 이미지 사용 (파인튜닝 모델에는 이미지가 없으므로 더미 이미지 생성)
            val dummyBitmap = createDummyImage()

            // 이미지 저장 및 ID 획득
            val imageId = imageRepository.saveImage(context, title, dummyBitmap)
            Log.d(TAG, "Default image saved with ID: $imageId")

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
                musicId = defaultMusicId,
                theme = englishTheme,
                audioData = audioData,
                voiceFeatures = voiceFeatures
            )

            Log.d(TAG, "Fairy tale saved with ID: $fairyTaleId")
            fairyTaleId
        } catch (e: Exception) {
            Log.e(TAG, "Error in fine-tuned story creation: ${e.message}", e)
            throw e
        }
    }

    // 더미 이미지 생성 함수 추가
    private fun createDummyImage(): Bitmap {
        val width = 512
        val height = 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // 배경색 설정
        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.rgb(255, 250, 240) // 연한 크림색
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 텍스트 설정
        paint.color = android.graphics.Color.rgb(100, 100, 100)
        paint.textSize = 40f
        paint.textAlign = android.graphics.Paint.Align.CENTER

        // 텍스트 그리기
        canvas.drawText("파인튜닝 모델로 생성된 동화", width / 2f, height / 2f - 20, paint)
        canvas.drawText("(이미지 없음)", width / 2f, height / 2f + 40, paint)

        return bitmap
    }
}