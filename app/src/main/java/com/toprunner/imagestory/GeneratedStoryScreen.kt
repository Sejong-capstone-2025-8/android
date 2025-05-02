package com.toprunner.imagestory.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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

@Composable
fun GeneratedStoryScreen(
    storyId: Long,
    navController: NavController,
    generatedStoryViewModel: GeneratedStoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    // "동화에 어울리는 목소리를 추천합니다" 라는 토스트를 띄우는 람다
    val recommendVoice: () -> Unit = {
        Toast.makeText(context, "동화에 어울리는 목소리를 추천합니다.", Toast.LENGTH_SHORT).show()
    }
    val scrollState = rememberScrollState()

    // 뷰모델 상태 구독
    val storyState by generatedStoryViewModel.storyState.collectAsState()
    val isPlaying by generatedStoryViewModel.isPlaying.collectAsState()
    val playbackProgress by generatedStoryViewModel.playbackProgress.collectAsState()
    val totalDuration by generatedStoryViewModel.totalDuration.collectAsState()

    // 시간 텍스트 계산 (예: progressText 및 현재 재생 시간)
    @SuppressLint("DefaultLocale")
    val progressText = "${(playbackProgress * 100).toInt()}%"
    // totalDuration은 밀리초 단위이므로 그대로 사용
    val currentPositionSeconds = (playbackProgress * totalDuration / 1000).toInt()
    val totalDurationSeconds = (totalDuration / 1000)
    val currentTimeText = String.format("%d:%02d", currentPositionSeconds / 60, currentPositionSeconds % 60)
    val audioDurationText = String.format("%d:%02d", totalDurationSeconds / 60, totalDurationSeconds % 60)

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
                Image(
                    bitmap = storyState.storyImage!!.asImageBitmap(),
                    contentDescription = "Story Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                .padding(horizontal = 8.dp)
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
                    .size(36.dp)
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
                    .size(36.dp)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.navigate(NavRoute.VoiceList.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .padding(end = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566))
            ) {
                Text(
                    text = "목소리 선택",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }

            Button(
                onClick = { navController.navigate(NavRoute.MusicList.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .padding(horizontal = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566))
            ) {
                Text(
                    text = "배경음 설정",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }

            Button(
                onClick = { recommendVoice() },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .padding(start = 2.dp),
                enabled = !storyState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566))
            ) {
                Text(
                    text = "목소리 추천",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }


        val sentencePositions = remember { mutableStateMapOf<Int, Int>() }

        // 동화 텍스트 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
        ) {
            if (storyState.storyContent.isNotEmpty()) {
                val sentenceRegex = Regex("(?<=[.!?])\\s+")

                val storyLines = remember(storyState.storyContent) {
                    storyState.storyContent.split(sentenceRegex).filter { it.isNotBlank() }
                }

                // 전체 텍스트 길이
                val totalLength = remember(storyLines) {
                    storyLines.sumOf { it.length }
                }

                // 누적 비율 리스트 생성
                val cumulativeRatios = remember(storyLines) {
                    val list = mutableListOf<Float>()
                    var cumulative = 0f
                    for (line in storyLines) {
                        cumulative += line.length
                        list.add(cumulative / totalLength)
                    }
                    list
                }

                // 현재 문장 인덱스 계산
                val currentSentenceIndex = cumulativeRatios.indexOfFirst { it >= playbackProgress }

                // 오토 스크롤 트리거
                LaunchedEffect(currentSentenceIndex) {
                    sentencePositions[currentSentenceIndex]?.let { y ->
                        scrollState.animateScrollTo(y)
                    }
                }

                storyLines.forEachIndexed { index, line ->
                    val isCurrent = index == currentSentenceIndex
                    Text(
                        text = line,
                        fontSize = 16.sp,
                        color = if (isCurrent) Color(0xFFFFC107) else Color.Black,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .onGloballyPositioned { coordinates ->
                                sentencePositions[index] = coordinates.positionInParent().y.toInt()
                            },
                        lineHeight = 24.sp
                    )
                }
            } else if (storyState.isLoading) {
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
                Text(
                    text = "동화 텍스트가 준비 중입니다...",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
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