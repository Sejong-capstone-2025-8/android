package com.toprunner.imagestory

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
import com.toprunner.imagestory.ui.components.VoiceRecommendationDialog
import com.toprunner.imagestory.viewmodel.FairyTaleViewModel
import com.toprunner.imagestory.GeneratedStoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.model.VoiceFeatures
import kotlinx.coroutines.delay

@Composable
fun GeneratedStoryScreen(
    storyId: Long,
    bgmPath: String? = null,
    navController: NavController,
    generatedStoryViewModel: GeneratedStoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    fairyTaleViewModel: FairyTaleViewModel? = null
) {
    val context = LocalContext.current

    LaunchedEffect(storyId, bgmPath) {
        generatedStoryViewModel.loadStory(storyId, context, bgmPath)
    }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // FairyTaleViewModel 생성 (만약 전달받지 않았다면)
    val localFairyTaleViewModel = fairyTaleViewModel ?: remember {
        FairyTaleViewModel(FairyTaleRepository(context))
    }

    // 뷰모델 상태 구독
    val storyState by generatedStoryViewModel.storyState.collectAsState()
    val isPlaying by generatedStoryViewModel.isPlaying.collectAsState()
    val playbackProgress by generatedStoryViewModel.playbackProgress.collectAsState()
    val totalDuration by generatedStoryViewModel.totalDuration.collectAsState()

    // 음성 추천 관련 상태
    val recommendationState by generatedStoryViewModel.recommendationState.collectAsState()
    var showRecommendationDialog by remember { mutableStateOf(false) }

    // BGM 관련 상태 구독
    val isBgmPlaying by generatedStoryViewModel.isBgmPlaying.collectAsState()
    val bgmVolume by generatedStoryViewModel.bgmVolume.collectAsState()
    val bgmPath by generatedStoryViewModel.bgmPath.collectAsState()

    // 시간 텍스트 계산
    @SuppressLint("DefaultLocale")
    val progressText = "${(playbackProgress * 100).toInt()}%"
    val currentPositionSeconds = (playbackProgress * totalDuration / 1000).toInt()
    val totalDurationSeconds = (totalDuration / 1000)
    val currentTimeText = String.format("%d:%02d", currentPositionSeconds / 60, currentPositionSeconds % 60)
    val audioDurationText = String.format("%d:%02d", totalDurationSeconds / 60, totalDurationSeconds % 60)

    // 동화 내용을 문장 단위로 분리
    val storySentences = remember(storyState.storyContent) {
        storyState.storyContent
            .split(Regex("(?<=[.!?]\\s)|(?<=[.!?]$)"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }

    // 현재 문장 인덱스
    var currentSentenceIndex by remember { mutableStateOf(-1) }

    // 재생 상태에 따른 현재 문장 하이라이트
    LaunchedEffect(isPlaying) {
        if (isPlaying && storySentences.isNotEmpty()) {
            // 문장 길이 기반 누적 범위 계산
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

            // 재생 진행에 따른 현재 문장 업데이트
            while (isPlaying) {
                try {
                    val progress = playbackProgress
                    val index = sentenceRanges.indexOfFirst { range -> progress in range }
                    if (index != -1 && index < storySentences.size && currentSentenceIndex != index) {
                        currentSentenceIndex = index
                    }

                    if (!isPlaying) {
                        currentSentenceIndex = -1
                    }
                } catch (e: Exception) {
                    Log.e("GeneratedStoryScreen", "Error updating text highlight: ${e.message}")
                }

                delay(100)
            }
        }
    }

    // 음성 추천 함수
    fun handleVoiceRecommendation() {
        // 추천 다이얼로그 표시 및 뷰모델에 추천 요청
        showRecommendationDialog = true
        generatedStoryViewModel.recommendVoice(context)
    }

    // 추천된 음성으로 새 동화 생성 함수
    fun createStoryWithRecommendedVoice() {
        scope.launch {
            try {
                // 동화 생성 시작 상태 설정 (FairyTaleViewModel에서 로딩 표시용)
                localFairyTaleViewModel.startCreatingRecommendedVoiceStory()

                // 뷰모델을 통해 새 동화 생성
                generatedStoryViewModel.createStoryWithRecommendedVoice(context)

                // 성공 메시지
                Toast.makeText(
                    context,
                    "추천된 음성으로 동화가 생성되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                // 동화 목록 갱신
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()

                // 생성 성공 시 동화 목록 화면으로 이동
                navController.navigate(NavRoute.FairyTaleList.route) {
                    popUpTo(NavRoute.FairyTaleList.route) { inclusive = true }
                }

            } catch (e: Exception) {
                // 오류 처리
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()
                Toast.makeText(
                    context,
                    "동화 생성 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 새 동화 생성 완료 시 네비게이션 처리
    LaunchedEffect(recommendationState.newStoryCreated) {
        if (recommendationState.newStoryCreated && recommendationState.newStoryId > 0) {
            // 상태 초기화
            generatedStoryViewModel.resetNewStoryState()

            // 동화 목록 갱신
            localFairyTaleViewModel.loadFairyTales()

            // 동화 목록 화면으로 네비게이션
            navController.navigate(NavRoute.FairyTaleList.route) {
                popUpTo(NavRoute.FairyTaleList.route) { inclusive = true }
            }
        }
    }

    // 동화 로드
    LaunchedEffect(storyId) {
        generatedStoryViewModel.loadStory(storyId, context)
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
            onValueChange = { newValue ->
                val newPositionMs = (newValue * totalDuration).toInt()
                generatedStoryViewModel.seekTo(newPositionMs)
            },
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
            // 재생/일시정지 버튼
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

            Spacer(modifier = Modifier.width(24.dp))

            // BGM 컨트롤 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9ED8D8))
                    .clickable {
                        Log.d("GeneratedStoryScreen", "배경음 버튼 클릭")
                        generatedStoryViewModel.toggleBackgroundMusic()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isBgmPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = if (isBgmPlaying) "BGM Pause" else "BGM Play",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // BGM 음량 조절 슬라이더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "배경음 음량 조절",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Slider(
                value = bgmVolume,
                onValueChange = { newVolume ->
                    generatedStoryViewModel.setBackgroundMusicVolume(newVolume)
                },
                valueRange = 0f..1f,
                steps = 8, // 10단계 정도로 나눔 (0.0 ~ 1.0)
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFAA8866),
                    activeTrackColor = Color(0xFFAA8866)
                )
            )
        }

        // 기능 버튼 영역 (목소리 선택, 배경음 설정, 목소리 추천)
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
                onClick = { navController.navigate(NavRoute.MusicList.routeWithArgs(storyId)) },
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
                onClick = { handleVoiceRecommendation() },
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

        // 동화 텍스트 영역
        val sentencePositions = remember { mutableStateMapOf<Int, Int>() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
        ) {
            if (storySentences.isNotEmpty()) {
                storySentences.forEachIndexed { index, sentence ->
                    val isCurrent = index == currentSentenceIndex
                    Text(
                        text = sentence,
                        fontSize = 16.sp,
                        color = if (isCurrent) Color(0xFFE9D364) else Color.Black,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .onGloballyPositioned { coordinates ->
                                sentencePositions[index] = coordinates.positionInParent().y.toInt()
                            },
                        lineHeight = 24.sp
                    )
                }

                // 현재 문장으로 자동 스크롤
                LaunchedEffect(currentSentenceIndex) {
                    sentencePositions[currentSentenceIndex]?.let { y ->
                        scrollState.animateScrollTo(y)
                    }
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

    // 음성 추천 다이얼로그
    if (showRecommendationDialog) {
        VoiceRecommendationDialog(
            isLoading = recommendationState.isLoading || recommendationState.cloneCreationInProgress,
            storyFeatures = storyState.voiceFeatures ?: VoiceFeatures(
                averagePitch = 150.0,
                pitchStdDev = 15.0,
                mfccValues = listOf(DoubleArray(13) { 0.0 })
            ),
            recommendedVoice = recommendationState.recommendedVoice,
            similarityPercentage = recommendationState.similarityPercentage,
            onDismiss = { showRecommendationDialog = false },
            onUseRecommendedVoice = {
                showRecommendationDialog = false
                createStoryWithRecommendedVoice()
            }
        )
    }

    // 전체 로딩 표시
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

    // 동화 목록 화면에서 로딩 상태 표시
    val isCreatingNewStory by localFairyTaleViewModel.isCreatingNewStory.collectAsState()
    if (isCreatingNewStory) {
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
                    text = "추천된 음성으로 동화를 생성하는 중입니다...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
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