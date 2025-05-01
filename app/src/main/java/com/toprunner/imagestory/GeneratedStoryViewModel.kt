package com.toprunner.imagestory.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.service.TTSService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

data class StoryState(
    val isLoading: Boolean = true,
    val storyTitle: String = "",
    val storyContent: String = "",
    val storyImage: Bitmap? = null,
    val audioPath: String? = null
)

class GeneratedStoryViewModel : ViewModel() {

    // UI에서 이 데이터를 표시하거나 추가적인 처리가 필요하면 수행 가능

    private val _storyState = MutableStateFlow(StoryState())
    val storyState: StateFlow<StoryState> = _storyState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration.asStateFlow()

    private var ttsService: TTSService? = null

    fun loadStory(storyId: Long, context: Context) {
        viewModelScope.launch {
            try {
                _storyState.value = StoryState(isLoading = true)

                // TTSService 초기화
                if (ttsService == null) {
                    ttsService = TTSService(context)
                }

                val fairyTaleRepository = FairyTaleRepository(context)
                val imageRepository = ImageRepository(context)
                val textRepository = TextRepository(context)

                // 동화 엔티티 로드
                val (fairyTale, _) = fairyTaleRepository.getFairyTaleById(storyId)

                val attributeJson = JSONObject(fairyTale.attribute)
                val averagePitch = attributeJson.getDouble("averagePitch")
                val pitchStdDev = attributeJson.getDouble("pitchStdDev")

                // 제목 설정
                _storyState.value = _storyState.value.copy(storyTitle = fairyTale.title)

                // 텍스트 내용 로드
                val textEntity = textRepository.getTextById(fairyTale.text_id)
                if (textEntity != null) {
                    _storyState.value = _storyState.value.copy(storyContent = textEntity.second)
                }

                // 이미지 로드
                val imageEntity = imageRepository.getImageById(fairyTale.image_id)
                if (imageEntity != null) {
                    loadImage(imageEntity.image_path)
                }

                val audioPath = attributeJson.optString("audioPath", "")
                _storyState.value = _storyState.value.copy(audioPath = audioPath)
                if (audioPath.isNotEmpty()) {
                    try {
                        val durationMs = ttsService?.calculateAudioDuration(audioPath) ?: 0
                        _totalDuration.value = durationMs // 초 단위를 밀리초로 변환
                    } catch (e: Exception) {
                        Log.e("GeneratedStoryViewModel", "Error calculating audio duration: ${e.message}")
                        _totalDuration.value = 0
                    }
                }

                _storyState.value = _storyState.value.copy(isLoading = false)


            } catch (e: Exception) {
                Log.e("GeneratedStoryViewModel", "Error loading story: ${e.message}", e)
                _storyState.value = _storyState.value.copy(
                    isLoading = false,
                    storyTitle = "오류 발생",
                    storyContent = "동화를 불러오는 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    private fun loadImage(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                _storyState.value = _storyState.value.copy(storyImage = bitmap)
            } else {
                Log.e("GeneratedStoryViewModel", "Image file does not exist: $imagePath")
            }
        } catch (e: Exception) {
            Log.e("GeneratedStoryViewModel", "Error loading image: ${e.message}", e)
        }
    }

    fun playAudio() {
        viewModelScope.launch {
            _storyState.value.audioPath?.let { path ->
                if (path.isEmpty()) {
                    Log.e("GeneratedStoryViewModel", "Audio path is empty")
                    return@launch
                }

                val success = if (_isPlaying.value) {
                    ttsService?.resumeAudio() ?: false
                } else {
                    ttsService?.playAudio(path) ?: false
                }

                if (success) {
                    _isPlaying.value = true
                    startProgressTracking()
                } else {
                    Log.e("GeneratedStoryViewModel", "Failed to play audio")
                }
            }
        }
    }

    fun pauseAudio() {
        viewModelScope.launch {
            val success = ttsService?.pauseAudio() ?: false
            if (success) {
                _isPlaying.value = false
            }
        }
    }

    fun stopAudio() {
        viewModelScope.launch {
            val success = ttsService?.stopAudio() ?: false
            if (success) {
                _isPlaying.value = false
                _playbackProgress.value = 0f
            }
        }
    }
    fun seekTo(positionMs: Int) {
        viewModelScope.launch {
            ttsService?.seekTo(positionMs)
        }
    }


    // startProgressTracking 메서드 개선
    private fun startProgressTracking() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                try {
                    val currentPos = ttsService?.getCurrentPosition() ?: 0
                    val total = ttsService?.getTotalDuration() ?: 1

                    // 이전 값과 큰 차이가 있을 때만 업데이트 (스무딩 효과)
                    val newProgress = if (total > 0) currentPos.toFloat() / total else 0f
                    if (kotlin.math.abs(newProgress - _playbackProgress.value) > 0.01f) {
                        _playbackProgress.value = newProgress
                    }

                    // 재생 종료 감지 개선
                    if (newProgress >= 0.99f || ttsService?.isPlaying() == false) {
                        _isPlaying.value = false
                        _playbackProgress.value = 0f
                        break
                    }
                } catch (e: Exception) {
                    Log.e("GeneratedStoryViewModel", "Error tracking progress: ${e.message}")
                }

                delay(50) // 더 빠른 업데이트 주기
            }
        }
    }

    fun recommendVoice(context: Context) {
        // 동화에 어울리는 목소리 추천 로직 구현
        // 실제로는 테마와 현재 동화 특성에 맞는 목소리 추천 알고리즘 필요
    }

    override fun onCleared() {
        super.onCleared()
        ttsService?.stopAudio()
        ttsService = null
    }

    fun toggleAudioPlayback() {
        viewModelScope.launch {
            _storyState.value.audioPath?.let { path ->
                val currentPos = ttsService?.getCurrentPosition() ?: 0
                if (_isPlaying.value) {
                    // 재생 중이면 정지
                    val paused = ttsService?.pauseAudio() ?: false
                    if (paused) {
                        _isPlaying.value = false
                    } else {
                        Log.e("GeneratedStoryViewModel", "Failed to pause audio")
                    }
                } else {
                    val success = if (currentPos > 0) {
                        // 이미 재생된 적이 있으면 resume
                        ttsService?.resumeAudio() ?: false
                    } else {
                        // 처음 재생이면 playAudio() 호출
                        ttsService?.playAudio(path) ?: false
                    }
                    if (success) {
                        _isPlaying.value = true
                        startProgressTracking()
                    } else {
                        Log.e("GeneratedStoryViewModel", "Failed to play audio")
                    }
                }
            } ?: run {
                Log.e("GeneratedStoryViewModel", "Audio path is empty")
            }
        }
    }

    fun updatePlaybackProgress(progress: Float) {
        _playbackProgress.value = progress
    }


    fun stopPlayback() {
        _isPlaying.value = false
        _playbackProgress.value = 0f
        stopAudio() // 기존에 있는 오디오 중지 메서드 호출
    }
    fun getCurrentPlaybackProgress(): Float {
        return ttsService?.getPlaybackProgress() ?: 0f
    }
}