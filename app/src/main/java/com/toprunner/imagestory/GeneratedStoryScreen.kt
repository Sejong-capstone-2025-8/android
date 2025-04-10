package com.toprunner.imagestory.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.data.entity.ImageEntity
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.repository.ImageRepository
import com.toprunner.imagestory.repository.TextRepository
import com.toprunner.imagestory.service.TTSService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

@Composable
fun GeneratedStoryScreen(
    storyId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 상태 변수들
    var fairyTaleEntity by remember { mutableStateOf<FairyTaleEntity?>(null) }
    var storyTitle by remember { mutableStateOf("") }
    var storyContent by remember { mutableStateOf("") }
    var storyImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var audioDuration by remember { mutableStateOf("0:00") }
    var currentAudioPath by remember { mutableStateOf<String?>(null) }

    // TTS 서비스
    val ttsService = remember { TTSService(context) }

    // 동화 로드
    LaunchedEffect(storyId) {
        loadStory(
            context = context,
            storyId = storyId,
            onStoryLoaded = { entity, content, bitmap, audioPath ->
                fairyTaleEntity = entity
                storyTitle = entity.title
                storyContent = content
                storyImage = bitmap
                currentAudioPath = audioPath
                isLoading = false
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    // 재생 상태 업데이트
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                playbackProgress = ttsService.getPlaybackProgress()
                // 재생이 끝나면 초기화
                if (playbackProgress >= 1f) {
                    isPlaying = false
                    playbackProgress = 0f
                }
                delay(100) // 100ms마다 업데이트
            }
        }
    }

    // 오디오 재생 기능
    val playStoryAudio = {
        currentAudioPath?.let { path ->
            if (path.isEmpty()) {
                Toast.makeText(context, "오디오 파일이 없습니다.", Toast.LENGTH_SHORT).show()
                return@let
            }

            val success = if (isPlaying) {
                ttsService.resumeAudio()
            } else {
                ttsService.playAudio(path)
            }

            if (success) {
                isPlaying = true
                updateAudioDurationText(ttsService.getTotalDuration(), audioDuration = { audioDuration = it })
            } else {
                Toast.makeText(context, "음성 재생에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "오디오 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 오디오 일시 정지 기능
    val pauseStoryAudio = {
        val success = ttsService.pauseAudio()
        if (success) {
            isPlaying = false
        }
    }

    // 오디오 정지 기능
    val stopStoryAudio = {
        val success = ttsService.stopAudio()
        if (success) {
            isPlaying = false
            playbackProgress = 0f
        }
    }

    // 목소리 추천 기능
    val recommendVoice = {
        Toast.makeText(context, "동화에 어울리는 목소리를 추천합니다.", Toast.LENGTH_SHORT).show()
        // 실제 구현에서는 알고리즘에 따라 목소리 추천 후 적용
    }

    // 시간 표시 계산
    @SuppressLint("DefaultLocale")
    val progressText = "${(playbackProgress * 100).toInt()}%"

    // 현재 재생 시간 계산
    val totalDurationSec = getTotalDurationInSeconds(audioDuration)
    val currentPositionSeconds = (playbackProgress * totalDurationSec).toInt()
    val currentTimeText = String.format("%d:%02d", currentPositionSeconds / 60, currentPositionSeconds % 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
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
                    .clickable { navController.navigateUp() },
                fontSize = 16.sp,
                color = Color(0xFF9C8A54)
            )
        }

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
                    bitmap = storyImage!!.asImageBitmap(),
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
                text = storyTitle.ifEmpty { "동화 제목" },
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
            onValueChange = { /* 진행바 드래그는 구현하지 않음 */ },
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
                text = "$currentTimeText / $audioDuration",
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
                        if (isPlaying) pauseStoryAudio() else playStoryAudio()
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
                    .clickable(enabled = !isLoading) { stopStoryAudio() },
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
                    .clickable(enabled = !isLoading) {
                        navController.navigate(NavRoute.VoiceList.route)
                    }
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
                    .clickable(enabled = !isLoading) {
                        navController.navigate(NavRoute.MusicList.route)
                    }
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
                onClick = { recommendVoice() },
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

    // 화면 이탈 시 오디오 정지
    DisposableEffect(Unit) {
        onDispose {
            ttsService.stopAudio()
        }
    }
}

// 동화 로드 함수
private suspend fun loadStory(
    context: Context,
    storyId: Long,
    onStoryLoaded: (FairyTaleEntity, String, android.graphics.Bitmap?, String?) -> Unit,
    onError: (String) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val fairyTaleRepository = FairyTaleRepository(context)
            val imageRepository = ImageRepository(context)
            val textRepository = TextRepository(context)

            // 동화 엔티티 로드
            val (story, _) = fairyTaleRepository.getFairyTaleById(storyId)

            // 텍스트 내용 로드
            val textContent = textRepository.getTextById(story.text_id)?.second ?: ""

            // 이미지 로드
            var bitmap: android.graphics.Bitmap? = null
            val imageEntity = imageRepository.getImageById(story.image_id)
            if (imageEntity != null) {
                bitmap = loadImage(imageEntity.image_path)
            }

            // 오디오 파일 경로 로드
            var audioPath: String? = null
            try {
                val attributeJson = JSONObject(story.attribute)
                audioPath = attributeJson.optString("audioPath", "")
            } catch (e: Exception) {
                // JSON 파싱 오류 처리
            }

            withContext(Dispatchers.Main) {
                onStoryLoaded(story, textContent, bitmap, audioPath)
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            onError("동화 로드 오류: ${e.message}")
        }
    }
}

// 이미지 로드 함수
private fun loadImage(imagePath: String): android.graphics.Bitmap? {
    return try {
        val file = File(imagePath)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

// 오디오 시간 업데이트 함수
private fun updateAudioDurationText(totalDurationMs: Int, audioDuration: (String) -> Unit) {
    val minutes = totalDurationMs / 60000
    val seconds = (totalDurationMs % 60000) / 1000
    audioDuration(String.format("%d:%02d", minutes, seconds))
}

// 총 재생 시간을 초 단위로 변환
private fun getTotalDurationInSeconds(durationText: String): Int {
    val parts = durationText.split(":")
    return if (parts.size >= 2) {
        val minutes = parts[0].toIntOrNull() ?: 0
        val seconds = parts[1].toIntOrNull() ?: 0
        minutes * 60 + seconds
    } else {
        0
    }
}