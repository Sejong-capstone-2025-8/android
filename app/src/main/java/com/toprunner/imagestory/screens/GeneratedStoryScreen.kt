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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.ui.components.VoiceRecommendationDialog
import com.toprunner.imagestory.viewmodel.FairyTaleViewModel
import com.toprunner.imagestory.viewmodel.GeneratedStoryViewModel
import com.toprunner.imagestory.data.entity.VoiceEntity
import kotlinx.coroutines.launch
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.service.GPTService
import com.toprunner.imagestory.ui.components.VoiceSelectionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.toprunner.imagestory.ui.components.ChatbotDialog
import com.toprunner.imagestory.ui.components.VoiceConversationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedStoryScreen(
    storyId: Long,
    bgmPath: String? = null,
    navController: NavController,
    generatedStoryViewModel: GeneratedStoryViewModel = viewModel(),
    fairyTaleViewModel: FairyTaleViewModel? = null,
    fairyTaleRepository: FairyTaleRepository
) {
    val usedFallbackVoice by generatedStoryViewModel.usedFallbackVoice.collectAsState()
    var showFallbackDialog by remember { mutableStateOf(false) }
    var handledFallback by remember { mutableStateOf(false) }



    // 상태 값 수집
    val playbackSpeed by generatedStoryViewModel.playbackSpeed.collectAsState()
    val pitch by generatedStoryViewModel.pitch.collectAsState()

    var showVoiceSelectionDialog by remember { mutableStateOf(false) }
    val voiceListState by generatedStoryViewModel.voiceListState.collectAsState()

    val context = LocalContext.current
    var isCreatingRecommendedVoice by remember { mutableStateOf(false) }

    LaunchedEffect(storyId, bgmPath) {
        generatedStoryViewModel.loadStory(storyId, context, bgmPath)
    }
    LaunchedEffect(usedFallbackVoice) {
        Log.d("GeneratedStoryScreen", "usedFallbackVoice: $usedFallbackVoice, handled: $handledFallback")
        if (usedFallbackVoice && !handledFallback) {
            showFallbackDialog = true
            handledFallback = true  // 처리 표시
        }
    }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // FairyTaleViewModel 생성 (만약 전달받지 않았다면)
    val localFairyTaleViewModel = fairyTaleViewModel ?: remember {
        FairyTaleViewModel(FairyTaleRepository(context))
    }
    var userMessage by remember { mutableStateOf("") }
    var chatbotResponse by remember { mutableStateOf("") }
    var showChatbotDialog by remember { mutableStateOf(false) }
    var storyContent by remember { mutableStateOf("") } // 동화 내용 저장용
    val conversationHistory = remember { mutableStateListOf<String>() }  // 대화 내역 저장

    // GPTService 인스턴스 생성
    val gptService = GPTService()
    // 챗봇과 대화하는 함수
    fun handleChatbot(onComplete: () -> Unit) {
        if (userMessage.isBlank()) {
            // 빈 메시지인 경우 바로 로딩 종료 콜백
            onComplete()
            return
        }
        if (userMessage.isNotBlank()) {
            // 대화 내역에 사용자 메시지 추가
            conversationHistory.add("나: $userMessage")
            // GPT API 호출하여 답변을 받아옴
            CoroutineScope(Dispatchers.IO).launch {
                val response = gptService.chatWithBot(userMessage,conversationHistory,storyContent)
                // 챗봇의 응답을 대화 내역에 추가
                conversationHistory.add("동화 챗봇: $response")
                chatbotResponse = response
                onComplete()
            }
        }
    }
    // 다이얼로그를 열 때 인사 추가
    LaunchedEffect(showChatbotDialog) {
        if (showChatbotDialog && conversationHistory.isEmpty()) {
            conversationHistory.add("동화 챗봇: 안녕하세요! 무엇을 도와드릴까요?")
        }
    }

    var isLoading by remember { mutableStateOf(false) }

    // 상태 변수 추가
    var showVoiceConversationDialog by remember { mutableStateOf(false) }

// 기존 ChatbotDialog 수정
    if (showChatbotDialog) {
        ChatbotDialog(
            conversationHistory = conversationHistory,
            userMessage = userMessage,
            onMessageChange = { userMessage = it },
            isLoading = isLoading,
            onSend = {
                isLoading = true
                handleChatbot {
                    isLoading = false
                }
            },
            onDismiss = { showChatbotDialog = false },
            onSpeakResponse = { responseText ->
                // 필요시 추가 처리
            },
            onStartVoiceConversation = {
                showChatbotDialog = false
                showVoiceConversationDialog = true
            }
        )
    }

// 새로운 음성 대화 다이얼로그 추가
    if (showVoiceConversationDialog) {
        VoiceConversationDialog(
            onDismiss = { showVoiceConversationDialog = false },
            onSendMessage = { message ->
                // handleChatbot와 동일한 로직으로 메시지 처리
                conversationHistory.add("나: $message")
                scope.launch {
                    try {
                        val response = gptService.chatWithBot(message, conversationHistory, storyContent)
                        conversationHistory.add("동화 챗봇: $response")
                        // 음성 대화 다이얼로그에 응답 전달하는 방법이 필요함
                    } catch (e: Exception) {
                        Log.e("GeneratedStoryScreen", "음성 대화 오류: ${e.message}")
                    }
                }
            },
            onReceiveResponse = { response ->
                // 응답 받았을 때 처리
                Log.d("GeneratedStoryScreen", "음성 응답 받음: $response")
            },
            storyContent = storyContent,
            gptService = gptService, // GPTService 전달
            conversationHistory = conversationHistory // 대화 기록 전달
        )
    }
    if (showVoiceConversationDialog) {
        VoiceConversationDialog(
            onDismiss = { showVoiceConversationDialog = false },
            onSendMessage = { message ->
                // 실제로는 VoiceConversationDialog 내부에서 처리하므로
                // 여기서는 특별한 처리가 필요 없음
                Log.d("GeneratedStoryScreen", "음성 메시지 전송: $message")
            },
            onReceiveResponse = { response ->
                // 응답 받았을 때 처리
                Log.d("GeneratedStoryScreen", "음성 응답 받음: $response")
            },
            storyContent = storyContent,
            gptService = gptService, // GPTService 전달
            conversationHistory = conversationHistory // 대화 기록 전달
        )
    }

    if (showFallbackDialog) {
        AlertDialog(
            onDismissRequest = {
                showFallbackDialog = false
                // 뷰모델 상태도 초기화 (옵션)
                generatedStoryViewModel.clearFallbackState()
            },
            title = {
                Text(
                    "음성 변경 알림",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F2E20)
                )
            },
            text = {
                Text(
                    "Elevenlabs 서버에 음성이 저장되어 있지 않습니다. 기본 음성으로 동화가 생성됩니다.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF5F4C40)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFallbackDialog = false
                        // 뷰모델 상태 초기화 (옵션)
                        generatedStoryViewModel.clearFallbackState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9D364))
                ) {
                    Text(
                        "확인",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color(0xFFFFFBF0),
            shape = RoundedCornerShape(16.dp)
        )
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
    fun handleVoiceSelection() {
        // 낭독용 음성 목록 로드
        generatedStoryViewModel.loadCloneVoices(context)
        // 다이얼로그 표시
        showVoiceSelectionDialog = true
    }

    fun createStoryWithSelectedVoice(selectedVoice: VoiceEntity) {
        scope.launch {
            try {
                // 로딩 상태 설정
                isCreatingRecommendedVoice = true

                // 다이얼로그 닫기
                showVoiceSelectionDialog = false

                // FairyTaleViewModel의 로딩 상태 활성화
                localFairyTaleViewModel.startCreatingRecommendedVoiceStory()

                // 뷰모델을 통해 새 동화 생성
                val newStoryId = generatedStoryViewModel.createStoryWithSelectedVoice(context, selectedVoice)

                // 성공 메시지
                Toast.makeText(
                    context,
                    "선택한 음성으로 동화가 생성되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                // 동화 목록 갱신
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()

                // 동화 목록 화면으로 이동
                navController.navigate(NavRoute.FairyTaleList.route) {
                    popUpTo(NavRoute.FairyTaleList.route) { inclusive = true }
                }

            } catch (e: Exception) {
                // 오류 처리
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()
                isCreatingRecommendedVoice = false
                Toast.makeText(
                    context,
                    "동화 생성 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // 항상 로딩 상태 비활성화
                isCreatingRecommendedVoice = false
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
                // 로딩 상태 설정
                isCreatingRecommendedVoice = true

                // 추천 다이얼로그 닫기 (이미 닫혔을 수도 있음)
                showRecommendationDialog = false

                // FairyTaleViewModel의 로딩 상태 활성화
                localFairyTaleViewModel.startCreatingRecommendedVoiceStory()

                // 중요: 실제 동화 생성 전에 화면 전환이 발생하지 않도록 함
                // 뷰모델을 통해 새 동화 생성 - 여기서 실제 비동기 작업 발생
                val newStoryId = generatedStoryViewModel.createStoryWithRecommendedVoice(context)

                // 성공 메시지
                Toast.makeText(
                    context,
                    "추천된 음성으로 동화가 생성되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                // 동화 목록 갱신
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()

                // 실제 동화 생성이 완료된 후에만 화면 전환
                navController.navigate(NavRoute.FairyTaleList.route) {
                    popUpTo(NavRoute.FairyTaleList.route) { inclusive = true }
                }

            } catch (e: Exception) {
                // 오류 처리
                localFairyTaleViewModel.finishCreatingRecommendedVoiceStory()
                isCreatingRecommendedVoice = false
                Toast.makeText(
                    context,
                    "동화 생성 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // 항상 로딩 상태 비활성화
                isCreatingRecommendedVoice = false
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

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBF0))
        ) {
            // 상단 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Image Story",
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "narrated by AI Voice",
                    fontSize = 14.sp,
                    color = Color(0xFFAA8866),
                    modifier = Modifier.padding(top = 2.dp)
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
                    .height(16.dp),
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
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 재생/일시정지 버튼
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
                        modifier = Modifier.size(22.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // BGM 컨트롤 버튼
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            // 속도 및 피치 조절 슬라이더 - 가로로 나란히 배치
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 속도 조절 슬라이더
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "낭독 속도: ${String.format("%.1f", playbackSpeed)}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )

                    Slider(
                        value = playbackSpeed,
                        onValueChange = { generatedStoryViewModel.setPlaybackSpeed(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 15, // 0.1 단위로 조절 가능: (2.0-0.5)/0.1 = 15
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(top = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFAA8866),
                            activeTrackColor = Color(0xFF3D5AFE),
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.7f)
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFC000))
                            )
                        },
                        track = { state ->
                            // 커스텀 트랙 (더 두껍게 설정)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp) // 트랙 높이를 8dp로 설정
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                // 비활성 부분
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(Color.LightGray.copy(alpha = 0.7f))
                                )
                                val normalizedValue = (state.value - 0.5f) / (2.0f - 0.5f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(normalizedValue)
                                        .height(8.dp)
                                        .background(Color(0xFF3D5AFE))
                                )
                            }
                        }
                    )
                }

                // 피치 조절 슬라이더
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "음의 높낮이: ${String.format("%.1f", pitch)}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )

                    Slider(
                        value = pitch,
                        onValueChange = { generatedStoryViewModel.setPitch(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 15, // 0.1 단위로 조절 가능: (2.0-0.5)/0.1 = 15
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(top = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFAA8866),
                            activeTrackColor = Color(0xFF3D5AFE),
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.7f)
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFC000))
                            )
                        },
                        track = { state ->
                            // 커스텀 트랙 (더 두껍게 설정)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp) // 트랙 높이를 8dp로 설정
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                // 비활성 부분
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(Color.LightGray.copy(alpha = 0.7f))
                                )
                                // 활성 부분
                                val normalizedValue = (state.value - 0.5f) / (2.0f - 0.5f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(normalizedValue)
                                        .height(8.dp)
                                        .background(Color(0xFF3D5AFE))
                                )
                            }
                        }
                    )
                }
                // BGM 음량 조절 슬라이더
                Column(
                    modifier = Modifier.weight(1f)

                ) {
                    Text(
                        text = "배경음 음량 조절",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    // 커스텀 슬라이더 크기 설정
                    Slider(
                        value = bgmVolume,
                        onValueChange = { newVolume ->
                            generatedStoryViewModel.setBackgroundMusicVolume(newVolume)
                        },
                        valueRange = 0f..1f,
                        steps = 8,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(8.dp)
                            .padding(top = 8.dp),
                        // 슬라이더 커스텀 설정
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFAA8866),
                            activeTrackColor = Color(0xFF3D5AFE),
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.7f)
                        ),
                        // 트랙과 썸 크기 조정을 위한 설정
                        thumb = {
                            // 커스텀 썸 (더 크게 설정)
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFC000))
                            )
                        },
                        track = { state ->
                            // 커스텀 트랙 (더 두껍게 설정)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp) // 트랙 높이를 8dp로 설정
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                // 비활성 부분
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(Color.LightGray.copy(alpha = 0.7f))
                                )
                                // 활성 부분
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(state.value)
                                        .height(8.dp)
                                        .background(Color(0xFF3D5AFE))
                                )
                            }
                        }
                    )

                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            // 기능 버튼 영역 (목소리 선택, 배경음 설정, 목소리 추천)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                NeuomorphicButton(
                    onClick = { handleVoiceSelection() },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    backgroundColor = Color(0xFFFEE566),
                    cornerRadius = 8.dp,
                    elevation = 4.dp
                ) {

                    Text(
                        text = "목소리 선택",
                        fontWeight = FontWeight.Bold,

                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))


                NeuomorphicButton(
                    onClick = { handleVoiceRecommendation() },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    backgroundColor = Color(0xFFFEE566),
                    cornerRadius = 8.dp,
                    elevation = 4.dp
                ) {
                    Text(
                        text = "목소리 추천",
                        fontWeight = FontWeight.Bold,

                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                NeuomorphicButton(
                    onClick = { navController.navigate(NavRoute.MusicList.routeWithArgs(storyId)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    backgroundColor = Color(0xFFFEE566),
                    cornerRadius = 8.dp,
                    elevation = 4.dp
                ) {
                    Text(
                        text = "배경음 설정",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

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
                                    sentencePositions[index] =
                                        coordinates.positionInParent().y.toInt()
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
        LaunchedEffect(storyId) {
            // 동화 내용 불러오기
            val (fairyTaleEntity, content) = fairyTaleRepository.getFairyTaleById(storyId)
            storyContent = content  // content 부분을 storyContent에 할당
        }

        //Box 위에 겹쳐서 띄우는 FloatingActionButton
        FloatingActionButton(
            onClick = { showChatbotDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = Color(0xFFE9D364)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.chatbot_image),
                contentDescription = "챗봇",
                modifier = Modifier.size(36.dp),
                tint = Color.Unspecified
            )
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
                    text = "동화를 생성하는 중입니다...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }

    if (isCreatingRecommendedVoice) {
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
                    text = "동화를 생성하는 중입니다...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
    if (showVoiceSelectionDialog) {
        VoiceSelectionDialog(
            voices = voiceListState.voices,
            isLoading = voiceListState.isLoading,
            onDismiss = { showVoiceSelectionDialog = false },
            onSelectVoice = { selectedVoice ->
                createStoryWithSelectedVoice(selectedVoice)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            generatedStoryViewModel.stopAudio()
        }
    }
}