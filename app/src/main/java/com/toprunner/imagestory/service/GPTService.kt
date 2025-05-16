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
    // gpt api 요청용
    suspend fun generateStory(image: Bitmap, theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "다음의 테마로 동화를 생성 중: $theme")
            Log.d(TAG, "원본 이미지 크기: ${image.width}x${image.height}")
            val base64Image = encodeImageToBase64(image)
            Log.d(TAG, "Base64 encoded image length: ${base64Image.length}")
            val requestBody = createRequestBody(base64Image, theme)
            Log.d(TAG, "Sending request to GPT API. URL: $API_URL")
            // 실제 API 호출 (실제 API URL과 헤더, API 키를 사용)
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
//            validateResponse(response) // 응답 검증 개선
//            response
            when (statusCode) {
                401, 403           -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599        -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299       -> throw StoryGenerationException("알 수 없는 오류가 발생했습니다 (code=$statusCode).")
            }
            return@withContext response
        } catch (e: StoryGenerationException) {
            throw e      // 위에서 던진 건 그대로
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story: ${e.message}", e)
            throw StoryGenerationException("동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }
    private fun validateResponse(response: String): Boolean {
        try {
            val jsonResponse = JSONObject(response)
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() == 0) {
                Log.w(TAG, "Empty choices array in response")
                return false
            }
            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.optString("content", "")

            // JSON 형식 확인
            JSONObject(content.replace("```json", "").replace("```", "").trim())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Invalid response format: ${e.message}")
            return false
        }
    }
    suspend fun chatWithBot(userMessage: String, previousMessages: List<String> = emptyList(),storycontent:String ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Chatting with bot: $userMessage")

            // 메시지 주고받기 위한 시스템과 사용자 메시지 배열 구성
            val messages = JSONArray().apply {
                // 시스템 메시지 추가
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", """
                            당신은 어린이 대상의 동화 전문가입니다.\n +
                                아래 원문 동화를 참고해서, 사용자의 질문에 친절하고 상세하게 답해주세요. +
                                --- 동화 전문 시작 ---
                                $storycontent
                                --- 동화 전문 끝 ---
                                """.trimIndent())
                    }
                )
                // 이전 대화(질문/답변) 전체를 user/assistant 역할로 차례로 추가
                previousMessages.chunked(2).forEach { pair ->
                    // pair[0] = "나: xxx", pair[1] = "동화 챗봇: yyy" 라 가정
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", pair[0].substringAfter("나: ").trim())
                    })
                    put(JSONObject().apply {
                        put("role", "assistant")
                        put("content", pair[1].substringAfter("동화 챗봇: ").trim())
                    })
                }
                // 사용자 메시지 추가
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
                put("max_tokens", 500)  // 응답 길이를 제한
                put("temperature", 0.7)                  // 창의성과 일관성 절충
            }

            // 실제 API 호출
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

            // GPT API에서 받은 응답 추출
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
    // 동화 리스트 저장용
    suspend fun generateFairyTaleEntity(image: Bitmap, theme: String): FairyTaleEntity {
        val jsonString = generateStory(image, theme)
        val storyJson = JSONObject(jsonString)

        return FairyTaleEntity(
            fairy_tales_id = 0,  // DB에서 자동 생성(autoGenerate 설정)
            title = storyJson.getString("title"),
            voice_id = 0,  // 별도 저장 후 id 획득 필요
            image_id = 0,  // 별도 저장 후 id 획득 필요
            text_id = 0,   // 별도 저장 후 id 획득 필요
            music_id = 0,  // 별도 저장 후 id 획득 필요
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
        // 이미지 크기와 질 조정으로 API 요청 크기 제한 대응
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

        // 동화 생성에 필요한 상세 프롬프트 작성 (JSON 출력 형식을 명시) - 검토 필요
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

        // messages 배열 구성: 첫 번째로 시스템 메시지, 두 번째로 사용자 메시지
        val messages = JSONArray().apply {
            // 시스템 메시지 추가
            put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", "당신은 창의적인 천재적인 작가입니다. 제공된 이미지와 테마를 기반으로 동화를 생성하세요. 출력은 다음 키를 포함하는 유효한 JSON 객체여야 합니다: title, theme, text, averagePitch, pitchStdDev, mfccValues. 추가 설명을 포함하지 마세요.")
                }
            )
            // 사용자 메시지 추가
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
    suspend fun generateStoryWithFineTunedModel(theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "파인튜닝 모델로 동화를 생성 중: $theme")

            // 테마 매핑 - 기존 함수와 동일
            val themePrompt = when (theme) {
                "fantasy" -> "한국어로 판타지 장르의 동화를 만들어주세요."
                "love" -> "한국어로 사랑에 관한 동화를 만들어주세요."
                "sf" -> "한국어로 공상과학(SF) 장르의 동화를 만들어주세요."
                "horror" -> "한국어로 무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
                "comedy" -> "한국어로 유머러스하고 재미있는 동화를 만들어주세요."
                "tragedy" -> "한국어로 슬프지만 교훈이 있는 비극적인 동화를 만들어주세요."
                else -> "한국어로 어린이를 위한 동화를 만들어주세요."
            }

            // 동화 생성에 필요한 상세 프롬프트 작성 (JSON 출력 형식을 명시)
            val detailedPrompt = """
        $themePrompt

        아래의 요구사항에 따라 동화를 생성해주세요:
        1. 결과는 반드시 JSON 형식의 객체여야 합니다.
        2. JSON 객체는 다음 키들을 포함해야 합니다: "title" (문자열), "theme" (문자열, (fantasy,love,sf,horror,comedy 중 하나)), "text" (문자열), "averagePitch" (실수), "pitchStdDev" (실수), "mfccValues" (숫자 배열들의 배열, 각 내부 배열은 13개의 숫자를 포함).
        3. 추가적인 설명이나 부가 텍스트 없이 오직 JSON 객체만 출력해야 합니다.
        4. 모든 숫자 값에는 표준 ASCII 문자만 사용하세요. 특히 마이너스 기호는 일반 하이픈 '-'를 사용하세요.
        5. 동화 텍스트는 300 단어에서 500 단어 사이의 분량으로 작성해주세요.
        """.trimIndent()

            // messages 배열 구성
            val messages = JSONArray().apply {
                // 시스템 메시지 추가
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", "당신은 창의적인 천재적인 작가입니다. 제공된 테마를 기반으로 동화를 생성하세요. 출력은 다음 키를 포함하는 유효한 JSON 객체여야 합니다: title, theme, text, averagePitch, pitchStdDev, mfccValues. 추가 설명을 포함하지 마세요.")
                    }
                )
                // 사용자 메시지 추가
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", detailedPrompt)
                    }
                )
            }

            // 파인튜닝 모델 사용
            val requestObj = JSONObject().apply {
                put("model", "ft:gpt-3.5-turbo-1106:personal::BVGbWaMn") // 파인튜닝 모델 ID 사용
                put("messages", messages)
                put("max_tokens", 2000)
            }

            // API 호출
            val (response, statusCode) = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestObj.toString()
            )

            // 결과 검증 및 반환
            when (statusCode) {
                401, 403 -> throw StoryGenerationException("API 키가 잘못되었습니다.")
                in 500..599 -> throw StoryGenerationException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                !in 200..299 -> throw StoryGenerationException("알 수 없는 오류가 발생했습니다 (code=$statusCode).")
            }

            return@withContext response
        } catch (e: StoryGenerationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story with fine-tuned model: ${e.message}", e)
            throw StoryGenerationException("파인튜닝 모델로 동화 생성 중 오류가 발생했습니다: ${e.message}")
        }
    }
}