package com.toprunner.imagestory.ui.components

import android.Manifest
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.OutlinedTextField as M2OutlinedTextField
import androidx.compose.material.TextFieldDefaults as M2TextFieldDefaults
import androidx.compose.material.Text as M2Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.util.VoiceChatHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatbotDialog(
    conversationHistory: List<String>,
    userMessage: String,
    isLoading: Boolean,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    onSpeakResponse: (String) -> Unit = {}, // 챗봇 응답을 음성으로 읽기 위한 콜백
    onStartVoiceConversation: () -> Unit = {} // 음성 대화 모드 시작 콜백
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 마이크 권한 상태
    val microphonePermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // 음성 관련 상태
    var isListening by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    // VoiceChatHelper 초기화
    val voiceChatHelper = remember {
        VoiceChatHelper(
            context = context,
            onSpeechResult = { recognizedText ->
                onMessageChange(recognizedText)
                isListening = false
            },
            onError = { error ->
                voiceError = error
                isListening = false
            }
        )
    }

    // 컴포넌트 해제 시 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            voiceChatHelper.cleanup()
        }
    }

    // 음성 인식 시작/중지 함수
    fun toggleVoiceRecognition() {
        if (!microphonePermissionState.status.isGranted) {
            microphonePermissionState.launchPermissionRequest()
            return
        }

        if (isListening) {
            voiceChatHelper.stopListening()
            isListening = false
        } else {
            voiceChatHelper.startListening()
            isListening = true
            voiceError = null
        }
    }

    // 텍스트를 음성으로 읽기
    fun speakText(text: String) {
        voiceChatHelper.speak(text)
        isSpeaking = true
        onSpeakResponse(text)
    }

    // 음성 중지
    fun stopSpeaking() {
        voiceChatHelper.stopSpeaking()
        isSpeaking = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            color = Color(0xFFF8F9FA)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFAFAFA), // 상단 밝은 회색
                                Color(0xFFF0F0F5)  // 하단 약간 더 어두운 회색
                            )
                        )
                    )
            ) {
                // 제목 바
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667eea),
                                        Color(0xFF764ba2)
                                    )
                                )
                            )
                            .padding(vertical = 20.dp, horizontal = 20.dp)
                    ) {
                    Text(
                        "동화 챗봇",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 음성 대화 모드 버튼
                        IconButton(
                            onClick = { onStartVoiceConversation() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mic),
                                contentDescription = "음성 대화 모드",
                                tint = Color.White
                            )
                        }

                        // TTS 제어 버튼
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    stopSpeaking()
                                } else {
                                    // 마지막 챗봇 응답을 음성으로 읽기
                                    val lastBotResponse = conversationHistory
                                        .lastOrNull { it.startsWith("동화 챗봇:") }
                                        ?.substringAfter("동화 챗봇: ")?.trim()

                                    if (!lastBotResponse.isNullOrBlank()) {
                                        speakText(lastBotResponse)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isSpeaking) R.drawable.ic_pause else R.drawable.ic_volume_up
                                ),
                                contentDescription = if (isSpeaking) "음성 중지" else "음성 재생",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                }
            }

                // 메시지 영역
                val listState = rememberLazyListState()
                LaunchedEffect(conversationHistory.size, isLoading) {
                    listState.animateScrollToItem(conversationHistory.size + if (isLoading) 1 else 0)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    items(conversationHistory) { raw ->
                        val isUser = raw.startsWith("나:")
                        val text = raw.substringAfter(": ").trim()

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser)
                                        Color(0xFF007AFF).copy(alpha = 0.9f)
                                    else
                                        Color(0xFFF8F9FA)
                                ),
                                shape = RoundedCornerShape(
                                    topStart = if (isUser) 20.dp else 4.dp,
                                    topEnd = if (isUser) 4.dp else 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                ),
                                //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .animateContentSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = text,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // 챗봇 메시지에 음성 재생 버튼 추가
                                    if (!isUser && text.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { speakText(text) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_volume_up),
                                                contentDescription = "음성으로 듣기",
                                                tint = Color(0xFFE9D364),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.widthIn(max = 240.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFFE9D364)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "생각하는 중...",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 오류 메시지 표시
                voiceError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // 입력창 + 버튼들
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 텍스트 입력 필드
                    M2OutlinedTextField(
                        value = userMessage,
                        onValueChange = onMessageChange,
                        placeholder = { M2Text("메시지를 입력하세요") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = M2TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF007AFF),
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            cursorColor = Color(0xFF007AFF),
                            focusedLabelColor = Color(0xFF007AFF)
                        ),
                        shape = RoundedCornerShape(15.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (userMessage.isNotBlank()) {
                                onSend()
                                onMessageChange("")
                                focusManager.clearFocus()
                            }
                        })
                    )

                    Spacer(Modifier.width(8.dp))

                    // 음성 인식 버튼
                    val voiceButtonScale by animateFloatAsState(
                        targetValue = if (isListening) 1.2f else 1f,
                        animationSpec = tween(150),
                        label = "voice button scale"
                    )

                    IconButton(
                        onClick = { toggleVoiceRecognition() },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(voiceButtonScale)
                            .background(
                                color = if (isListening) Color(0xFFFF5722) else Color(0xFF2196F3),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isListening) R.drawable.ic_stop else R.drawable.ic_mic
                            ),
                            contentDescription = if (isListening) "음성 인식 중지" else "음성 인식 시작",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // 텍스트 전송 버튼
                    IconButton(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                onSend()
                                onMessageChange("")
                                focusManager.clearFocus()
                            }
                        },
                        enabled = userMessage.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (userMessage.isNotBlank()) Color(0xFFE9D364) else Color.LightGray,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "전송",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}