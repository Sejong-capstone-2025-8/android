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
import com.toprunner.imagestory.data.entity.MusicEntity
import com.toprunner.imagestory.repository.MusicRepository
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MusicListActivity : ComponentActivity() {
    private val TAG = "MusicListActivity"
    private val musicRepository by lazy { MusicRepository(this) }
    private var musics by mutableStateOf<List<MusicEntity>>(emptyList())
    private var isLoading by mutableStateOf(true)
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingMusicId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadMusic()

        setContent {
            ImageStoryTheme {
                MusicListScreen(
                    musics = musics,
                    currentPlayingMusicId = currentPlayingMusicId,
                    isLoading = isLoading,
                    onBackClicked = { finish() },
                    onMusicClicked = { musicId ->
                        playMusicSample(musicId)
                    },
                    onAddMusicClicked = {
                        Toast.makeText(this, "음악 추가 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onNavigationItemClicked = { screen ->
                        navigateToScreen(screen)
                    }
                )
            }
        }
    }

    private fun loadMusic() {
        isLoading = true
        lifecycleScope.launch {
            try {
                // 기본 배경 음악 모델 생성 (아직 없는 경우에만)
                createDefaultMusicModels()

                // 모든 음악 모델 로드
                musics = musicRepository.getAllMusic().sortedByDescending { it.created_at }
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading music: ${e.message}", e)
                Toast.makeText(this@MusicListActivity, "음악 목록을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    private suspend fun createDefaultMusicModels() {
        // 기본 음악 모델들 정의
        val defaultMusics = listOf(
            Triple("평화로운 숲", "forest", "peaceful"),
            Triple("신나는 모험", "adventure", "exciting"),
            Triple("잔잔한 피아노", "piano", "calm"),
            Triple("꿈꾸는 동화", "fairy_tale", "dreamy"),
            Triple("우주 여행", "space", "sci-fi")
        )

        // 기존 음악 갯수 확인
        val existingMusics = musicRepository.getAllMusic()

        // 기본 모델이 없는 경우에만 생성
        if (existingMusics.isEmpty()) {
            Log.d(TAG, "Creating default music models")

            // 각 기본 음악 모델 저장
            for ((title, musicType, genre) in defaultMusics) {
                try {
                    // 속성 정보
                    val attributeJson = """
                        {
                            "musicType": "$musicType",
                            "genre": "$genre"
                        }
                    """.trimIndent()

                    // 빈 오디오 파일 (실제로는 assets에서 가져오거나 서버에서 다운로드)
                    val dummyAudioData = ByteArray(0)

                    // DB에 음악 정보 저장
                    musicRepository.saveMusic(
                        title = title,
                        genre = genre,
                        musicData = dummyAudioData
                    )

                    Log.d(TAG, "Created default music: $title")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating default music '$title': ${e.message}", e)
                }
            }
        }
    }

    private fun playMusicSample(musicId: Long) {
        lifecycleScope.launch {
            try {
                // 먼저 현재 재생 중인 오디오 정지
                stopCurrentAudio()

                // 선택한 음악 엔티티 찾기
                val music = musics.find { it.music_id == musicId } ?: return@launch

                // 음악 파일이 존재하는지 확인
                val file = File(music.music_path)
                if (!file.exists() || file.length().toInt() == 0) {
                    // 샘플 오디오 없음 - 생성 필요 (실제로는 API 호출)
                    Toast.makeText(this@MusicListActivity, "샘플 음악이 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 음악 파일 재생
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(music.music_path)
                    prepare()
                    start()
                    setOnCompletionListener {
                        currentPlayingMusicId = null
                        release()
                        mediaPlayer = null
                    }
                }

                // 현재 재생 중인 음악 ID 업데이트
                currentPlayingMusicId = musicId

            } catch (e: Exception) {
                Log.e(TAG, "Error playing music sample: ${e.message}", e)
                Toast.makeText(this@MusicListActivity, "음악 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                currentPlayingMusicId = null
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
        currentPlayingMusicId = null
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
            "voice_list" -> {
                val intent = Intent(this, VoiceListActivity::class.java)
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
fun MusicListScreen(
    musics: List<MusicEntity>,
    currentPlayingMusicId: Long?,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onMusicClicked: (Long) -> Unit,
    onAddMusicClicked: () -> Unit,
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
                text = "음악 리스트",
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

        // 음악 추가 버튼
        Button(
            onClick = { onAddMusicClicked() },
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
                painter = painterResource(id = R.drawable.ic_music),
                contentDescription = "Add Music",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "음악 추가하기",
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

        // 음악 목록
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
            } else if (musics.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_music),
                        contentDescription = "No music",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "저장된 음악이 없습니다.\n새로운 음악을 추가해보세요!",
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
                    items(musics) { music ->
                        MusicItem(
                            music = music,
                            isPlaying = currentPlayingMusicId == music.music_id,
                            onClick = { onMusicClicked(music.music_id) }
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
                    onClick = { onNavigationItemClicked("voice_list") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_music,
                    text = "음악 리스트",
                    tint = iconTint,
                    isSelected = true,
                    onClick = { /* 현재 화면은 클릭 불필요 */ }
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
fun MusicItem(
    music: MusicEntity,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(music.created_at))

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
            // 음악 아이콘 또는 재생중 표시
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_music
                    ),
                    contentDescription = if (isPlaying) "Playing" else "Music",
                    tint = if (isPlaying) Color.Black else Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보 (제목, 장르)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = music.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "장르: ${music.attribute}",
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