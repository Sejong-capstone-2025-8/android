package com.toprunner.imagestory.ui.components

import android.Manifest
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.service.GPTService
import com.toprunner.imagestory.util.VoiceChatHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

enum class VoiceConversationState {
    WAITING,      // 시작 대기
    LISTENING,    // 사용자 음성 듣는 중
    PROCESSING,   // 음성 처리 중
    SPEAKING,     // AI 응답 중
    ERROR         // 오류 상태
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceConversationDialog(
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    onReceiveResponse: (String) -> Unit,
    storyContent: String = "",
    gptService: GPTService, // GPTService 추가
    conversationHistory: MutableList<String> // 대화 기록 추가
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val microphonePermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // 대화 상태
    var conversationState by remember { mutableStateOf(VoiceConversationState.WAITING) }
    var currentUserMessage by remember { mutableStateOf("") }
    var currentBotResponse by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAutoMode by remember { mutableStateOf(false) }

    // 대화 기록 (UI 표시용)
    val voiceConversationHistory = remember { mutableStateListOf<Pair<String, String>>() }

    // VoiceChatHelper를 늦은 초기화로 생성
    var voiceChatHelper: VoiceChatHelper? by remember { mutableStateOf(null) }

    // VoiceChatHelper 초기화 - TTS 완료 콜백 포함
    LaunchedEffect(Unit) {
        voiceChatHelper = VoiceChatHelper(
            context = context,
            onSpeechResult = { recognizedText ->
                currentUserMessage = recognizedText
                conversationState = VoiceConversationState.PROCESSING

                // GPT에 메시지 전송하고 응답 처리
                scope.launch {
                    try {
                        // 대화 기록에 사용자 메시지 추가
                        conversationHistory.add("나: $recognizedText")

                        // GPT API 호출
                        val response = gptService.chatWithBot(recognizedText, conversationHistory, storyContent)

                        // 대화 기록에 챗봇 응답 추가
                        conversationHistory.add("동화 챗봇: $response")

                        // 현재 응답 설정
                        currentBotResponse = response

                        // UI 대화 기록에 추가
                        voiceConversationHistory.add(Pair(recognizedText, response))

                        // 응답 상태로 변경
                        conversationState = VoiceConversationState.SPEAKING

                        // TTS로 응답 읽기
                        voiceChatHelper?.speak(response)
                        onReceiveResponse(response)

                        // TTS 완료는 onTTSComplete 콜백에서 처리됨

                    } catch (e: Exception) {
                        errorMessage = "응답 처리 중 오류 발생: ${e.message}"
                        conversationState = VoiceConversationState.ERROR

                        // 3초 후 자동 복구
                        delay(3000)
                        if (isAutoMode) {
                            conversationState = VoiceConversationState.LISTENING
                            voiceChatHelper?.startListening()
                        } else {
                            conversationState = VoiceConversationState.WAITING
                        }
                    }
                }
            },
            onError = { error ->
                errorMessage = error
                conversationState = VoiceConversationState.ERROR
            },
            onTTSComplete = {
                // TTS 완료 시 호출
                if (isAutoMode && conversationState == VoiceConversationState.SPEAKING) {
                    scope.launch {
                        delay(1000) // 1초 대기 후 다시 듣기 시작
                        conversationState = VoiceConversationState.LISTENING
                        voiceChatHelper?.startListening()
                    }
                } else if (conversationState == VoiceConversationState.SPEAKING) {
                    conversationState = VoiceConversationState.WAITING
                }
            }
        )
    }

    // 응답 길이에 따른 TTS 시간 계산 (대략적)
    fun calculateSpeechDuration(text: String): Long {
        // 한국어 기준 대략 분당 150-200자 정도 읽음
        val charactersPerSecond = 3.0
        return ((text.length / charactersPerSecond) * 1000).toLong()
    }

    // 자동 모드 시작 처리
    LaunchedEffect(isAutoMode, voiceChatHelper) {
        if (isAutoMode && conversationState == VoiceConversationState.WAITING &&
            microphonePermissionState.status.isGranted && voiceChatHelper != null) {
            delay(1000)
            conversationState = VoiceConversationState.LISTENING
            voiceChatHelper?.startListening()
        }
    }

    // 컴포넌트 해제 시 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            voiceChatHelper?.cleanup()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0F0C29),
                                Color(0xFF24243e),
                                Color(0xFF302b63)
                            ),
                            radius = 800f
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "음성 대화",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 메인 애니메이션 영역
                // 네온 효과와 블러 추가
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00D4FF).copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                radius = 300f
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    VoiceAnimationVisualizer(
                        state = conversationState,
                        errorMessage = errorMessage
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 상태 텍스트
                Text(
                    text = when (conversationState) {
                        VoiceConversationState.WAITING -> "음성 대화를 시작하세요"
                        VoiceConversationState.LISTENING -> "듣고 있어요... 말씀해주세요"
                        VoiceConversationState.PROCESSING -> "메시지를 처리하는 중..."
                        VoiceConversationState.SPEAKING -> "답변하고 있어요"
                        VoiceConversationState.ERROR -> errorMessage ?: "오류가 발생했습니다"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W600,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // 현재 사용자 메시지 표시
                if (currentUserMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"$currentUserMessage\"",
                        fontSize = 14.sp,
                        color = Color(0xFFE9D364),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 현재 봇 응답 표시
                if (currentBotResponse.isNotEmpty() && conversationState == VoiceConversationState.SPEAKING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"$currentBotResponse\"",
                        fontSize = 14.sp,
                        color = Color(0xFF9C27B0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 대화 기록
                if (voiceConversationHistory.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        items(voiceConversationHistory.reversed()) { (user, bot) ->
                            VoiceConversationItem(
                                userMessage = user,
                                botResponse = bot
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 컨트롤 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 수동 시작/중지 버튼
                    FloatingActionButton(
                        onClick = {
                            if (!microphonePermissionState.status.isGranted) {
                                microphonePermissionState.launchPermissionRequest()
                                return@FloatingActionButton
                            }

                            when (conversationState) {
                                VoiceConversationState.WAITING, VoiceConversationState.ERROR -> {
                                    conversationState = VoiceConversationState.LISTENING
                                    voiceChatHelper?.startListening()
                                    errorMessage = null
                                }
                                VoiceConversationState.LISTENING -> {
                                    voiceChatHelper?.stopListening()
                                    conversationState = VoiceConversationState.WAITING
                                }
                                VoiceConversationState.SPEAKING -> {
                                    voiceChatHelper?.stopSpeaking()
                                    conversationState = VoiceConversationState.WAITING
                                }
                                else -> {}
                            }
                        },
                        containerColor = when (conversationState) {
                            VoiceConversationState.LISTENING -> Color(0xFFFF3B30)
                            VoiceConversationState.SPEAKING -> Color(0xFFFF9500)
                            else -> Color(0xFF027AFF)
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = Color(0xFF007AFF).copy(alpha = 0.25f)
                            )
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when (conversationState) {
                                    VoiceConversationState.LISTENING -> R.drawable.ic_stop
                                    VoiceConversationState.SPEAKING -> R.drawable.ic_pause
                                    else -> R.drawable.ic_mic
                                }
                            ),
                            contentDescription = "음성 컨트롤",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 자동 모드 토글
                    Switch(
                        checked = isAutoMode,
                        onCheckedChange = { newValue ->
                            isAutoMode = newValue
                            if (newValue && conversationState == VoiceConversationState.WAITING) {
                                conversationState = VoiceConversationState.LISTENING
                                voiceChatHelper?.startListening()
                            } else if (!newValue && conversationState == VoiceConversationState.LISTENING) {
                                voiceChatHelper?.stopListening()
                                conversationState = VoiceConversationState.WAITING
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF090633),
                            checkedTrackColor = Color(0xFF4738EE).copy(alpha = 0.5f)
                        )
                    )
                }

                Text(
                    text = if (isAutoMode) "자동 대화 모드" else "수동 대화 모드",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// 나머지 함수들은 동일하게 유지...
@Composable
fun VoiceAnimationVisualizer(
    state: VoiceConversationState,
    errorMessage: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_animation")

    // 메인 애니메이션
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 파동 애니메이션
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .scale(if (state == VoiceConversationState.LISTENING) scale else 1f)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width / 6

        when (state) {
            VoiceConversationState.WAITING -> {
                drawWaitingAnimation(center, baseRadius)
            }
            VoiceConversationState.LISTENING -> {
                drawListeningAnimation(center, baseRadius, waveOffset, scale)
            }
            VoiceConversationState.PROCESSING -> {
                drawProcessingAnimation(center, baseRadius, rotation)
            }
            VoiceConversationState.SPEAKING -> {
                drawSpeakingAnimation(center, baseRadius, waveOffset)
            }
            VoiceConversationState.ERROR -> {
                drawErrorAnimation(center, baseRadius)
            }
        }
    }
}

private fun DrawScope.drawWaitingAnimation(center: Offset, radius: Float) {

    drawCircle(
        color = Color(0xFF4050D7).copy(alpha = 0.3f),
        radius = radius * 2,
        center = center
    )
    drawCircle(
        color = Color(0xFF1F1F7A),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawListeningAnimation(
    center: Offset,
    radius: Float,
    waveOffset: Float,
    scale: Float
) {
    // 음성 인식 중 - 파동 효과
    for (i in 1..4) {
        val waveRadius = radius * (1 + i * 0.6f) * scale
        val alpha = (0.8f - (i * 0.15f)) * sin(waveOffset + i).toFloat().coerceIn(0f, 1f)

        // 외부 글로우
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3F40E5).copy(alpha = alpha * 0.3f),
                    Color.Transparent
                )
            ),
            radius = waveRadius + 10.dp.toPx(),
            center = center
        )

        // 메인 링
        drawCircle(
            color = Color(0xFF3F40E5).copy(alpha = alpha),
            radius = waveRadius,
            center = center,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 중앙 마이크
    drawCircle(
        color = Color(0xFF2196F3),
        radius = radius,
        center = center
    )

    // 음성 레벨 표시 (랜덤한 바)
    for (i in 0 until 8) {
        val angle = i * 45f * PI / 180f
        val barLength = radius * (0.5f + Random.nextFloat() * 0.5f) * scale
        val startOffset = Offset(
            center.x + cos(angle).toFloat() * radius * 1.5f,
            center.y + sin(angle).toFloat() * radius * 1.5f
        )
        val endOffset = Offset(
            center.x + cos(angle).toFloat() * (radius * 1.5f + barLength),
            center.y + sin(angle).toFloat() * (radius * 1.5f + barLength)
        )

        drawLine(
            color = Color(0xFF64B5F6),
            start = startOffset,
            end = endOffset,
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawProcessingAnimation(center: Offset, radius: Float, rotation: Float) {
    // 처리 중 - 회전하는 점들
    for (i in 0 until 6) {
        val angle = (rotation + i * 60f) * PI / 180f
        val dotCenter = Offset(
            center.x + cos(angle).toFloat() * radius * 1.5f,
            center.y + sin(angle).toFloat() * radius * 1.5f
        )

        drawCircle(
            color = Color(0xFF3E3AD0),
            radius = radius * 0.2f,
            center = dotCenter
        )
    }

    // 중앙 원
    drawCircle(
        color = Color(0xFF120C44).copy(alpha = 0.3f),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawSpeakingAnimation(center: Offset, radius: Float, waveOffset: Float) {
    // 음성 출력 중 - 스피커 파동
    for (i in 1..4) {
        val waveRadius = radius * (1 + i * 0.6f)
        val alpha = (sin(waveOffset + i * PI / 2) + 1) / 2 * 0.5f

        drawCircle(
            color = Color(0xFF4319B2).copy(alpha = alpha.toFloat()),
            radius = waveRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // 중앙 스피커
    drawCircle(
        color = Color(0xFF2E0764),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawErrorAnimation(center: Offset, radius: Float) {
    // 오류 상태 - 빨간 X
    drawCircle(
        color = Color(0xFF15105D).copy(alpha = 0.3f),
        radius = radius * 1.5f,
        center = center
    )

    drawCircle(
        color = Color(0xFF15105D),
        radius = radius,
        center = center
    )

    // X 표시
    val lineLength = radius * 0.4f
    drawLine(
        color = Color.White,
        start = Offset(center.x - lineLength, center.y - lineLength),
        end = Offset(center.x + lineLength, center.y + lineLength),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(center.x + lineLength, center.y - lineLength),
        end = Offset(center.x - lineLength, center.y + lineLength),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Composable
fun VoiceConversationItem(
    userMessage: String,
    botResponse: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // 사용자 메시지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = userMessage,
                    color = Color(0xFFE9D364),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 봇 응답
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = botResponse,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}