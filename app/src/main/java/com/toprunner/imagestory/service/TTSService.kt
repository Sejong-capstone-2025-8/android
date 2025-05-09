package com.toprunner.imagestory.service

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
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
import java.util.UUID

class TTSService(private val context: Context) {
    private val voiceRepository = VoiceRepository(context)
    private val networkUtil = NetworkUtil()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: String? = null
    private var currentPosition: Int = 0
    private var totalDuration: Int = 0
    private var playbackSpeed = 1.0f  // 기본 속도 (1.0 = 정상)
    private var pitch = 1.0f          // 기본 피치 (1.0 = 정상)



    companion object {
        private const val API_URL = "https://api.elevenlabs.io/v1/text-to-speech"
        private val API_KEY = BuildConfig.ELEVENLABS_API_KEY
        private const val TAG = "TTSService"
    }

    // 속도 설정 함수
    fun setPlaybackSpeed(speed: Float): Boolean {
        return try {
            playbackSpeed = speed.coerceIn(0.5f, 2.0f) // 0.5배속 ~ 2배속으로 제한

            mediaPlayer?.let { player ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val params = player.playbackParams
                    params.speed = playbackSpeed
                    player.playbackParams = params
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting playback speed: ${e.message}", e)
            false
        }
    }

    // 피치 설정 함수
    fun setPitch(newPitch: Float): Boolean {
        return try {
            pitch = newPitch.coerceIn(0.5f, 2.0f) // 0.5 ~ 2.0으로 제한

            mediaPlayer?.let { player ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val params = player.playbackParams
                    params.pitch = pitch
                    player.playbackParams = params
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting pitch: ${e.message}", e)
            false
        }
    }

    fun getPlaybackSpeed(): Float = playbackSpeed
    fun getPitch(): Float = pitch

    suspend fun generateVoice(text: String, voiceId: Long): ByteArray = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating voice for text length: ${text.length} with voice ID: $voiceId")

            // 음성 엔티티 확인을 위해 voiceRepository의 getVoiceById 호출 추가
            val voiceEntity = voiceRepository.getVoiceById(voiceId)
            if (voiceEntity == null) {
                Log.e(TAG, "Voice entity not found for ID: $voiceId")
                return@withContext ByteArray(0)
            }

            // 음성 정보 로그 추가
            Log.d(TAG, "Using voice: ${voiceEntity.title} with ID: ${voiceEntity.voice_id}")

            val headers = mapOf(
                "Content-Type" to "application/json",
                "xi-api-key" to API_KEY
            )

            // elevenlabsVoiceId 가져오기
            val elevenlabsVoiceId = getElevenlabsVoiceId(voiceId, voiceEntity)
            Log.d(TAG, "Using ElevenLabs voice ID: $elevenlabsVoiceId")


            val requestBody = createRequestBody(text)
            val apiUrl = "$API_URL/$elevenlabsVoiceId"

            try {
                val responseBytes = networkUtil.downloadAudio(apiUrl, headers, requestBody)
                if (responseBytes.isEmpty()) {
                    Log.e(TAG, "Empty response from ElevenLabs API")
                    throw IllegalStateException("음성 생성에 실패했습니다: 응답이 비어있습니다.")
                }

                Log.d(TAG, "Successfully generated audio, size: ${responseBytes.size} bytes")
                return@withContext responseBytes
            } catch (e: Exception) {
                // API 오류 발생 시 기본 음성(Rachel)으로 재시도
                Log.e(TAG, "Error with voice ID: $elevenlabsVoiceId. Trying fallback voice. Error: ${e.message}")

                // 기본 음성 ID로 재시도 (Rachel - 안정적인 음성)
                val fallbackVoiceId = "21m00Tcm4TlvDq8ikWAM"
                val fallbackApiUrl = "$API_URL/$fallbackVoiceId"

                try {
                    val fallbackResponse = networkUtil.downloadAudio(fallbackApiUrl, headers, requestBody)
                    if (fallbackResponse.isNotEmpty()) {
                        Log.d(TAG, "Fallback voice generation successful, size: ${fallbackResponse.size} bytes")
                        return@withContext fallbackResponse
                    }
                } catch (fallbackError: Exception) {
                    Log.e(TAG, "Fallback voice also failed: ${fallbackError.message}")
                }

                // 모든 시도가 실패하면 기본 오디오 반환
                return@withContext generateDummyAudio(text.length)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating voice: ${e.message}", e)
            generateDummyAudio(text.length)
        }

    }



    // 더미 오디오 데이터 생성 (테스트용)
    private fun generateDummyAudio(textLength: Int): ByteArray {
        // textLength에 비례하는 임의의 바이트 배열 생성
        val size = 1000 + (textLength / 10)
        val dummyAudio = ByteArray(size) { (Math.random() * 256).toInt().toByte() }

        // 임시 파일에 저장 (실제 파일이 있어야 MediaPlayer가 작동함)
        val fileName = "dummy_audio_${UUID.randomUUID()}.wav"
        val audioDir = File(context.filesDir, "audio_files").apply {
            if (!exists()) mkdirs()
        }
        val audioFile = File(audioDir, fileName)

        try {
            FileOutputStream(audioFile).use { it.write(dummyAudio) }
            Log.d(TAG, "Dummy audio saved to: ${audioFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving dummy audio: ${e.message}", e)
        }

        return dummyAudio
    }

    private fun getElevenlabsVoiceId(voiceId: Long, voiceEntity: VoiceEntity? = null): String {
        // 1. 음성 객체가 전달된 경우, attribute에서 elevenlabsVoiceId를 추출 시도
        if (voiceEntity != null) {
            try {
                val attributeJson = JSONObject(voiceEntity.attribute)
                val elevenlabsId = attributeJson.optString("elevenlabsVoiceId", "")
                if (elevenlabsId.isNotEmpty()) {
                    Log.d(TAG, "Found ElevenLabs ID in voice attributes: $elevenlabsId")
                    return elevenlabsId
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing voice attributes: ${e.message}")
            }
        }

        // 2. 기본 매핑 사용
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

                // 안드로이드 M(API 23) 이상에서 속도 및 피치 설정
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val params = PlaybackParams()
                    params.speed = playbackSpeed
                    params.pitch = pitch
                    playbackParams = params
                }

                setOnCompletionListener {
                    this@TTSService.currentPosition = 0
                    release()
                    mediaPlayer = null
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
                true
            } else {
                false
            }
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
                    return true
                }
            }
            false
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
        try {
            val current = getCurrentPosition()
            val total = getTotalDuration()
            return if (total > 0) {
                // 0~1 범위로 제한
                (current.toFloat() / total).coerceIn(0f, 1f)
            } else 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playback progress: ${e.message}")
            return 0f
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    fun calculateAudioDuration(audioPath: String): Int {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration  // 밀리초 단위로 반환하도록 수정
            mediaPlayer.release()
            return duration
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating audio duration: ${e.message}", e)
            return 0
        }
    }
    // TTSService.kt (추가)
    fun seekTo(positionMs: Int): Boolean {
        return try {
            mediaPlayer?.seekTo(positionMs)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking audio: ${e.message}", e)
            false
        }
    }


}