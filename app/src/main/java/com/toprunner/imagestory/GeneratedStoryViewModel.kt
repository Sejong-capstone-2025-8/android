package com.toprunner.imagestory

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.service.TTSService
import com.toprunner.imagestory.util.VoiceFeaturesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

data class StoryState(
    val isLoading: Boolean = true,
    val storyId: Long = -1L,
    val storyTitle: String = "",
    val storyContent: String = "",
    val storyImage: Bitmap? = null,
    val audioPath: String? = null,
    val voiceFeatures: VoiceFeatures? = null,
    val theme: String = "",
    val voiceId: Long = 0L  // 추가: 현재 사용 중인 음성 ID
)

data class VoiceRecommendationState(
    val isLoading: Boolean = false,
    val recommendedVoice: VoiceEntity? = null,
    val similarityPercentage: Int = 0,
    val cloneCreationInProgress: Boolean = false,
    val newStoryCreated: Boolean = false,  // 추가: 새 동화 생성 완료 여부
    val newStoryId: Long = -1L  // 추가: 새로 생성된 동화 ID
)

class GeneratedStoryViewModel : ViewModel() {

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _voiceListState = MutableStateFlow(VoiceListState())
    val voiceListState: StateFlow<VoiceListState> = _voiceListState.asStateFlow()


    private val _storyState = MutableStateFlow(StoryState())
    val storyState: StateFlow<StoryState> = _storyState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration.asStateFlow()

    // 음성 추천 기능 관련 상태
    private val _recommendationState = MutableStateFlow(VoiceRecommendationState())
    val recommendationState: StateFlow<VoiceRecommendationState> = _recommendationState.asStateFlow()

    // 원본 동화 엔티티 저장
    private var originalFairyTale: FairyTaleEntity? = null

    private var ttsService: TTSService? = null

    private val _bgmPath = MutableStateFlow<String?>(null)
    val bgmPath: StateFlow<String?> = _bgmPath.asStateFlow()

    private var bgmPlayer: MediaPlayer? = null
    private val _isBgmPlaying = MutableStateFlow(false)
    val isBgmPlaying: StateFlow<Boolean> = _isBgmPlaying.asStateFlow()

    private val _bgmVolume = MutableStateFlow(1f) // Default 100%
    val bgmVolume: StateFlow<Float> = _bgmVolume.asStateFlow()

    data class VoiceListState(
        val isLoading: Boolean = false,
        val voices: List<VoiceEntity> = emptyList(),
        val error: String? = null
    )

