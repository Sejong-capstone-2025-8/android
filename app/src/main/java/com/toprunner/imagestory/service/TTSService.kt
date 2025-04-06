package com.toprunner.imagestory.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.toprunner.imagestory.BuildConfig
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class TTSService(private val context: Context) {
    private val voiceRepository = VoiceRepository(context)
    private val networkUtil = NetworkUtil()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: String? = null
    private var currentPosition: Int = 0
    private var totalDuration: Int = 0

    companion object {
        private const val API_URL = "https://api.elevenlabs.io/v1/text-to-speech"
        private const val API_KEY = BuildConfig.ELEVENLABS_API_KEY
        private const val TAG = "TTSService"
    }

    suspend fun generateVoice(text: String, voiceId: Long = 0): ByteArray = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating voice for text length: ${text.length}")

            // API 요청 헤더
            val headers = mapOf(
                "Content-Type" to "application/json",
                "xi-api-key" to API_KEY
            )

            // 음성 ID 결정 (기본값: 'eleven_multilingual_v2')
            val elevenlabsVoiceId = getElevenlabsVoiceId(voiceId)
            Log.d(TAG, "Using Elevenlabs voice ID: $elevenlabsVoiceId")

            // 요청 본문 생성
            val requestBody = createRequestBody(text)

            // API URL (음성 ID 포함)
            val apiUrl = "$API_URL/$elevenlabsVoiceId"
            Log.d(TAG, "Sending request to Elevenlabs API: $apiUrl")

            // API 요청 전송
            val responseBytes = networkUtil.downloadAudio(apiUrl, headers, requestBody)
            Log.d(TAG, "Received audio response size: ${responseBytes.size} bytes")

            // 응답이 비어있으면 오류 처리
            if (responseBytes.isEmpty()) {
                throw IllegalStateException("음성 생성에 실패했습니다: 응답이 비어있습니다.")
            }

            responseBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error generating voice: ${e.message}", e)

            // 오류 발생 시 기본 오디오 반환
            try {
                context.assets.open("error_audio.mp3").readBytes()
            } catch (assetException: Exception) {
                Log.e(TAG, "Error loading fallback audio: ${assetException.message}", assetException)
                // 빈 오디오 반환
                ByteArray(0)
            }
        }
    }

    private fun getElevenlabsVoiceId(voiceId: Long): String {
        // 음성 ID에 따라 Elevenlabs 음성 ID 결정
        // 현재는 간단한 매핑만 구현
        return when (voiceId) {
            1L -> "21m00Tcm4TlvDq8ikWAM" // Rachel
            2L -> "AZnzlk1XvdvUeBnXmlld" // Domi
            3L -> "EXAVITQu4vr4xnSDxMaL" // Bella
            4L -> "ErXwobaYiN019PkySvjV" // Antoni
            5L -> "MF3mGyEYCl7XYWbV9V6O" // Elli
            else -> "21m00Tcm4TlvDq8ikWAM" // 기본값 Rachel
        }
    }

    private fun createRequestBody(text: String): String {
        val jsonObj = JSONObject().apply {
            put("text", text)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.5)
                put("similarity_boost", 0.75)
            })
        }
        return jsonObj.toString()
    }

    suspend fun getVoiceList(): List<VoiceEntity> = withContext(Dispatchers.IO) {
        voiceRepository.getAllVoices()
    }

    fun playAudio(audioPath: String): Boolean {
        return try {
            stopAudio() // 기존 재생 중인 오디오 정지

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                setOnCompletionListener {
                    this@TTSService.currentPosition = 0
                }
                start()
            }

            currentAudioPath = audioPath
            totalDuration = mediaPlayer?.duration ?: 0
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio: ${e.message}", e)
            false
        }
    }

    fun pauseAudio(): Boolean {
        return try {
            if (mediaPlayer?.isPlaying == true) {
                currentPosition = mediaPlayer?.currentPosition ?: 0
                mediaPlayer?.pause()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio: ${e.message}", e)
            false
        }
    }

    fun resumeAudio(): Boolean {
        return try {
            mediaPlayer?.apply {
                if (!isPlaying) {
                    seekTo(currentPosition)
                    start()
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio: ${e.message}", e)
            false
        }
    }

    fun stopAudio(): Boolean {
        return try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
            mediaPlayer = null
            currentPosition = 0
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio: ${e.message}", e)
            false
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: currentPosition
    }

    fun getTotalDuration(): Int {
        return totalDuration
    }

    fun getPlaybackProgress(): Float {
        if (totalDuration <= 0) return 0f
        return getCurrentPosition().toFloat() / totalDuration
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}