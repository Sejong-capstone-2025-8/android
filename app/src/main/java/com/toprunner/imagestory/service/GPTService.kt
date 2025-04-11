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
            Log.d(TAG, "Generating story with theme: $theme")
            val base64Image = encodeImageToBase64(image)
            val requestBody = createRequestBody(base64Image, theme)
            // 실제 API 호출 (실제 API URL과 헤더, API 키를 사용)
            val response = networkUtil.sendHttpRequest(
                url = API_URL,
                method = "POST",
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $API_KEY"
                ),
                body = requestBody
            )
            Log.d(TAG, "GPT API raw response: $response")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error generating story: ${e.message}", e)
            generateErrorResponse(e.message ?: "Unknown error")
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


    fun encodeImageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        // 이미지 크기와 질 조정으로 API 요청 크기 제한 대응
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scaleRatio = 1024f / maxOf(bitmap.width, bitmap.height)
            val scaledWidth = (bitmap.width * scaleRatio).toInt()
            val scaledHeight = (bitmap.height * scaleRatio).toInt()
            bitmap.scale(scaledWidth, scaledHeight)
        } else {
            bitmap
        }

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun createRequestBody(base64Image: String, theme: String): String {
        val themePrompt = when (theme) {
            "fantasy" -> "이 이미지를 바탕으로 한국어로 판타지 장르의 동화를 만들어주세요. 마법, 모험, 상상의 세계를 포함해주세요."
            "love" -> "이 이미지를 바탕으로 한국어로 사랑에 관한 동화를 만들어주세요. 따뜻한 감정과 인간 관계를 중심으로 해주세요."
            "sf" -> "이 이미지를 바탕으로 한국어로 공상과학(SF) 장르의 동화를 만들어주세요. 미래, 기술, 우주 등의 요소를 포함해주세요."
            "horror" -> "이 이미지를 바탕으로 한국어로 무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
            "comedy" -> "이 이미지를 바탕으로 한국어로 유머러스하고 재미있는 동화를 만들어주세요. 웃음을 줄 수 있는 상황이나 캐릭터를 포함해주세요."
            else -> "이 이미지를 바탕으로 한국어로 어린이를 위한 동화를 만들어주세요."
        }

        // 동화 생성에 필요한 상세 프롬프트 작성 (JSON 출력 형식을 명시) - 검토 필요
        val detailedPrompt = """
        $themePrompt

        아래의 요구사항에 따라 동화를 생성해주세요:
        1. 결과는 반드시 JSON 형식의 객체여야 합니다.
        2. JSON 객체는 다음 키들을 포함해야 합니다: "title" (문자열), "theme" (문자열, (fantasy,love,sf,horror,comedy 중 하나)), "text" (문자열), "averagePitch" (실수), "pitchStdDev" (실수), "mfccValues" (숫자 배열들의 배열, 각 내부 배열은 13개의 숫자를 포함).
        3. 추가적인 설명이나 부가 텍스트 없이 오직 JSON 객체만 출력해야 합니다.

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
            put("model", "gpt-4o")
            put("messages", messages)
            put("max_tokens", 5000)
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