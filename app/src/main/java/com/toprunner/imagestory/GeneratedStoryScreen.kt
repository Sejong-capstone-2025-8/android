package com.toprunner.imagestory.screens

import android.annotation.SuppressLint
import android.util.Log
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
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.viewmodel.GeneratedStoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import com.toprunner.imagestory.service.TTSService

@SuppressLint("DefaultLocale")
@Composable
fun GeneratedStoryScreen(
    storyId: Long,
    navController: NavController,
    generatedStoryViewModel: GeneratedStoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    // 현재 낭독 중인 문장의 인덱스
    val context = LocalContext.current
    val ttsService = remember { TTSService(context) }

    // "동화에 어울리는 목소리를 추천합니다" 라는 토스트를 띄우는 람다
    val recommendVoice: () -> Unit = {
        Toast.makeText(context, "동화에 어울리는 목소리를 추천합니다.", Toast.LENGTH_SHORT).show()
    }
    val scrollState = rememberScrollState()
    // 로컬 변수 추가
    var localProgress by remember { mutableStateOf(0f) }

    // 뷰모델 상태 구독
    val storyState by generatedStoryViewModel.storyState.collectAsState()
    val isPlaying by generatedStoryViewModel.isPlaying.collectAsState()
    val playbackProgress by generatedStoryViewModel.playbackProgress.collectAsState()
    val totalDuration by generatedStoryViewModel.totalDuration.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidthDp = with(LocalDensity.current) {
        configuration.screenWidthDp.dp
    }

    var currentSentenceIndex by remember { mutableStateOf(-1) }

    // 동화 내용을 문장 단위로 분리 - 더 정확한 정규식 패턴 사용
    val storySentences = remember(storyState.storyContent) {
        storyState.storyContent
            .split(Regex("(?<=[.!?]\\s)|(?<=[.!?]$)"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }


    // 재생 상태 업데이트 LaunchedEffect 수정
    LaunchedEffect(isPlaying) {
        if (isPlaying && storySentences.isNotEmpty()) {
            // 1. 문장 길이 기반 누적 범위 계산 (1회만 계산)
            val totalLength = storySentences.sumOf { it.length }
            val sentenceRanges = buildList {
                var cumulative = 0
                for (s in storySentences) {
                    val start = cumulative.toFloat() / totalLength
                    cumulative += s.length
                    val end = cumulative.toFloat() / totalLength
                    add(start..end)
                }
            }

            // 로컬에서는 ViewModel의 진행률을 관찰하고 문장 인덱스만 업데이트
            while (isPlaying) {
                try {
                    val progress = playbackProgress // ViewModel의 StateFlow에서 이미 수집된 값 사용

                    // 2. progress 값에 해당하는 문장 인덱스 추정 (이전과 다를 때만 업데이트)
                    val index = sentenceRanges.indexOfFirst { range -> progress in range }
                    if (index != -1 && index < storySentences.size && currentSentenceIndex != index) {
                        currentSentenceIndex = index
                    }

                    // 재생이 중지되면 인덱스 초기화
                    if (!isPlaying) {
                        currentSentenceIndex = -1
                    }
                } catch (e: Exception) {
                    Log.e("GeneratedStoryScreen", "Error updating text highlight: ${e.message}")
                }

                delay(100) // 텍스트 강조는 좀 더 긴 간격으로 업데이트
            }
        }
    }

    // 시간 텍스트 계산 (예: progressText 및 현재 재생 시간)
    @SuppressLint("DefaultLocale")
    val progressText = "${(playbackProgress * 100).toInt()}%"
    // totalDuration은 밀리초 단위이므로 그대로 사용
    // 전체 시간이 0이 아닐 때만 계산
    val currentPositionMs = (playbackProgress * totalDuration).toInt()
    val totalDurationMs = totalDuration

// 분:초 형식으로 변환
    val currentMinutes = currentPositionMs / 60000
    val currentSeconds = (currentPositionMs % 60000) / 1000
    val totalMinutes = totalDurationMs / 60000
    val totalSeconds = (totalDurationMs % 60000) / 1000

    val currentTimeText = String.format("%d:%02d", currentMinutes, currentSeconds)
    val audioDurationText = String.format("%d:%02d", totalMinutes, totalSeconds)

    // 동화 로드: 뷰모델에서 loadStory 호출
    LaunchedEffect(storyId) {
        generatedStoryViewModel.loadStory(storyId, context)
    }

    // 슬라이더 onValueChange: 새 위치 계산 (totalDuration은 밀리초 단위)
    val onSliderValueChange: (Float) -> Unit = { newValue ->
        val newPositionMs = (newValue * totalDuration).toInt()
        generatedStoryViewModel.seekTo(newPositionMs)
    }

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
                text = storyState.storyTitle.ifEmpty { "동화 제목" },
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

        // 이미지 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (storyState.storyImage != null && !storyState.isLoading) {
                val imageRatio = storyState.storyImage!!.width.toFloat() / storyState.storyImage!!.height.toFloat()
                // 최대 높이를 250dp로 제한
                val maxHeight = 250.dp
                // 계산된 높이가 최대 높이보다 작은 경우에만 계산된 높이 사용
                val calculatedHeight = screenWidthDp / imageRatio
                val imageHeight = minOf(calculatedHeight, maxHeight)

                Image(
                    bitmap = storyState.storyImage!!.asImageBitmap(),
                    contentDescription = "Story Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
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
                    if (storyState.isLoading) {
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

        // 제목 및 나레이터 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = storyState.storyTitle.ifEmpty { "동화 제목" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "narrated by AI Voice",
                fontSize = 16.sp,
                color = Color(0xFFAA8866),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 슬라이더 (드래그하여 재생 위치 이동)
        Slider(
            value = playbackProgress,
            onValueChange = onSliderValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE9D364),
                activeTrackColor = Color(0xFFE9D364),
                inactiveTrackColor = Color(0xFFE0E0E0)
            ),
            enabled = !storyState.isLoading
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
                text = "$currentTimeText / $audioDurationText",
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
            // 재생/일시정지 버튼: 뷰모델의 toggleAudioPlayback() 호출
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .clickable { generatedStoryViewModel.toggleAudioPlayback() },
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
                    .clickable { generatedStoryViewModel.stopAudio() },
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

        // 목소리 설정 섹션 (버튼 등)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(NavRoute.VoiceList.route) }
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
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(NavRoute.MusicList.route) }
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
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { recommendVoice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE566)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !storyState.isLoading
            ) {
                Text(
                    text = "목소리 추천",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            // 동화 텍스트 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                when {
                    storySentences.isNotEmpty() -> {
                        storySentences.forEachIndexed { index, sentence ->
                            val isCurrent = index == currentSentenceIndex
                            Text(
                                text = sentence,
                                fontSize = 16.sp,
                                color = if (isCurrent) Color(0xFFE9D364) else Color.Black,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 4.dp),
                                lineHeight = 24.sp
                            )
                        }
                    }
                    storyState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE9D364))
                        }
                    }
                    else -> {
                        Text(
                            text = "동화 텍스트가 준비 중입니다...",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (storyState.isLoading) {
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

    DisposableEffect(Unit) {
        onDispose {
            generatedStoryViewModel.stopAudio()
        }
    }
}






// 오디오 시간 업데이트 함수
@SuppressLint("DefaultLocale")
private fun updateAudioDurationText(totalDurationMs: Int, audioDuration: (String) -> Unit) {
    val minutes = totalDurationMs / 60000
    val seconds = (totalDurationMs % 60000) / 1000
    audioDuration(String.format("%d:%02d", minutes, seconds))
}

private fun getTotalDurationInSeconds(durationText: String): Int {
    // durationText는 "m:ss" 형식이라고 가정
    val parts = durationText.split(":")
    return if (parts.size >= 2) {
        val minutes = parts[0].toIntOrNull() ?: 0
        val seconds = parts[1].toIntOrNull() ?: 0
        minutes * 60 + seconds
    } else 0
}