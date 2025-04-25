package com.toprunner.imagestory.screens

import android.Manifest
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.ui.components.VoiceRecorderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
/*
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecordingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 녹음 상태
    val isRecording by remember { mutableStateOf(0) }
    val isPlaying by remember { mutableStateOf(0) }

    var recordingTimeSeconds by remember { mutableStateOf(0) }
    var recordingProgress by remember { mutableStateOf(0) }

    // 녹음 샘플 텍스트
    val sampleText = "지친 여우는 찬바람이 부는 언덕 위에서, 잠시 멈춰서 세상을 바라보았어요.\n파란 구름 아래에서 조용히 걷던 토끼는, 갑자기 번쩍이는 빛에 눈을 감았어요."

    // 마이크 권한 체크
    val microphonePermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // 녹음 시간 형식 지정
    val minutes = recordingTimeSeconds / 60
    val seconds = recordingTimeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    // 권한 요청
    LaunchedEffect(Unit) {
        if (!microphonePermissionState.status.isGranted) {
            microphonePermissionState.launchPermissionRequest()
        }
    }

    // 녹음 시간 업데이트
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimeSeconds = 0
            recordingProgress = 0

            while (isRecording) {
                delay(1000)
                recordingTimeSeconds++
                recordingProgress = (recordingTimeSeconds * 100 / 60).coerceAtMost(100) // 최대 60초

                // 60초 지나면 자동 녹음 중지
                if (recordingTimeSeconds >= 60) {
                    isRecording = false
                }
            }
        }
    }

    // 녹음 시작/중지 함수
    val toggleRecording = {
        if (!microphonePermissionState.status.isGranted) {
            Toast.makeText(context, "녹음을 위해 마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            microphonePermissionState.launchPermissionRequest()
        } else {
            isRecording = !isRecording

            if (isRecording) {
                Toast.makeText(context, "녹음이 시작되었습니다", Toast.LENGTH_SHORT).show()
                // TODO: 실제 녹음 시작 로직 구현
            } else {
                Toast.makeText(context, "녹음이 중지되었습니다", Toast.LENGTH_SHORT).show()
                // TODO: 실제 녹음 중지 로직 구현
            }
        }
    }

    // 녹음 완료 처리
    val completeRecording = {
        if (isRecording) {
            isRecording = false
        }

        if (recordingTimeSeconds > 0) {
            // TODO: 실제 녹음 파일 저장 및 처리 로직 구현
            Toast.makeText(context, "목소리가 저장되었습니다", Toast.LENGTH_SHORT).show()
            navController.navigate(NavRoute.VoiceList.route) {
                popUpTo(NavRoute.VoiceList.route) { inclusive = true }
            }
        } else {
            Toast.makeText(context, "녹음을 먼저 완료해주세요", Toast.LENGTH_SHORT).show()
        }
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
                text = "목소리 녹음",
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

//        // 녹음 안내 텍스트
//        Text(
//            text = "목소리 녹음하기",
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Center
//        )

        // 녹음 설명
        Text(
            text = "아래 텍스트를 읽으며 목소리를 녹음해주세요.\n천천히 또박또박 읽어주세요.\n연기하듯이 감정이 담길 수록 좋습니다!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(horizontal = 16.dp),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 녹음 시간 표시
        Text(
            text = "녹음 시간: $timeText",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (isRecording) Color(0xFFE76F51) else Color.Gray
        )

        // 녹음 진행 상태 바
        LinearProgressIndicator(
            progress = { recordingProgress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .height(8.dp),
            color = Color(0xFFE76F51),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 녹음 샘플 텍스트 - 스크롤 가능한 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = sampleText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }

        // 녹음 컨트롤 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 녹음 시작/정지 버튼
            FloatingActionButton(
                onClick = { toggleRecording() },
                modifier = Modifier.size(64.dp),
                containerColor = if (isRecording) Color(0xFFE76F51) else Color(0xFFE9D364),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isRecording) R.drawable.ic_stop else R.drawable.ic_mic
                    ),
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 녹음 완료 버튼
        Button(
            onClick = { completeRecording() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD166),
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isRecording && recordingTimeSeconds > 0
        ) {
            Text(
                text = "녹음 완료",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (!isRecording && recordingTimeSeconds > 0) Color.Black else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

 */