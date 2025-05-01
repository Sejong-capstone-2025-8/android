package com.toprunner.imagestory.service

import android.content.Context
import android.util.Log
import com.toprunner.imagestory.BuildConfig
import com.toprunner.imagestory.SimpleAudioAnalyzer
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.util.AudioAnalyzer
import com.toprunner.imagestory.util.FileStorageManager
import com.toprunner.imagestory.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * VoiceCloneService - 음성 복제 기능을 담당하는 서비스 클래스
 * Elevenlabs API를 활용한 Instant Voice Cloning 기능 구현
 */
class VoiceCloneService(private val context: Context) {
    private val TAG = "VoiceCloneService"
    private val voiceRepository = VoiceRepository(context)
    private val networkUtil = NetworkUtil()
    private val fileStorageManager = FileStorageManager()

    companion object {
        private const val API_URL_VOICE_CLONE = "https://api.elevenlabs.io/v1/voices/add"
        private const val API_URL_TTS = "https://api.elevenlabs.io/v1/text-to-speech"
        private val API_KEY = BuildConfig.ELEVENLABS_API_KEY
    }

    /**
     * 음성 파일을 기반으로 Elevenlabs API를 통해 복제 음성 생성
     * @param sourceVoicePath 원본 음성 파일 경로
     * @param sampleText 생성된 음성으로 읽을 예시 텍스트
     * @param name 생성될 복제 음성의 이름
     * @return Pair<Boolean, String> 성공 여부와 메시지(또는 오류 메시지)
     */
    suspend fun cloneVoice(sourceVoicePath: String, sampleText: String, name: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting voice cloning process for $name")

            // 1. 원본 음성 파일 가져오기
            val sourceFile = File(sourceVoicePath)
            if (!sourceFile.exists()) {
                return@withContext Pair(false, "원본 음성 파일을 찾을 수 없습니다.")
            }


            // 2. API 요청 헤더 설정
            val headers = mapOf(
                "Accept" to "application/json",
                "xi-api-key" to API_KEY
            )

            // 3. 음성 파일을 바이트 배열로 읽기
            val audioData = sourceFile.readBytes()

            // 4. 음성 클론 요청 준비 (Multipart 요청 구성)
            val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
            val contentType = "multipart/form-data; boundary=$boundary"

            // 5. 요청 바디 구성
            val outputStream = java.io.ByteArrayOutputStream()

            // name 부분
            outputStream.write("--$boundary\r\n".toByteArray())
            outputStream.write("Content-Disposition: form-data; name=\"name\"\r\n\r\n".toByteArray())
            outputStream.write("$name\r\n".toByteArray())

            // description 부분 (선택적)
            outputStream.write("--$boundary\r\n".toByteArray())
            outputStream.write("Content-Disposition: form-data; name=\"description\"\r\n\r\n".toByteArray())
            outputStream.write("낭독용으로 생성된 복제 음성입니다.\r\n".toByteArray())

            // 음성 파일 부분
            outputStream.write("--$boundary\r\n".toByteArray())
            outputStream.write("Content-Disposition: form-data; name=\"files\"; filename=\"sample_audio.wav\"\r\n".toByteArray())
            outputStream.write("Content-Type: audio/wav\r\n\r\n".toByteArray())
            outputStream.write(audioData)
            outputStream.write("\r\n".toByteArray())

            // 종료 부분
            outputStream.write("--$boundary--\r\n".toByteArray())

            val requestBody = outputStream.toByteArray()

            // 6. 헤더 추가 (Multipart 요청용)
            val cloneHeaders = headers + mapOf("Content-Type" to contentType)

            // 7. API 요청 - 음성 클론 생성
            Log.d(TAG, "Sending voice clone request to Elevenlabs API")
            val response = networkUtil.sendMultipartRequest(
                url = API_URL_VOICE_CLONE,
                headers = cloneHeaders,
                body = requestBody
            )

            // 8. 응답 처리
            val jsonResponse = JSONObject(response)
            val voiceId = jsonResponse.optString("voice_id", "")

            if (voiceId.isBlank()) {
                return@withContext Pair(false, "음성 ID를 받지 못했습니다.")
            }

            Log.d(TAG, "Voice clone created with ID: $voiceId")

            // 9. 예시 텍스트로 음성 샘플 생성
            val sampleAudio = generateSampleAudio(voiceId, sampleText)
            if (sampleAudio.isEmpty()) {
                return@withContext Pair(false, "샘플 오디오 생성에 실패했습니다.")
            }

//            // 10... 샘플값은 나중을 위해 남겨둠
//            val voiceFeatures = VoiceFeatures(
//                averagePitch = 120.0, // 기본값 사용
//                pitchStdDev = 15.0,   // 기본값 사용
//                mfccValues = listOf(DoubleArray(13) { 0.0 }) // 기본값 사용
//            )
            // 10.TarsosDSP를 사용하여 원본 음성 파일 분석
            val simpleAnalyzer = SimpleAudioAnalyzer(context)
            Log.d(TAG, "Analyzing source voice file: $sourceVoicePath")
            val voiceFeatures = simpleAnalyzer.analyzeAudio(sourceVoicePath)
            Log.d(TAG, "Voice analysis complete: pitch=${voiceFeatures.averagePitch}, stdDev=${voiceFeatures.pitchStdDev}")

            // 속성 JSON 생성 (복제 음성 표시용) - 수정
            val attributeJson = JSONObject().apply {
                put("isClone", true)
                put("originalVoicePath", sourceVoicePath)
                put("elevenlabsVoiceId", voiceId)
                put("voiceType", "custom_clone")
                // 아래 특성값 직접 저장 추가
                put("averagePitch", voiceFeatures.averagePitch)
                put("pitchStdDev", voiceFeatures.pitchStdDev)

                // MFCC 값도 저장
                val mfccArray = JSONArray()
                for (coeffs in voiceFeatures.mfccValues) {
                    val coeffArray = JSONArray()
                    for (coeff in coeffs) {
                        coeffArray.put(coeff)
                    }
                    mfccArray.put(coeffArray)
                }
                put("mfccValues", mfccArray)
            }.toString()

            // 12. 저장소에 저장
            val savedId = voiceRepository.saveVoice(
                title = name,
                attributeJson = attributeJson,
                audioData = sampleAudio,
                voiceFeatures = voiceFeatures
            )

            Log.d(TAG, "Clone voice saved with ID: $savedId")
            return@withContext Pair(true, "음성이 성공적으로 복제되었습니다.")

        } catch (e: Exception) {
            Log.e(TAG, "Error cloning voice: ${e.message}", e)
            return@withContext Pair(false, "음성 복제 중 오류 발생: ${e.message}")
        }
    }

    /**
     * 생성된 복제 음성으로 예시 텍스트를 읽는 오디오 생성
     * @param voiceId Elevenlabs에서 생성된 음성 ID
     * @param text 읽을 텍스트
     * @return 생성된 오디오 바이트 배열
     */
    private suspend fun generateSampleAudio(voiceId: String, text: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating sample audio with voice ID: $voiceId")

            // 1. TTS API 요청 헤더
            val headers = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "audio/mpeg",
                "xi-api-key" to API_KEY
            )

            // 2. 요청 바디 구성
            val requestBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.75)
                })
            }.toString()

            // 3. API 요청 - TTS 생성
            val apiUrl = "$API_URL_TTS/$voiceId"
            val responseBytes = networkUtil.downloadAudio(apiUrl, headers, requestBody)

            if (responseBytes.isEmpty()) {
                Log.e(TAG, "Empty response from TTS API")
                return@withContext ByteArray(0)
            }

            Log.d(TAG, "Sample audio generated successfully: ${responseBytes.size} bytes")
            return@withContext responseBytes

        } catch (e: Exception) {
            Log.e(TAG, "Error generating sample audio: ${e.message}", e)
            return@withContext ByteArray(0)
        }
    }
}