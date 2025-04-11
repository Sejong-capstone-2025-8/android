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

class GPTService {
    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
        private val API_KEY = BuildConfig.GPT_API_KEY
        private const val TAG = "GPTService"
    }

    private val networkUtil = NetworkUtil()

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
        // 요청 메시지의 content에 프롬프트와 이미지 데이터를 함께 삽입
        val contentText = "$themePrompt\n\n이미지 데이터: data:image/jpeg;base64,$base64Image"

        // 기본 메시지 배열 생성 (단일 메시지 객체)
        val messages = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", contentText)
                }
            )
        }


//        val prompt = """
//            $themePrompt
//
//            동화는 다음과 같은 형식으로 제공해주세요:
//            {
//                "title": "동화 제목",
//                "text": "동화 내용...",
//                "averagePitch": 120.5,
//                "pitchStdDev": 15.2,
//                "mfccValues": [[값1, 값2, ..., 값13], [값1, 값2, ..., 값13], ...]
//            }
//
//            averagePitch는 0~200 사이의 실수로, 음성의 평균 피치를 나타냅니다.
//            pitchStdDev는 0~50 사이의 실수로, 음성 피치의 표준편차를 나타냅니다.
//            mfccValues는 13차원 MFCC 계수 값을 나타내는 2차원 배열입니다.
//
//            동화는 5분 정도 읽을 수 있는 분량(약 3000~5000자)으로 작성해주세요.
//            전체 관람가 내용으로 작성해주세요.
//            동화는 한국어로 작성해주세요.
//        """.trimIndent()
//
//        // JSON 요청 본문 생성
//        val messages = JSONArray().apply {
//            put(JSONObject().apply {
//                put("role", "user")
//                put("content", JSONArray().apply {
//                    // 텍스트 메시지
//                    put(JSONObject().apply {
//                        put("type", "text")
//                        put("text", prompt)
//                    })
//                    // 이미지 메시지
//                    put(JSONObject().apply {
//                        put("type", "image_url")
//                        put("image_url", JSONObject().apply {
//                            put("url", "data:image/jpeg;base64,$base64Image")
//                        })
//                    })
//                })
//            })
//        }

        val requestObj = JSONObject().apply {
            put("model", "gpt-4o")
            put("messages", messages)
            put("max_tokens", 4000)
        }

        return requestObj.toString()
    }

    // 더미 응답 생성 (API 호출 없이 테스트용)
//    private fun generateDummyResponse(theme: String): String {
//        val uuid = UUID.randomUUID().toString().substring(0, 8)
//
//        val title = when (theme) {
//            "fantasy" -> "마법의 숲속 모험"
//            "love" -> "마음을 나누는 친구들"
//            "sf" -> "별빛 너머의 우주여행"
//            "horror" -> "어둠 속의 비밀"
//            "comedy" -> "웃음가득 장난꾸러기"
//            else -> "신비한 여행"
//        }
//
//        val storyStart = when (theme) {
//            "fantasy" -> "옛날 옛적, 마법이 가득한 숲속에 작은 마을이 있었습니다. 그곳에 살던 소녀 리나는 어느 날 숲속에서 반짝이는 빛을 발견했습니다."
//            "love" -> "따뜻한 봄날, 작은 학교의 교실에서 만난 두 친구 민수와 지영이는 서로에게 특별한 감정을 느꼈습니다."
//            "sf" -> "미래의 어느 날, 첨단 기술로 가득한 도시에 사는 소년 테오는 우주 탐험을 꿈꾸고 있었습니다."
//            "horror" -> "깊은 밤, 이상하게 울리는 소리에 잠에서 깬 지훈이는 집안의 이상한 그림자를 발견했습니다."
//            "comedy" -> "장난꾸러기 동동이는 항상 친구들과 선생님을 놀라게 하는 장난을 생각해냈습니다."
//            else -> "어느 화창한 날, 호기심 많은 아이가 숲속에서 반짝이는 물건을 발견했습니다."
//        }
//
//        val storyMiddle = "그것은 무척 신비로웠고, 아이는 그것을 조심스럽게 집어들었습니다. 갑자기 주변이 환해지더니 아이는 전혀 다른 세계로 이동했습니다. 그곳은 색다른 생물들과 놀라운 풍경으로 가득했습니다. \"어디로 왔을까?\" 아이는 궁금해했습니다. 얼마 지나지 않아 아이는 그곳의 주민들과 만나 이야기를 나누게 되었습니다. 그들은 아이에게 도움을 요청했습니다. 그들의 세계가 위험에 처해있다는 것이었습니다. 용기있는 아이는 그들을 돕기로 결심했습니다. 모험의 시작이었습니다."
//
//        val storyEnd = when (theme) {
//            "fantasy" -> "마법의 힘을 모아 마침내 어둠의 마법사를 물리친 리나는 마을로 돌아와 영웅이 되었습니다. 그 후로도 그녀는 종종 마법의 숲을 방문하며 신비로운 친구들과 새로운 모험을 즐겼답니다."
//            "love" -> "함께 보낸 시간을 통해 서로의 소중함을 깨달은 민수와 지영이는 평생의 우정을 약속했습니다. 그들의 따뜻한 마음은 모든 사람들에게 전해져 학교 전체가 더욱 화목해졌답니다."
//            "sf" -> "마침내 우주선을 타고 별들 사이를 여행한 테오는 지구로 돌아와 자신의 경험을 친구들과 나누었습니다. 그의 이야기는 많은 아이들에게 꿈과 희망을 주었답니다."
//            "horror" -> "알고 보니 그 그림자는 길을 잃은 작은 고양이였습니다. 지훈이는 안도의 한숨을 쉬며 고양이에게 우유를 주었고, 그 후로 둘은 좋은 친구가 되었답니다."
//            "comedy" -> "결국 장난으로 인해 큰 소동이 일어났지만, 모두가 웃음으로 마무리되었습니다. 동동이는 장난을 치면서도 다른 사람들을 배려하는 법을 배웠답니다."
//            else -> "모험을 마치고 집으로 돌아온 아이는 그날의 경험을 절대 잊지 못했습니다. 그리고 언젠가 다시 그 신비로운 세계를 방문할 수 있기를 간절히 바랐답니다."
//        }
//
//        val completeStory = storyStart + "\n\n" + storyMiddle + "\n\n" + storyEnd
//
//        return """
//        {
//            "title": "$title - $uuid",
//            "text": "$completeStory",
//            "averagePitch": ${110 + (Math.random() * 30)},
//            "pitchStdDev": ${10 + (Math.random() * 15)},
//            "mfccValues": [[${(0..12).joinToString { (Math.random() * 2 - 1).toString() }}]]
//        }
//        """.trimIndent()
//    }

    private fun generateErrorResponse(errorMessage: String): String {
        return """
        {
            "title": "동화 생성 실패",
            "text": "동화를 생성하는 과정에서 오류가 발생했습니다: $errorMessage\n\n대신 기본 동화를 제공합니다.\n\n옛날 옛적에 작은 마을에 착한 아이가 살았습니다. 어느 날 숲속에서 길을 잃은 작은 새를 발견했습니다. 아이는 새를 집으로 데려와 정성껏 돌봐주었습니다. 며칠 후 새의 날개가 나아 하늘로 날아갔습니다. 그리고 매년 봄이 되면 그 새는 아이를 찾아와 감사의 노래를 불러주었답니다.",
            "averagePitch": 120.0,
            "pitchStdDev": 15.0,
            "mfccValues": [[0,0,0,0,0,0,0,0,0,0,0,0,0]]
        }
        """.trimIndent()
    }
}