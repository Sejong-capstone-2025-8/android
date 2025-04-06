package com.toprunner.imagestory.service

import com.toprunner.imagestory.BuildConfig
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.toprunner.imagestory.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GPTService {
    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
        private const val API_KEY = BuildConfig.GPT_API_KEY
        private const val TAG = "GPTService"
    }

    private val networkUtil = NetworkUtil()

    suspend fun generateStory(image: Bitmap, theme: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating story with theme: $theme")

            // 이미지를 Base64로 인코딩
            val base64Image = encodeImageToBase64(image)

            // 요청 본문 생성
            val requestBody = createRequestBody(base64Image, theme)

            // API 요청 헤더
            val headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $API_KEY"
            )

            // API 요청 전송
            Log.d(TAG, "Sending request to GPT API")
            val response = networkUtil.sendHttpRequest(
                API_URL,
                "POST",
                headers,
                requestBody
            )
            Log.d(TAG, "Received response from GPT API")

            // 응답 파싱
            parseResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story: ${e.message}", e)
            throw IllegalStateException("동화 생성에 실패했습니다: ${e.message}")
        }
    }

    fun encodeImageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        // 이미지 크기와 질 조정으로 API 요청 크기 제한 대응
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scaleRatio = 1024f / maxOf(bitmap.width, bitmap.height)
            val scaledWidth = (bitmap.width * scaleRatio).toInt()
            val scaledHeight = (bitmap.height * scaleRatio).toInt()
            Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        } else {
            bitmap
        }

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun createRequestBody(base64Image: String, theme: String): String {
        val themePrompt = when (theme) {
            "판타지" -> "이 이미지를 바탕으로 판타지 장르의 동화를 만들어주세요. 마법, 모험, 상상의 세계를 포함해주세요."
            "사랑" -> "이 이미지를 바탕으로 사랑에 관한 동화를 만들어주세요. 따뜻한 감정과 인간 관계를 중심으로 해주세요."
            "SF" -> "이 이미지를 바탕으로 공상과학(SF) 장르의 동화를 만들어주세요. 미래, 기술, 우주 등의 요소를 포함해주세요."
            "공포" -> "이 이미지를 바탕으로 무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
            "코미디" -> "이 이미지를 바탕으로 유머러스하고 재미있는 동화를 만들어주세요. 웃음을 줄 수 있는 상황이나 캐릭터를 포함해주세요."
            else -> "이 이미지를 바탕으로 어린이를 위한 동화를 만들어주세요."
        }

        val prompt = """
            $themePrompt
            
            동화는 다음과 같은 형식으로 제공해주세요:
            {
                "title": "동화 제목",
                "text": "동화 내용...",
                "averagePitch": 120.5,
                "pitchStdDev": 15.2,
                "mfccValues": [[값1, 값2, ..., 값13], [값1, 값2, ..., 값13], ...]
            }
            
            averagePitch는 0~200 사이의 실수로, 음성의 평균 피치를 나타냅니다.
            pitchStdDev는 0~50 사이의 실수로, 음성 피치의 표준편차를 나타냅니다.
            mfccValues는 13차원 MFCC 계수 값을 나타내는 2차원 배열입니다.
            
            동화는 5분 정도 읽을 수 있는 분량(약 3000~5000자)으로 작성해주세요.
            전체 관람가 내용으로 작성해주세요.
            동화는 한국어로 작성해주세요.
        """.trimIndent()

        // JSON 요청 본문 생성
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", JSONArray().apply {
                    // 텍스트 메시지
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", prompt)
                    })
                    // 이미지 메시지
                    put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", JSONObject().apply {
                            put("url", "data:image/jpeg;base64,$base64Image")
                        })
                    })
                })
            })
        }

        val requestObj = JSONObject().apply {
            put("model", "gpt-4o")
            put("messages", messages)
            put("max_tokens", 4000)
        }

        return requestObj.toString()
    }

    private fun parseResponse(response: String): String {
        try {
            val jsonResponse = JSONObject(response)

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw IllegalStateException("GPT API 오류: ${error.optString("message", "알 수 없는 오류")}")
            }

            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() == 0) {
                throw IllegalStateException("GPT API가 응답을 생성하지 못했습니다.")
            }

            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.getString("content")

            Log.d(TAG, "Raw GPT response content: $content")

            // 응답이 이미 JSON 형식인지 확인
            return try {
                // JSON 형식을 추출하기 위해 중괄호 찾기
                val startIndex = content.indexOf("{")
                val endIndex = content.lastIndexOf("}")

                if (startIndex >= 0 && endIndex > startIndex) {
                    val jsonContent = content.substring(startIndex, endIndex + 1)
                    // JSON 형식인지 확인
                    val jsonObject = JSONObject(jsonContent)

                    // 필수 필드 확인
                    if (!jsonObject.has("title") || !jsonObject.has("text")) {
                        throw Exception("필수 필드가 없습니다")
                    }

                    // 필요한 필드 추가
                    if (!jsonObject.has("averagePitch")) {
                        jsonObject.put("averagePitch", 120.0)
                    }
                    if (!jsonObject.has("pitchStdDev")) {
                        jsonObject.put("pitchStdDev", 15.0)
                    }
                    if (!jsonObject.has("mfccValues")) {
                        val mfccArray = JSONArray()
                        val coeffArray = JSONArray()
                        for (i in 0 until 13) {
                            coeffArray.put(0.0)
                        }
                        mfccArray.put(coeffArray)
                        jsonObject.put("mfccValues", mfccArray)
                    }

                    jsonObject.toString()
                } else {
                    // 중괄호가 없는 경우 전체 텍스트를 내용으로 사용
                    """
                    {
                        "title": "자동 생성된 제목",
                        "text": ${JSONObject.quote(content)},
                        "averagePitch": 120.0,
                        "pitchStdDev": 15.0,
                        "mfccValues": [[0,0,0,0,0,0,0,0,0,0,0,0,0]]
                    }
                    """.trimIndent()
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON 파싱 오류: ${e.message}", e)
                // JSON 형식이 아닌 경우 JSON으로 변환
                """
                {
                    "title": "자동 생성된 제목",
                    "text": ${JSONObject.quote(content)},
                    "averagePitch": 120.0,
                    "pitchStdDev": 15.0,
                    "mfccValues": [[0,0,0,0,0,0,0,0,0,0,0,0,0]]
                }
                """.trimIndent()
            }
        } catch (e: Exception) {
            Log.e(TAG, "응답 파싱 오류: ${e.message}", e)
            throw IllegalStateException("GPT API 응답 파싱 오류: ${e.message}")
        }
    }
}