    // 속도 설정 함수
    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            ttsService?.setPlaybackSpeed(speed)?.let { success ->
                if (success) {
                    _playbackSpeed.value = speed
                    Log.d("GeneratedStoryViewModel", "Playback speed set: $speed")
                }
            }
        }
    }

    // 피치 설정 함수
    fun setPitch(newPitch: Float) {
        viewModelScope.launch {
            ttsService?.setPitch(newPitch)?.let { success ->
                if (success) {
                    _pitch.value = newPitch
                    Log.d("GeneratedStoryViewModel", "Pitch set: $newPitch")
                }
            }
        }
    }

    fun loadCloneVoices(context: Context) {
        viewModelScope.launch {
            try {
                _voiceListState.value = VoiceListState(isLoading = true)

                val voiceRepository = VoiceRepository(context)
                val allVoices = voiceRepository.getAllVoices()

                // 낭독용 음성만 필터링
                val cloneVoices = allVoices.filter { voice ->
                    try {
                        val attributeJson = JSONObject(voice.attribute)
                        attributeJson.optBoolean("isClone", false)
                    } catch (e: Exception) {
                        false
                    }
                }

                _voiceListState.value = VoiceListState(
                    isLoading = false,
                    voices = cloneVoices
                )

                Log.d(TAG, "Loaded ${cloneVoices.size} clone voices")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading clone voices: ${e.message}", e)
                _voiceListState.value = VoiceListState(
                    isLoading = false,
                    error = "음성 목록을 불러오는 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }


    fun setBackgroundMusicPath(path: String) {
        Log.d("GeneratedStoryViewModel", "배경음 경로 설정 전: ${_bgmPath.value}")
        _bgmPath.value = path
        Log.d("GeneratedStoryViewModel", "배경음 경로 설정 후: ${_bgmPath.value}")

        // 경로가 유효한지 확인
        val file = File(path)
        if (!file.exists()) {
            Log.e("GeneratedStoryViewModel", "배경음 파일이 존재하지 않습니다: $path")
        } else {
            Log.d("GeneratedStoryViewModel", "배경음 파일 확인 완료: $path (${file.length()} bytes)")
        }
    }

    fun setBackgroundMusicVolume(volume: Float) {
        bgmPlayer?.setVolume(volume, volume)  // Same volume for left and right
        _bgmVolume.value = volume
        Log.d("GeneratedStoryViewModel", "BGM volume set: ${"%.2f".format(volume)}")
    }
    fun loadBgmPathFromArgs(bgmPath: String?) {
        if (!bgmPath.isNullOrBlank()) {
            val file = File(bgmPath)
            if (file.exists()) {
                _bgmPath.value = bgmPath
                Log.d("GeneratedStoryViewModel", "BGM path loaded from args: $bgmPath")
            } else {
                Log.e("GeneratedStoryViewModel", "BGM file does not exist: $bgmPath")
            }
        }
    }

    fun toggleBackgroundMusic() {
        viewModelScope.launch {
            val bgm = _bgmPath.value

            Log.d("GeneratedStoryViewModel", "토글 배경음악 - 현재 경로: $bgm, 재생 상태: ${_isBgmPlaying.value}")

            if (bgm.isNullOrBlank()) {
                Log.e("GeneratedStoryViewModel", "BGM 경로가 없습니다 - 재생 불가")
                return@launch
            }

            val file = File(bgm)
            if (!file.exists()) {
                Log.e("GeneratedStoryViewModel", "배경음 파일이 존재하지 않습니다: $bgm")
                _bgmPath.value = null // 잘못된 경로는 초기화
                return@launch
            }

            // Rest of the function remains the same
            if (_isBgmPlaying.value) {
                pauseBackgroundMusic()
            } else {
                if (bgmPlayer == null) {
                    playBackgroundMusic(bgm)
                } else {
                    resumeBackgroundMusic()
                }
            }
        }
    }
    suspend fun createStoryWithSelectedVoice(context: Context, selectedVoice: VoiceEntity): Long {
        try {
            Log.d(TAG, "Starting to create story with selected voice: ${selectedVoice.title}, ID: ${selectedVoice.voice_id}")

            if (_storyState.value.storyId <= 0) {
                throw IllegalStateException("원본 동화 ID가 유효하지 않습니다")
            }

            // 동화 생성 진행 중 상태로 변경
            _recommendationState.value = _recommendationState.value.copy(cloneCreationInProgress = true)

            // 원본 동화 ID와 텍스트 내용
            val originalStoryId = _storyState.value.storyId
            val storyContent = _storyState.value.storyContent

            // 새 동화 제목 생성 0515 박찬우 변경사항
            val baseTitle = _storyState.value.storyTitle.substringBefore(" (버전:")
            val newTitle = "$baseTitle (버전: ${selectedVoice.title})"

            // TTS 서비스 초기화 확인
            if (ttsService == null) {
                ttsService = TTSService(context)
            }

            // 선택한 음성으로 동화 텍스트 읽기
            Log.d(TAG, "Generating audio with selected voice ID: ${selectedVoice.voice_id}")
            val audioData = ttsService?.generateVoice(storyContent, selectedVoice.voice_id) ?: ByteArray(0)

            if (audioData.isEmpty()) {
                Log.e(TAG, "Failed to generate audio with selected voice")
                throw IllegalStateException("선택한 음성으로 오디오를 생성할 수 없습니다")
            }

            Log.d(TAG, "Successfully generated audio with selected voice, size: ${audioData.size} bytes")

            // 새 동화 저장
            val fairyTaleRepository = FairyTaleRepository(context)

            val newStoryId = fairyTaleRepository.saveSelectedVoiceStory(
                originalStoryId = originalStoryId,
                selectedVoiceId = selectedVoice.voice_id,
                newTitle = newTitle,
                audioData = audioData
            )

            _recommendationState.value = _recommendationState.value.copy(
                cloneCreationInProgress = false,
                newStoryCreated = true,
                newStoryId = newStoryId
            )

            Log.d(TAG, "Created new story with ID: $newStoryId")

            // 생성된 동화 ID 반환
            return newStoryId

        } catch (e: Exception) {
            Log.e(TAG, "Error creating story with selected voice: ${e.message}", e)
            _recommendationState.value = _recommendationState.value.copy(cloneCreationInProgress = false)
            throw e
        }
    }

    private fun playBackgroundMusic(path: String) {
        try {
            bgmPlayer?.release()
            bgmPlayer = null

            Log.d("GeneratedStoryViewModel", "배경음악 경로: $path")
            val file = File(path)
            if (!file.exists()) {
                Log.e("GeneratedStoryViewModel", "음악 파일이 존재하지 않습니다: $path")
                return
            }

            bgmPlayer = MediaPlayer().apply {
                setDataSource(path)
                isLooping = true
                prepare()
                start()
            }
            _isBgmPlaying.value = true
            Log.d("GeneratedStoryViewModel", "배경음악 재생 성공: $path")
        } catch (e: Exception) {
            Log.e("GeneratedStoryViewModel", "배경음악 재생 실패: ${e.message}", e)
        }
    }

    private fun pauseBackgroundMusic() {
        try {
            bgmPlayer?.pause()
            _isBgmPlaying.value = false
            Log.d("GeneratedStoryViewModel", "BGM paused")
        } catch (e: Exception) {
            Log.e("GeneratedStoryViewModel", "Failed to pause BGM: ${e.message}")
        }
    }

    private fun resumeBackgroundMusic() {
        try {
            bgmPlayer?.start()
            _isBgmPlaying.value = true
            Log.d("GeneratedStoryViewModel", "BGM resumed")
        } catch (e: Exception) {
            Log.e("GeneratedStoryViewModel", "Failed to resume BGM: ${e.message}")
        }
    }
    private fun stopBackgroundMusic() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
        _isBgmPlaying.value = false
        Log.d("GeneratedStoryViewModel", "BGM stopped")
    }

    // Update these existing methods to handle BGM as well
    override fun onCleared() {
        super.onCleared()
        ttsService?.stopAudio()
        ttsService = null
        bgmPlayer?.release()
        bgmPlayer = null
    }

    fun stopAudio() {
        viewModelScope.launch {
            val success = ttsService?.stopAudio() ?: false
            if (success) {
                _isPlaying.value = false
                _playbackProgress.value = 0f
                stopBackgroundMusic()  // Also stop BGM
            }
        }
    }

    // Update toggleAudioPlayback to handle BGM
    fun toggleAudioPlayback() {
        viewModelScope.launch {
            _storyState.value.audioPath?.let { path ->
                val currentPos = ttsService?.getCurrentPosition() ?: 0

                if (_isPlaying.value) {
                    // If playing, pause
                    val paused = ttsService?.pauseAudio() ?: false
                    if (paused) {
                        pauseBackgroundMusic()  // Also pause BGM
                        _isPlaying.value = false
                        Log.d("GeneratedStoryViewModel", "TTS and BGM paused")
                    } else {
                        Log.e("GeneratedStoryViewModel", "Failed to pause TTS")
                    }
                } else {
                    // If paused or not started, play/resume
                    val success = if (currentPos > 0) {
                        ttsService?.resumeAudio() ?: false
                    } else {
                        ttsService?.playAudio(path) ?: false
                    }

                    if (success) {
                        _isPlaying.value = true
                        startProgressTracking()

                        val bgm = _bgmPath.value
                        if (!bgm.isNullOrBlank()) {
                            if (currentPos > 0) {
                                resumeBackgroundMusic()  // Resume BGM
                            } else {
                                playBackgroundMusic(bgm) // Start BGM
                            }
                            _isBgmPlaying.value = true
                        } else {
                            Log.d("GeneratedStoryViewModel", "No BGM path (only TTS playing)")
                        }

                        Log.d("GeneratedStoryViewModel", "TTS started playing")
                    } else {
                        Log.e("GeneratedStoryViewModel", "Failed to play TTS")
                    }
                }
            } ?: run {
                Log.e("GeneratedStoryViewModel", "Audio path is empty")
            }
        }
    }

    private var voiceFeaturesUtil: VoiceFeaturesUtil? = null

    fun loadStory(storyId: Long, context: Context, bgmPath: String? = null) {
        viewModelScope.launch {
            try {
                _storyState.value = StoryState(isLoading = true)

                ttsService = TTSService(context)
                voiceFeaturesUtil = VoiceFeaturesUtil()

                // 배경음 경로가 제공되면 즉시 설정
                if (!bgmPath.isNullOrBlank()) {
                    _bgmPath.value = bgmPath
                    Log.d("GeneratedStoryViewModel", "Setting BGM path from args: $bgmPath")
                }

                val fairyTaleRepository = FairyTaleRepository(context)
                val imageRepository = ImageRepository(context)
                val textRepository = TextRepository(context)

                // 동화 엔티티 로드 - 기존 코드 유지
                val (fairyTale, content) = fairyTaleRepository.getFairyTaleById(storyId)
                originalFairyTale = fairyTale

                val attributeJson = JSONObject(fairyTale.attribute)
                val theme = attributeJson.optString("theme", "fantasy")

                // 음성 특성 추출 - 기존 코드 유지
                val voiceFeatures = VoiceFeatures(
                    averagePitch = attributeJson.optDouble("averagePitch", 150.0),
                    pitchStdDev = attributeJson.optDouble("pitchStdDev", 15.0),
                    mfccValues = extractMfccValues(attributeJson)
                )

                // 제목 설정 - 기존 코드 유지
                _storyState.value = _storyState.value.copy(
                    storyId = storyId,
                    storyTitle = fairyTale.title,
                    storyContent = content,
                    voiceFeatures = voiceFeatures,
                    theme = theme,
                    voiceId = fairyTale.voice_id
                )

                // 이미지 로드 - 기존 코드 유지
                val imageEntity = imageRepository.getImageById(fairyTale.image_id)
                if (imageEntity != null) {
                    loadImage(imageEntity.image_path)
                }

                val audioPath = attributeJson.optString("audioPath", "")
                _storyState.value = _storyState.value.copy(audioPath = audioPath)

                if (audioPath.isNotEmpty()) {
                    _totalDuration.value = ttsService?.calculateAudioDuration(audioPath) ?: 0
                }

                // 배경음악 경로가 없으면 속성에서 복원 시도
                if (_bgmPath.value.isNullOrBlank()) {
                    val savedBgmPath = attributeJson.optString("bgmPath", "")
                    if (savedBgmPath.isNotEmpty()) {
                        val file = File(savedBgmPath)
                        if (file.exists()) {
                            _bgmPath.value = savedBgmPath
                            Log.d("GeneratedStoryViewModel", "Restored BGM path from attributes: $savedBgmPath")
                        }
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

    private fun extractMfccValues(attributeJson: JSONObject): List<DoubleArray> {
        val mfccValues = mutableListOf<DoubleArray>()
        try {
            if (attributeJson.has("mfccValues")) {
                val mfccArray = attributeJson.getJSONArray("mfccValues")
                for (i in 0 until mfccArray.length()) {
                    val coeffArray = mfccArray.getJSONArray(i)
                    val coeffs = DoubleArray(coeffArray.length())
                    for (j in 0 until coeffArray.length()) {
                        coeffs[j] = coeffArray.getDouble(j)
                    }
                    mfccValues.add(coeffs)
                }
            } else {
                // 기본값 추가
                mfccValues.add(DoubleArray(13) { 0.0 })
            }
        } catch (e: Exception) {
            Log.e("GeneratedStoryViewModel", "Error extracting MFCC values: ${e.message}", e)
            mfccValues.add(DoubleArray(13) { 0.0 })
        }
        return mfccValues
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
                    // 🔥 배경음악도 같이 재생
                    val bgm = _bgmPath.value
                    Log.d("GeneratedStoryViewModel", "🎵 가져온 BGM 경로: $bgm") // ✅ 로그 찍기
                    if (!bgm.isNullOrBlank()) {
                        playBackgroundMusic(bgm)
                    }

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

    fun seekTo(positionMs: Int) {
        viewModelScope.launch {
            ttsService?.seekTo(positionMs)
        }
    }

    private fun startProgressTracking() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                val currentPos = ttsService?.getCurrentPosition() ?: 0
                val total = ttsService?.getTotalDuration() ?: 1
                _playbackProgress.value = if (total > 0) currentPos.toFloat() / total else 0f

                // 전체 재생 시간도 업데이트
                _totalDuration.value = total

                if (_playbackProgress.value >= 1f) {
                    _isPlaying.value = false
                    _playbackProgress.value = 0f
                    break
                }
                kotlinx.coroutines.delay(100) // 100ms마다 업데이트
            }
        }
    }

    // 음성 추천 기능 - 사용 가능한 음성 중 가장 적합한 것을 찾음
    fun recommendVoice(context: Context) {
        viewModelScope.launch {
            try {
                _recommendationState.value = VoiceRecommendationState(isLoading = true)

                val storyFeatures = _storyState.value.voiceFeatures
                if (storyFeatures == null) {
                    Log.e("GeneratedStoryViewModel", "Cannot recommend voice: story features not available")
                    _recommendationState.value = _recommendationState.value.copy(isLoading = false)
                    return@launch
                }

                // VoiceRepository에서 사용 가능한 모든 음성 가져오기
                val voiceRepository = VoiceRepository(context)
                val allVoices = voiceRepository.getAllVoices()

                // 클론된 음성(낭독용 음성)만 필터링
                val cloneVoices = allVoices.filter { voice ->
                    try {
                        val attributeJson = JSONObject(voice.attribute)
                        attributeJson.optBoolean("isClone", false)
                    } catch (e: Exception) {
                        false
                    }
                }

                if (cloneVoices.isEmpty()) {
                    Log.d("GeneratedStoryViewModel", "No clone voices available for recommendation")
                    _recommendationState.value = _recommendationState.value.copy(isLoading = false)
                    return@launch
                }

                // 각 음성에 대해 특성 가져와서 유사도 계산
                val voiceScores = mutableListOf<Pair<VoiceEntity, Double>>()

                for (voice in cloneVoices) {
                    try {
                        val voiceFeatures = voiceRepository.getVoiceFeatures(voice.voice_id)
                        val similarity = voiceFeaturesUtil?.calculateSimilarity(storyFeatures, voiceFeatures) ?: 0.5
                        voiceScores.add(Pair(voice, similarity))
                    } catch (e: Exception) {
                        Log.e("GeneratedStoryViewModel", "Error calculating similarity for voice ${voice.voice_id}: ${e.message}")
                        // 오류 발생 시에도 일단 목록에 추가 (기본 점수 0.3)
                        voiceScores.add(Pair(voice, 0.3))
                    }
                }

                // 유사도가 가장 높은 음성 선택
                val bestMatch = voiceScores.maxByOrNull { it.second }

                if (bestMatch != null) {
                    val similarityPercentage = (bestMatch.second * 100).toInt().coerceIn(0, 100)
                    _recommendationState.value = VoiceRecommendationState(
                        isLoading = false,
                        recommendedVoice = bestMatch.first,
                        similarityPercentage = similarityPercentage
                    )

                    Log.d("GeneratedStoryViewModel", "Recommended voice: ${bestMatch.first.title} with similarity: $similarityPercentage%")
                } else {
                    _recommendationState.value = _recommendationState.value.copy(isLoading = false)
                }

            } catch (e: Exception) {
                Log.e("GeneratedStoryViewModel", "Error recommending voice: ${e.message}", e)
                _recommendationState.value = _recommendationState.value.copy(isLoading = false)
            }
        }
    }

    // 추천된 음성으로 동화 복제
    suspend fun createStoryWithRecommendedVoice(context: Context): Long {
        try {
            Log.d(TAG, "Starting to create story with recommended voice")

            val recommendedVoice = _recommendationState.value.recommendedVoice
                ?: throw IllegalStateException("추천된 음성이 없습니다")

            if (_storyState.value.storyId <= 0) {
                throw IllegalStateException("원본 동화 ID가 유효하지 않습니다")
            }

            _recommendationState.value = _recommendationState.value.copy(cloneCreationInProgress = true)

            // 원본 동화 ID와 텍스트 내용
            val originalStoryId = _storyState.value.storyId
            val storyContent = _storyState.value.storyContent

            // 새 동화 제목 생성 0515 박찬우 변경사항
            val baseTitle = _storyState.value.storyTitle.substringBefore(" (버전:")
            val newTitle = "$baseTitle (버전: 추천된 음성)"

            // 로그 추가 - 추천된 음성 ID 확인
            Log.d(TAG, "Using recommended voice ID: ${recommendedVoice.voice_id}, title: ${recommendedVoice.title}")

            // TTS 서비스 초기화 확인
            if (ttsService == null) {
                ttsService = TTSService(context)
            }

            // 추천된 음성으로 동화 텍스트 읽기 - 여기서 문제가 발생할 수 있음
            val audioData = ttsService?.generateVoice(storyContent, recommendedVoice.voice_id) ?: ByteArray(0)

            if (audioData.isEmpty()) {
                Log.e(TAG, "Failed to generate audio with recommended voice")
                throw IllegalStateException("추천된 음성으로 오디오를 생성할 수 없습니다")
            }

            Log.d(TAG, "Successfully generated audio with recommended voice, size: ${audioData.size} bytes")

            // 새 동화 저장
            val fairyTaleRepository = FairyTaleRepository(context)
            val newStoryId = fairyTaleRepository.createStoryWithRecommendedVoice(
                originalStoryId = originalStoryId,
                recommendedVoiceId = recommendedVoice.voice_id,
                newTitle = newTitle,
                audioData = audioData
            )

            _recommendationState.value = _recommendationState.value.copy(
                cloneCreationInProgress = false,
                newStoryCreated = true,
                newStoryId = newStoryId
            )

            Log.d(TAG, "Created new story with ID: $newStoryId")

            // 생성된 동화 ID 반환
            return newStoryId

        } catch (e: Exception) {
            Log.e(TAG, "Error creating story with recommended voice: ${e.message}", e)
            _recommendationState.value = _recommendationState.value.copy(cloneCreationInProgress = false)
            throw e
        }
    }
    // 추가: 새 동화 생성 완료 상태 초기화
    fun resetNewStoryState() {
        _recommendationState.value = _recommendationState.value.copy(
            newStoryCreated = false,
            newStoryId = -1L
        )
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