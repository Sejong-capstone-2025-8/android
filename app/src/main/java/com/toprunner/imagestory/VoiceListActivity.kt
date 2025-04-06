package com.toprunner.imagestory

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceListActivity : ComponentActivity() {
    private val TAG = "VoiceListActivity"
    private val voiceRepository by lazy { VoiceRepository(this) }
    private var voices by mutableStateOf<List<VoiceEntity>>(emptyList())
    private var isLoading by mutableStateOf(true)
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingVoiceId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadVoices()

        setContent {
            ImageStoryTheme {
                VoiceListScreen(
                    voices = voices,
                    currentPlayingVoiceId = currentPlayingVoiceId,
                    isLoading = isLoading,
                    onBackClicked = { finish() },
                    onVoiceClicked = { voiceId ->
                        playVoiceSample(voiceId)
                    },
                    onRecordNewVoiceClicked = {
                        navigateToVoiceRecording()
                    },
                    onNavigationItemClicked = { screen ->
                        navigateToScreen(screen)
                    }
                )
            }
        }
    }

    private fun loadVoices() {
        isLoading = true
        lifecycleScope.launch {
            try {
                // 기본 음성 모델을 먼저 생성 (아직 없는 경우에만)
                createDefaultVoiceModels()

                // 모든 음성 모델 로드
                voices = voiceRepository.getAllVoices().sortedByDescending { it.created_at }
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading voices: ${e.message}", e)
                Toast.makeText(this@VoiceListActivity, "목소리 목록을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    private suspend fun createDefaultVoiceModels() {
        // 기본 음성 모델들 정의
        val defaultVoices = listOf(
            Triple("Rachel (여성)", "rachel", "21m00Tcm4TlvDq8ikWAM"),
            Triple("Antoni (남성)", "antoni", "ErXwobaYiN019PkySvjV"),
            Triple("Domi (여성)", "domi", "AZnzlk1XvdvUeBnXmlld"),
            Triple("Bella (여성)", "bella", "EXAVITQu4vr4xnSDxMaL"),
            Triple("Elli (여성)", "elli", "MF3mGyEYCl7XYWbV9V6O")
        )

        // 기존 목소리 갯수 확인
        val existingVoices = voiceRepository.getAllVoices()

        // 기본 모델이 없는 경우에만 생성
        if (existingVoices.isEmpty()) {
            Log.d(TAG, "Creating default voice models")

            // 각 기본 음성 모델 저장
            for ((title, voiceType, elevenlabsId) in defaultVoices) {
                try {
                    // 기본 음성 특성 생성
                    val voiceFeatures = com.toprunner.imagestory.model.VoiceFeatures(
                        averagePitch = when (voiceType) {
                            "rachel", "domi", "bella", "elli" -> 165.0 // 여성 평균 피치
                            else -> 110.0 // 남성 평균 피치
                        },
                        pitchStdDev = 15.0,
                        mfccValues = listOf(DoubleArray(13) { 0.0 })
                    )

                    // 속성 정보에 elevenlabs ID 포함
                    val attributeJson = """
                        {
                            "voiceType": "$voiceType",
                            "elevenlabsVoiceId": "$elevenlabsId"
                        }
                    """.trimIndent()

                    // 빈 오디오 파일 (실제로는 TTS API에서 샘플을 생성하겠지만 여기서는 생략)
                    val dummyAudioData = ByteArray(0)

                    // DB에 음성 정보 저장
                    voiceRepository.saveVoice(
                        title = title,
                        attributeJson = attributeJson,
                        audioData = dummyAudioData,
                        voiceFeatures = voiceFeatures
                    )

                    Log.d(TAG, "Created default voice: $title")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating default voice '$title': ${e.message}", e)
                }
            }
        }
    }

    private fun playVoiceSample(voiceId: Long) {
        lifecycleScope.launch {
            try {
                // 먼저 현재 재생 중인 오디오 정지
                stopCurrentAudio()

                // 선택한 음성 엔티티 찾기
                val voice = voices.find { it.voice_id == voiceId } ?: return@launch

                // 음성 파일이 존재하는지 확인
                val file = File(voice.voice_path)
                if (!file.exists() || file.length().toInt() == 0) {
                    // 샘플 오디오 없음 - 생성 필요 (실제로는 API 호출)
                    Toast.makeText(this@VoiceListActivity, "샘플 음성이 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 음성 파일 재생
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(voice.voice_path)
                    prepare()
                    start()
                    setOnCompletionListener {
                        currentPlayingVoiceId = null
                        release()
                        mediaPlayer = null
                    }
                }

                // 현재 재생 중인 음성 ID 업데이트
                currentPlayingVoiceId = voiceId

            } catch (e: Exception) {
                Log.e(TAG, "Error playing voice sample: ${e.message}", e)
                Toast.makeText(this@VoiceListActivity, "음성 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                currentPlayingVoiceId = null
            }
        }
    }

    private fun stopCurrentAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentPlayingVoiceId = null
    }

    private fun navigateToVoiceRecording() {
        val intent = Intent(this, VoiceRecordingActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToScreen(screen: String) {
        when (screen) {
            "home" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            "fairytale_list" -> {
                val intent = Intent(this, FairyTaleListActivity::class.java)
                startActivity(intent)
                finish()
            }
            "music_list" -> {
                val intent = Intent(this, MusicListActivity::class.java)
                startActivity(intent)
                finish()
            }
            "settings" -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> {
                Log.d(TAG, "Unknown screen: $screen")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCurrentAudio()
    }
}

@Composable
fun VoiceListScreen(
    voices: List<VoiceEntity>,
    currentPlayingVoiceId: Long?,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onVoiceClicked: (Long) -> Unit,
    onRecordNewVoiceClicked: () -> Unit,
    onNavigationItemClicked: (String) -> Unit
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "목소리 리스트",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "뒤로",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onBackClicked() },
                fontSize = 16.sp,
                color = Color(0xFF9C8A54)
            )
        }

        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 녹음하기 버튼
        Button(
            onClick = { onRecordNewVoiceClicked() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD166)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = "Record",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "녹음하기",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 목소리 목록
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE9D364)
                    )
                }
            } else if (voices.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "No voices",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "저장된 목소리가 없습니다.\n새로운 목소리를 녹음해보세요!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(voices) { voice ->
                        VoiceItem(
                            voice = voice,
                            isPlaying = currentPlayingVoiceId == voice.voice_id,
                            onClick = { onVoiceClicked(voice.voice_id) }
                        )
                    }
                }
            }
        }

        // 하단 네비게이션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconTint = Color(0xFFAA8866) // 베이지/갈색

                BottomNavItem(
                    iconResId = R.drawable.ic_home,
                    text = "홈화면",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("home") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "동화 리스트",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("fairytale_list") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "목소리 리스트",
                    tint = iconTint,
                    isSelected = true,
                    onClick = { /* 현재 화면은 클릭 불필요 */ }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_music,
                    text = "음악 리스트",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("music_list") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_settings,
                    text = "설정",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("settings") }
                )
            }
        }
    }
}

@Composable
fun VoiceItem(
    voice: VoiceEntity,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(voice.created_at))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 마이크 아이콘 또는 재생중 표시
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_mic
                    ),
                    contentDescription = if (isPlaying) "Playing" else "Voice",
                    tint = if (isPlaying) Color.Black else Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보 (제목, 날짜)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = voice.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // 재생 상태 표시
            if (isPlaying) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_volume_up),
                    contentDescription = "Playing",
                    tint = Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = "Play",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}