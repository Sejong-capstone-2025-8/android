package com.toprunner.imagestory

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.data.entity.ImageEntity
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.service.TTSService
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class GeneratedStoryActivity : ComponentActivity() {
    private val TAG = "GeneratedStoryActivity"
    private val fairyTaleRepository by lazy { FairyTaleRepository(this) }
    private val imageRepository by lazy { ImageRepository(this) }
    private val textRepository by lazy { TextRepository(this) }
    private val ttsService by lazy { TTSService(this) }

    private var fairyTaleId: Long = -1
    private var fairyTaleEntity by mutableStateOf<FairyTaleEntity?>(null)
    private var storyTitle by mutableStateOf<String>("")
    private var storyContent by mutableStateOf<String>("")
    private var storyImage by mutableStateOf<android.graphics.Bitmap?>(null)
    private var isPlaying by mutableStateOf(false)
    private var isLoading by mutableStateOf(true)
    private var playbackProgress by mutableStateOf(0f)
    private var audioDuration by mutableStateOf("0:00")
    private var currentAudioPath by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 전달된 동화 ID 가져오기
        fairyTaleId = intent.getLongExtra("STORY_ID", -1)

        if (fairyTaleId == -1L) {
            Toast.makeText(this, "동화 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 동화 로드
        loadStory(fairyTaleId)

        // 재생 위치 업데이트를 위한 코루틴
        lifecycleScope.launch {
            while (isActive) {
                if (isPlaying) {
                    updatePlaybackProgress()
                }
                delay(100) // 100ms마다 업데이트
            }
        }

        setContent {
            ImageStoryTheme {
                GeneratedStoryScreen(
                    storyTitle = storyTitle,
                    storyContent = storyContent,
                    storyImage = storyImage,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    playbackProgress = playbackProgress,
                    audioDuration = audioDuration,
                    onPlayClicked = { playStoryAudio() },
                    onPauseClicked = { pauseStoryAudio() },
                    onStopClicked = { stopStoryAudio() },
                    onVoiceSelectClicked = { navigateToScreen("voice_select") },
                    onMusicSelectClicked = { navigateToScreen("music_select") },
                    onVoiceRecommendClicked = { recommendVoice() },
                    onBackClicked = { finish() },
                    onNavigationItemClicked = { screen -> navigateToScreen(screen) }
                )
            }
        }
    }

    private fun loadStory(fairyTaleId: Long) {
        Log.d(TAG, "Loading story with ID: $fairyTaleId")
        lifecycleScope.launch {
            try {
                isLoading = true

                // 동화 엔티티 로드
                val (story, _) = fairyTaleRepository.getFairyTaleById(fairyTaleId)
                fairyTaleEntity = story
                storyTitle = story.title
                Log.d(TAG, "Story title: $storyTitle")

                // 텍스트 내용 로드
                val textEntity = textRepository.getTextById(story.text_id)
                if (textEntity != null) {
                    storyContent = textEntity.second
                    Log.d(TAG, "Story content loaded, length: ${storyContent.length}")
                }

                // 이미지 로드
                val imageEntity = imageRepository.getImageById(story.image_id)
                if (imageEntity != null) {
                    loadImage(imageEntity)
                }

                // 오디오 파일 경로 로드
                try {
                    val attributeJson = JSONObject(story.attribute)
                    currentAudioPath = attributeJson.optString("audioPath", "")
                    Log.d(TAG, "Audio path: $currentAudioPath")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing attribute JSON: ${e.message}", e)
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(this@GeneratedStoryActivity, "동화 로드 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error loading story", e)
                finish()
            }
        }
    }

    private fun loadImage(imageEntity: ImageEntity) {
        try {
            val file = File(imageEntity.image_path)
            if (file.exists()) {
                storyImage = BitmapFactory.decodeFile(file.absolutePath)
                Log.d(TAG, "Image loaded successfully")
            } else {
                Log.e(TAG, "Image file does not exist: ${imageEntity.image_path}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image: ${e.message}", e)
        }
    }

    private fun playStoryAudio() {
        currentAudioPath?.let { path ->
            if (path.isEmpty()) {
                Toast.makeText(this, "오디오 파일이 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val success = if (isPlaying) {
                ttsService.resumeAudio()
            } else {
                ttsService.playAudio(path)
            }

            if (success) {
                isPlaying = true
                updateAudioDurationText()
            } else {
                Toast.makeText(this, "음성 재생에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "오디오 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseStoryAudio() {
        val success = ttsService.pauseAudio()
        if (success) {
            isPlaying = false
        }
    }

    private fun stopStoryAudio() {
        val success = ttsService.stopAudio()
        if (success) {
            isPlaying = false
            playbackProgress = 0f
        }
    }

    private fun updatePlaybackProgress() {
        playbackProgress = ttsService.getPlaybackProgress()
        if (playbackProgress >= 1f) {
            // 재생 완료
            isPlaying = false
            playbackProgress = 0f
        }
    }

    private fun updateAudioDurationText() {
        val totalDurationMs = ttsService.getTotalDuration()
        val minutes = totalDurationMs / 60000
        val seconds = (totalDurationMs % 60000) / 1000
        audioDuration = String.format("%d:%02d", minutes, seconds)
    }

    private fun recommendVoice() {
        // 현재는 UI만 구현하고 실제 기능은 구현하지 않음
        Toast.makeText(this, "동화에 어울리는 목소리를 추천합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToScreen(screen: String) {
        when (screen) {
            "home" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            "fairytale_list" -> {
                val intent = Intent(this, FairyTaleListActivity::class.java)
                startActivity(intent)
                finish()
            }
            "voice_list", "voice_select" -> {
                val intent = Intent(this, VoiceListActivity::class.java)
                startActivity(intent)
            }
            "music_list", "music_select" -> {
                val intent = Intent(this, MusicListActivity::class.java)
                startActivity(intent)
            }
            "settings" -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> Log.d(TAG, "Unknown screen: $screen")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsService.stopAudio()
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun GeneratedStoryScreen(
    storyTitle: String,
    storyContent: String,
    storyImage: android.graphics.Bitmap?,
    isPlaying: Boolean,
    isLoading: Boolean,
    playbackProgress: Float,
    audioDuration: String,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onVoiceSelectClicked: () -> Unit,
    onMusicSelectClicked: () -> Unit,
    onVoiceRecommendClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onNavigationItemClicked: (String) -> Unit
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    val scrollState = rememberScrollState()
    val progressText = "${(playbackProgress * 100).toInt()}%"

    // 표시할 현재 시간 계산
    val currentPositionSeconds = (playbackProgress * audioDuration.split(":").let {
        if (it.size >= 2) (it[0].toIntOrNull() ?: 0) * 60 + (it[1].toIntOrNull() ?: 0) else 0
    }).toInt()
    val currentTimeText = String.format("%d:%02d", currentPositionSeconds / 60, currentPositionSeconds % 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 상단 헤더 (생성된 동화 + back 버튼)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "생성된 동화",
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
                color = Color(0xFF9C8A54) // 올리브/금색
            )
        }

        // 디바이더 라인
        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 이미지 영역 (책 이미지)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (storyImage != null && !isLoading) {
                Image(
                    bitmap = storyImage.asImageBitmap(),
                    contentDescription = "Story Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFE9D364)
                        )
                    } else {
                        Text(
                            text = "이미지 준비 중...",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 동화 제목 및 나레이터 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (storyTitle.isNotEmpty()) storyTitle else "동화 제목",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "narrated by AI Voice",
                fontSize = 16.sp,
                color = Color(0xFFAA8866), // 베이지/갈색
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 오디오 진행 상태 바
        Slider(
            value = playbackProgress,
            onValueChange = { /* 진행바 드래그는 현재 구현하지 않음 */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(20.dp), // 전체 높이를 줄임
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE9D364), // 노란색 썸네일
                activeTrackColor = Color(0xFFE9D364), // 노란색 활성 트랙
                inactiveTrackColor = Color(0xFFE0E0E0) // 회색 비활성 트랙
            ),
            enabled = !isLoading
        )

        // 시간 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progressText,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = currentTimeText, // 재생 중인 시간
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // 재생 컨트롤 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 재생/일시정지 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .clickable(enabled = !isLoading) {
                        if (isPlaying) onPauseClicked() else onPlayClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // 정지 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .clickable(enabled = !isLoading) { onStopClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stop),
                    contentDescription = "Stop",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 목소리 설정 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 목소리 선택 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onVoiceSelectClicked() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "목소리 선택",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }

            // 디바이더 라인
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 배경음 설정 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onMusicSelectClicked() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "배경음 설정",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }

            // 디바이더 라인
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 목소리 추천 버튼 수정 - 더 명확한 버튼 형태로
            Button(
                onClick = { onVoiceRecommendClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5) // 밝은 회색 배경
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                Text(
                    text = "목소리 추천",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // 디바이더 라인
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 동화 텍스트 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                if (storyContent.isNotEmpty()) {
                    // 동화 텍스트를 줄바꿈 기준으로 분리
                    val storyLines = storyContent.split("\n")
                    storyLines.forEach { line ->
                        if (line.isNotEmpty()) {
                            Text(
                                text = line,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 24.sp
                            )
                        }
                    }
                } else if (isLoading) {
                    // 로딩 중 표시
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFE9D364)
                        )
                    }
                } else {
                    // 기본 텍스트 표시
                    Text(
                        text = "동화 텍스트가 준비 중입니다...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 스크롤을 위한 하단 여백
                Spacer(modifier = Modifier.height(80.dp))
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
                    onClick = { onNavigationItemClicked("voice_list") }
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

    // 로딩 표시
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "동화를 불러오는 중입니다...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

