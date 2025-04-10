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
import java.util.UUID

class TTSService(private val context: Context) {
    private val voiceRepository = VoiceRepository(context)
    private val networkUtil = NetworkUtil()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioPath: String? = null
    private var currentPosition: Int = 0
    private var totalDuration: Int = 0

    companion object {
        private const val API_URL = "https://api.elevenlabs.io/v1/text-to-speech"
        private val API_KEY = BuildConfig.ELEVENLABS_API_KEY
        private const val TAG = "TTSService"
    }

    suspend fun generateVoice(text: String, voiceId: Long): ByteArray = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating voice for text length: ${text.length} with voice ID: $voiceId")
            val headers = mapOf(
                "Content-Type" to "application/json",
                "xi-api-key" to API_KEY
            )
            val elevenlabsVoiceId = getElevenlabsVoiceId(voiceId)
            val requestBody = createRequestBody(text)
            val apiUrl = "$API_URL/$elevenlabsVoiceId"
            val responseBytes = networkUtil.downloadAudio(apiUrl, headers, requestBody)
            if (responseBytes.isEmpty()) {
                throw IllegalStateException("음성 생성에 실패했습니다: 응답이 비어있습니다.")
            }
            responseBytes
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

    private fun getElevenlabsVoiceId(voiceId: Long): String {
        // 음성 ID에 따라 Elevenlabs 음성 ID 결정
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
        if (totalDuration <= 0) return 0f
        return getCurrentPosition().toFloat() / totalDuration
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    fun calculateAudioDuration(audioPath: String): Int {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration / 1000 // 초 단위로 변환
            mediaPlayer.release()
            return duration
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating audio duration: ${e.message}", e)
            return 0
        }
    }
}