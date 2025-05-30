package com.toprunner.imagestory.service

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.toprunner.imagestory.BuildConfig
import com.toprunner.imagestory.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import androidx.core.graphics.scale
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.controller.StoryGenerationException
import com.toprunner.imagestory.controller.VoiceGenerationException

class GPTService {
    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
        private val API_KEY = BuildConfig.GPT_API_KEY
        private const val TAG = "GPTService"
    }
    private val networkUtil = NetworkUtil()

    // 기존 generateStory 메서드 유지
    suspend fun generateStory(image: Bitmap, theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "다음의 테마로 동화를 생성 중: $theme")
            Log.d(TAG, "원본 이미지 크기: ${image.width}x${image.height}")
            val base64Image = encodeImageToBase64(image)
            Log.d(TAG, "Base64 encoded image length: ${base64Image.length}")
            val requestBody = createRequestBody(base64Image, theme)
            Log.d(TAG, "Sending request to GPT API. URL: $API_URL")

            val (response, statusCode) = networkUtil.sendHttpRequest(
                url     = API_URL,
                method  = "POST",
                headers = mapOf(
                    "Content-Type"  to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body    = requestBody
            )
            Log.d(TAG, "GPT API raw response (code=$statusCode): $response")

            when (statusCode) {
                401, 403           -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599        -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299       -> throw StoryGenerationException("알 수 없는 오류가 발생했습니다 (code=$statusCode).")
            }
            return@withContext response
        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story: ${e.message}", e)
            throw StoryGenerationException("동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    // 새로운 메서드: 이미지 분석 후 파인튜닝 모델로 동화 생성
    suspend fun generateStoryWithImageAnalysis(image: Bitmap, theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "이미지 분석 후 파인튜닝 모델로 동화 생성 시작: $theme")

            // 1단계: GPT-4o로 이미지 분석
            val imageDescription = analyzeImageWithGPT4o(image)
            Log.d(TAG, "이미지 분석 완료: $imageDescription")

            // 2단계: 분석된 이미지 정보와 테마를 파인튜닝 모델에 전달
            val storyResponse = generateStoryWithFineTunedModelFromDescription(imageDescription, theme)
            Log.d(TAG, "파인튜닝 모델로 동화 생성 완료")

            return@withContext storyResponse

        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in image analysis and fine-tuned story generation: ${e.message}", e)
            throw StoryGenerationException("이미지 분석 및 동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    // 이미지를 GPT-4o로 분석하는 메서드
    private suspend fun analyzeImageWithGPT4o(image: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "GPT-4o로 이미지 분석 시작")
            val base64Image = encodeImageToBase64(image)

            val messages = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", """
                            당신은 이미지를 분석하여 동화 생성에 필요한 정보를 추출하는 전문가입니다.
                            이미지를 자세히 분석하고 다음 요소들을 포함한 상세한 설명을 제공해주세요:
                            1. 주요 인물이나 캐릭터 (외모, 나이, 성별, 표정, 복장 등)
                            2. 배경과 환경 (장소, 시간대, 날씨, 분위기 등)
                            3. 물체나 소품들 (중요한 아이템, 도구, 장식 등)
                            4. 색상과 분위기 (전체적인 톤, 감정적 느낌)
                            5. 행동이나 상황 (무엇이 일어나고 있는지)
                            6. 스토리 요소 (이야기로 발전시킬 수 있는 흥미로운 점들)
                            
                            응답은 한국어로 작성하고, 동화 생성 AI가 이해하기 쉽도록 구체적이고 생생하게 작성해주세요.
                        """.trimIndent())
                    }
                )
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "이 이미지를 자세히 분석하여 한국어 동화 생성에 필요한 모든 정보를 추출해주세요.")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                    put("detail", "high")
                                })
                            })
                        })
                    }
                )
            }

            val requestObj = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", messages)
                put("max_tokens", 1000)
                put("temperature", 0.7)
            }

            val (response, statusCode) = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestObj.toString()
            )

            when (statusCode) {
                401, 403 -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599 -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299 -> throw StoryGenerationException("이미지 분석 중 오류가 발생했습니다 (code=$statusCode).")
            }

            val responseJson = JSONObject(response)
            val imageDescription = responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            Log.d(TAG, "이미지 분석 결과: $imageDescription")
            return@withContext imageDescription

        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image with GPT-4o: ${e.message}", e)
            throw StoryGenerationException("이미지 분석 중 오류가 발생했습니다: ${e.message}")
        }
    }

    // 이미지 설명과 테마를 기반으로 파인튜닝 모델로 동화 생성
    private suspend fun generateStoryWithFineTunedModelFromDescription(
        imageDescription: String,
        theme: String
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "파인튜닝 모델로 동화 생성 중 - 테마: $theme")

            val themePrompt = when (theme) {
                "fantasy" -> "판타지 장르의 동화를 만들어주세요."
                "love" -> "사랑에 관한 동화를 만들어주세요."
                "sf" -> "공상과학(SF) 장르의 동화를 만들어주세요."
                "horror" -> "무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
                "comedy" -> "유머러스하고 재미있는 동화를 만들어주세요."
                "tragedy" -> "슬프지만 교훈이 있는 비극적인 동화를 만들어주세요."
                else -> "어린이를 위한 동화를 만들어주세요."
            }

            val detailedPrompt = """
                다음 이미지 분석 정보를 바탕으로 한국어로 ${themePrompt}
                
                [이미지 분석 정보]
                $imageDescription
                
                [요구사항]
                1. 결과는 반드시 JSON 형식이어야 합니다.
                2. JSON 객체는 다음 키들을 포함해야 합니다: 
                   - "title" (문자열): 동화 제목
                   - "theme" (문자열): $theme
                   - "text" (문자열): 동화 본문 (반드시 한국어로 작성)
                   - "averagePitch" (실수): 120.0 ~ 200.0 사이의 값
                   - "pitchStdDev" (실수): 10.0 ~ 30.0 사이의 값
                   - "mfccValues" (숫자 배열들의 배열): 정확히 5개의 배열, 각각 13개의 숫자
                3. 동화 텍스트는 한국어로 300-500 단어 분량으로 작성해주세요.
                4. 이미지의 특징을 살려 생생하고 흥미로운 이야기로 만들어주세요.
                5. JSON 형식을 정확히 지켜주세요. 배열이 완전히 닫혀야 합니다.
                6. 추가적인 설명이나 부가 텍스트 없이 오직 JSON 객체만 출력해주세요.
                
                예시 형식:
                {
                    "title": "동화 제목",
                    "theme": "$theme",
                    "text": "한국어로 된 동화 내용...",
                    "averagePitch": 150.0,
                    "pitchStdDev": 15.0,
                    "mfccValues": [
                        [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0],
                        [1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1, 10.1, 11.1, 12.1, 13.1],
                        [1.2, 2.2, 3.2, 4.2, 5.2, 6.2, 7.2, 8.2, 9.2, 10.2, 11.2, 12.2, 13.2],
                        [1.3, 2.3, 3.3, 4.3, 5.3, 6.3, 7.3, 8.3, 9.3, 10.3, 11.3, 12.3, 13.3],
                        [1.4, 2.4, 3.4, 4.4, 5.4, 6.4, 7.4, 8.4, 9.4, 10.4, 11.4, 12.4, 13.4]
                    ]
                }
            """.trimIndent()

            val messages = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", "당신은 한국어 동화 작가입니다. 제공된 이미지 분석 정보와 테마를 기반으로 한국어 동화를 생성하세요. 출력은 다음 키를 포함하는 유효한 JSON 객체여야 합니다: title, theme, text, averagePitch, pitchStdDev, mfccValues. 반드시 한국어로 작성하고, JSON 형식을 정확히 지켜주세요.")
                    }
                )
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", detailedPrompt)
                    }
                )
            }

            val requestObj = JSONObject().apply {
                put("model", "ft:gpt-3.5-turbo-1106:personal::BVGbWaMn") // 파인튜닝 모델 ID
                put("messages", messages)
                put("max_tokens", 1000)
                put("temperature", 0.8)
            }

            val (response, statusCode) = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestObj.toString()
            )

            when (statusCode) {
                401, 403 -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599 -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299 -> throw StoryGenerationException("파인튜닝 모델 동화 생성 중 오류가 발생했습니다 (code=$statusCode).")
            }

            return@withContext response

        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story with fine-tuned model from description: ${e.message}", e)
            throw StoryGenerationException("파인튜닝 모델로 동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    // GPTService.kt에서 generateStoryWithFineTunedModel 메서드 수정
    suspend fun generateStoryWithFineTunedModel(theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "파인튜닝 모델로 동화를 생성 중: $theme")

            val themePrompt = when (theme) {
                "fantasy" -> "판타지 장르의 동화"
                "love" -> "사랑에 관한 동화"
                "sf" -> "공상과학(SF) 장르의 동화"
                "horror" -> "무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화"
                "comedy" -> "유머러스하고 재미있는 동화"
                "tragedy" -> "슬프지만 교훈이 있는 비극적인 동화"
                else -> "어린이를 위한 동화"
            }

            val detailedPrompt = """
${themePrompt}를 한국어로 만들어주세요.

다음 형식의 JSON으로 응답해주세요:
{
    "title": "동화 제목",
    "theme": "$theme",
    "text": "한국어로 작성된 동화 본문 (300-500 단어)",
    "averagePitch": 150.0,
    "pitchStdDev": 15.0,
    "mfccValues": [
        [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0],
        [1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1, 10.1, 11.1, 12.1, 13.1],
        [1.2, 2.2, 3.2, 4.2, 5.2, 6.2, 7.2, 8.2, 9.2, 10.2, 11.2, 12.2, 13.2],
        [1.3, 2.3, 3.3, 4.3, 5.3, 6.3, 7.3, 8.3, 9.3, 10.3, 11.3, 12.3, 13.3],
        [1.4, 2.4, 3.4, 4.4, 5.4, 6.4, 7.4, 8.4, 9.4, 10.4, 11.4, 12.4, 13.4]
    ]
}

중요한 규칙:
1. 오직 JSON 형식으로만 응답하세요.
2. 추가 설명이나 텍스트를 포함하지 마세요.
3. 동화 본문은 반드시 한국어로 작성하세요.
4. JSON의 모든 따옴표와 브레이스를 정확히 닫아주세요.
        """.trimIndent()

            val messages = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", "당신은 한국어 동화 작가입니다. 요청받은 테마의 동화를 JSON 형식으로 생성하세요. 반드시 유효한 JSON 형식으로만 응답하고, 추가 텍스트는 포함하지 마세요.")
                    }
                )
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", detailedPrompt)
                    }
                )
            }

            val requestObj = JSONObject().apply {
                put("model", "ft:gpt-3.5-turbo-1106:personal::BVGbWaMn") // 파인튜닝 모델 ID 사용
                put("messages", messages)
                put("max_tokens", 2000)
                put("temperature", 0.8)
            }

            val (response, statusCode) = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestObj.toString()
            )

            when (statusCode) {
                401, 403 -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599 -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299 -> throw StoryGenerationException("알 수 없는 오류가 발생했습니다 (code=$statusCode).")
            }

            Log.d(TAG, "Fine-tuned model raw response: $response")
            return@withContext response
        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story with fine-tuned model: ${e.message}", e)
            throw StoryGenerationException("파인튜닝 모델로 동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }

    // 나머지 기존 메서드들 유지...
    suspend fun chatWithBot(userMessage: String, previousMessages: List<String> = emptyList(),storycontent:String ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Chatting with bot: $userMessage")

            val messages = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", """
                        당신은 어린이 대상의 동화 전문가입니다.
                        아래 원문 동화를 참고해서, 사용자의 질문에 친절하고 상세하게 답해주세요.
                        응답은 2-3문장으로 간결하게 해주세요.
                        --- 동화 전문 시작 ---
                        $storycontent
                        --- 동화 전문 끝 ---
                        """.trimIndent())
                    }
                )

                previousMessages.chunked(2).forEach { pair ->
                    if (pair.isNotEmpty()) {
                        val userMsg = pair[0].let { msg ->
                            when {
                                msg.startsWith("나: ") -> msg.substringAfter("나: ")
                                msg.startsWith("사용자: ") -> msg.substringAfter("사용자: ")
                                else -> msg
                            }.trim()
                        }

                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userMsg)
                        })
                    }

                    if (pair.size > 1) {
                        val assistantMsg = pair[1].let { msg ->
                            when {
                                msg.startsWith("동화 챗봇: ") -> msg.substringAfter("동화 챗봇: ")
                                msg.startsWith("챗봇: ") -> msg.substringAfter("챗봇: ")
                                msg.startsWith("AI: ") -> msg.substringAfter("AI: ")
                                else -> msg
                            }.trim()
                        }

                        put(JSONObject().apply {
                            put("role", "assistant")
                            put("content", assistantMsg)
                        })
                    }
                }

                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    }
                )
            }

            val requestObj = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", messages)
                put("max_tokens", 200)
                put("temperature", 0.7)
            }

            val response = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestObj.toString()
            )

            Log.d(TAG, "Chatbot API raw response: $response")

            val (responseText, statusCode) = response
            val responseJson = JSONObject(responseText)
            val message = responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            message

        } catch (e: Exception) {
            Log.e(TAG, "Error chatting with bot: ${e.message}", e)
            "챗봇과의 대화 중 오류가 발생했습니다. 다시 시도해 주세요."
        }
    }

    suspend fun generateFairyTaleEntity(image: Bitmap, theme: String): FairyTaleEntity {
        val jsonString = generateStory(image, theme)
        val storyJson = JSONObject(jsonString)

        return FairyTaleEntity(
            fairy_tales_id = 0,
            title = storyJson.getString("title"),
            voice_id = 0,
            image_id = 0,
            text_id = 0,
            music_id = 0,
            attribute = JSONObject().apply {
                put("theme", storyJson.getString("theme"))
                put("text", storyJson.getString("text"))
                put("averagePitch", storyJson.getDouble("averagePitch"))
                put("pitchStdDev", storyJson.getDouble("pitchStdDev"))
                put("mfccValues", storyJson.getJSONArray("mfccValues"))
            }.toString(),
            created_at = System.currentTimeMillis()
        )
    }

    fun encodeImageToBase64(bitmap: Bitmap, quality: Int = 90, maxDimension: Int = 1536): String {
        val baos = ByteArrayOutputStream()
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scaleRatio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            val scaledWidth = (bitmap.width * scaleRatio).toInt()
            val scaledHeight = (bitmap.height * scaleRatio).toInt()
            bitmap.scale(scaledWidth, scaledHeight)
        } else {
            bitmap
        }

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun createRequestBody(base64Image: String, theme: String): String {
        val themePrompt = when (theme) {
            "fantasy" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 판타지 장르의 동화를 만들어주세요."
            "love" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 사랑에 관한 동화를 만들어주세요."
            "sf" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 공상과학(SF) 장르의 동화를 만들어주세요."
            "horror" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
            "comedy" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 유머러스하고 재미있는 동화를 만들어주세요."
            "tragedy" -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 슬프지만 교훈이 있는 비극적인 동화를 만들어주세요."
            else -> "이미지를 정확하게 분석하고 이미지 정보를 바탕으로 한국어로 어린이를 위한 동화를 만들어주세요."
        }

        val detailedPrompt = """
        $themePrompt

        아래의 요구사항에 따라 동화를 생성해주세요:
        1. 결과는 반드시 JSON 형식의 객체여야 합니다.
        2. JSON 객체는 다음 키들을 포함해야 합니다: "title" (문자열), "theme" (문자열, (fantasy,love,sf,horror,comedy 중 하나)), "text" (문자열), "averagePitch" (실수), "pitchStdDev" (실수), "mfccValues" (숫자 배열들의 배열, 각 내부 배열은 13개의 숫자를 포함).
        3. 추가적인 설명이나 부가 텍스트 없이 오직 JSON 객체만 출력해야 합니다.
        4. 모든 숫자 값에는 표준 ASCII 문자만 사용하세요. 특히 마이너스 기호는 일반 하이픈 '-'를 사용하세요.
        5. 동화 텍스트는 300 단어에서 500 단어 사이의 분량으로 작성해주세요.

        이미지 데이터: data:image/jpeg;base64,$base64Image
    """.trimIndent()

        val messages = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", "당신은 창의적인 천재적인 작가입니다. 제공된 이미지와 테마를 기반으로 동화를 생성하세요. 출력은 다음 키를 포함하는 유효한 JSON 객체여야 합니다: title, theme, text, averagePitch, pitchStdDev, mfccValues. 추가 설명을 포함하지 마세요.")
                }
            )
            put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", detailedPrompt)
                }
            )
        }

        val requestObj = JSONObject().apply {
            put("model", "o4-mini")
            put("messages", messages)
            put("max_completion_tokens", 8000)
        }

        return requestObj.toString()
    }

    private fun generateErrorResponse(errorMessage: String): String {
        return """
        {
            "title": "동화 생성 실패",
            "theme": "공포"
            "text": "동화를 생성하는 과정에서 오류가 발생했습니다: $errorMessage\n\n대신 기본 동화를 제공합니다.\n\n옛날 옛적에 작은 마을에 착한 아이가 살았습니다. 어느 날 숲속에서 길을 잃은 작은 새를 발견했습니다. 아이는 새를 집으로 데려와 정성껏 돌봐주었습니다. 며칠 후 새의 날개가 나아 하늘로 날아갔습니다. 그리고 매년 봄이 되면 그 새는 아이를 찾아와 감사의 노래를 불러주었답니다.",
            "averagePitch": 120.0,
            "pitchStdDev": 15.0,
            "mfccValues": [[0,0,0,0,0,0,0,0,0,0,0,0,0]]
        }
        """.trimIndent()
    }
}