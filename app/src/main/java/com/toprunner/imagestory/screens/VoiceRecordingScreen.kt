package com.toprunner.imagestory.screens

import android.Manifest
import android.media.MediaRecorder
import android.util.Log
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
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.SimpleAudioAnalyzer
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.ui.components.ImprovedVoiceFeatureVisualization
import com.toprunner.imagestory.util.AudioAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File




class PitchAndMfccAnalyzer {
    fun analyzeAudio(filePath: String): VoiceFeatures {
        // 실제 구현: 파일을 열어 프레임 단위로 피치와 MFCC값 계산
        val averagePitch = 150.0
        val pitchStdDev = 10.0
        val mfccValues = listOf(DoubleArray(13) { 0.0 }, DoubleArray(13) { 1.0 })
        return VoiceFeatures(averagePitch, pitchStdDev, mfccValues)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecordingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 녹음 상태 및 진행 변수
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimeSeconds by remember { mutableStateOf(0) }
    var recordingProgress by remember { mutableStateOf(0) }



    // 녹음 샘플 텍스트
    val sampleText = "안녕하세요. 이 텍스트를 읽어주세요. 이 녹음은 당신의 목소리로 동화를 읽어주기 위한 샘플로 사용됩니다. 천천히 또박또박 읽어주세요."

    // 마이크 권한 체크
    val microphonePermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // 녹음 시간 표시
    val minutes = recordingTimeSeconds / 60
    val seconds = recordingTimeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    // MediaRecorder 및 기록 파일 경로
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordFilePath by remember { mutableStateOf<String?>(null) }

    // 오디오 분석 도구 (실제 구현 시 TarsosDSP 등으로 대체)
    val pitchAnalyzer = remember { PitchAndMfccAnalyzer() }
    // 오디오 분석 도구
    val simpleAnalyzer = SimpleAudioAnalyzer(context)
    val result = if (recordFilePath != null) {
        simpleAnalyzer.analyzeAudio(recordFilePath!!)
    } else {
        // 기본값 반환
        VoiceFeatures(
            averagePitch = 150.0,
            pitchStdDev = 15.0,
            mfccValues = listOf(DoubleArray(13) { 0.0 })
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    // 음성 분석 결과를 저장할 상태 변수
    var analysisComplete by remember { mutableStateOf(false) }
    var analyzedFeatures by remember { mutableStateOf<VoiceFeatures?>(null) }


    // 권한 요청
    LaunchedEffect(Unit) {
        if (!microphonePermissionState.status.isGranted) {
            microphonePermissionState.launchPermissionRequest()
        }
    }

    // 녹음 시간 업데이트 (녹음 중이면 1초마다 업데이트)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimeSeconds = 0
            recordingProgress = 0
            while (isRecording) {
                delay(1000)
                recordingTimeSeconds++
                recordingProgress = (recordingTimeSeconds * 100 / 60).coerceAtMost(100)
                // 60초가 지나면 자동 중지
                if (recordingTimeSeconds >= 60) {
                    isRecording = false
                }
            }
        }
    }

    // 실제 녹음 시작 함수
    fun startRecording() {
        try {
            val outputDir = context.filesDir
            val fileName = "record_${System.currentTimeMillis()}.wav" // WAV 또는 3GP 포맷 사용 가능
            val outFile = File(outputDir, fileName)
            recordFilePath = outFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                // 여기서는 THREE_GPP 포맷과 AMR_NB 인코더를 사용합니다.
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordFilePath)
                prepare()
                start()
            }
            isRecording = true
            Toast.makeText(context, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "녹음 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 실제 녹음 중지 함수
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Toast.makeText(context, "녹음이 중지되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "녹음 중지 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 녹음 시작/중지 토글 함수 (버튼 클릭 시 호출)
    val toggleRecording = {
        if (!microphonePermissionState.status.isGranted) {
            Toast.makeText(context, "녹음을 위해 마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            microphonePermissionState.launchPermissionRequest()
        } else {
            if (!isRecording) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    // 녹음 완료 처리 함수
    fun completeRecording() {
        if (isRecording) {
            stopRecording()
        }
        // 녹음된 시간과 파일 경로가 유효한지 확인
        if (recordingTimeSeconds <= 0 || recordFilePath.isNullOrEmpty()) {
            Toast.makeText(context, "녹음이 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                // 분석 시작 상태로 변경
                isAnalyzing = true
                analysisComplete = false

                // 파일 경로를 로그로 출력
                Log.d("VoiceRecordingScreen", "Analyzing audio file: $recordFilePath")


                // 분석 결과를 UI에 표시하기 위해 상태 업데이트
                withContext(Dispatchers.Main) {
                    analyzedFeatures = result
                    analysisComplete = true
                    isAnalyzing = false
                    Toast.makeText(context, "음성 분석값 생성 중...", Toast.LENGTH_SHORT).show()
                    // 분석 결과 로그 출력
                    Log.d("VoiceRecordingScreen", "Analysis complete: pitchAvg=${result.averagePitch}, stdDev=${result.pitchStdDev}")

                }

                // DB에 저장 (VoiceRepository 사용)
                val voiceRepo = VoiceRepository(context)
                val voiceTitle = "내 목소리 - ${System.currentTimeMillis()}"
                // attribute JSON에는 필요한 값만 저장 (추후 기능 확장 가능)
                val attributeJson = "{\"voiceType\":\"custom\"}"

                val fileBytes = File(recordFilePath!!).readBytes()
                val voiceId = voiceRepo.saveVoice(
                    title = voiceTitle,
                    attributeJson = attributeJson,
                    audioData = fileBytes,
                    voiceFeatures = result
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "목소리가 저장되었습니다 (ID: $voiceId)", Toast.LENGTH_SHORT).show()
//                    navController.navigate(NavRoute.VoiceList.route) { // 이 부분은 UI/UX 분석후 수정, 일단 보류
//                        popUpTo(NavRoute.VoiceList.route) { inclusive = true }
//                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isAnalyzing = false
                    Toast.makeText(context, "녹음 분석 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("VoiceRecordingScreen", "Analysis error", e)

                }
            }
        }
    }

    @Composable
    fun VoiceFeatureVisualization(voiceFeatures: VoiceFeatures) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "평균 피치: ${voiceFeatures.averagePitch.toInt()} Hz",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "피치 표준편차: ${voiceFeatures.pitchStdDev.toInt()} Hz",
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 피치 시각화 (간단한 수평 바)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray)
            ) {
                // 피치에 따라 바 크기 조정 (50Hz~350Hz 범위 가정)
                val fillPercent = (voiceFeatures.averagePitch - 50) / 300
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillPercent.toFloat().coerceIn(0f, 1f))
                        .background(Color(0xFFE9D364))
                )
            }
        }
    }

    // UI 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
            .verticalScroll(scrollState)
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

        // 녹음 안내 텍스트
        Text(
            text = "목소리 녹음하기",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // 녹음 설명
        Text(
            text = "아래 텍스트를 읽으며 목소리를 녹음해주세요.\n천천히 또박또박 읽어주세요.",
            modifier = Modifier
                .fillMaxWidth()
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

        // 녹음 컨트롤 버튼 (녹음 시작/정지)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        // 분석 중 표시
        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE9D364)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "음성 특징을 분석하는 중입니다...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 분석 결과 표시 섹션
        if (analysisComplete && analyzedFeatures != null) {
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.5.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "음성 분석 결과",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 기존의 VoiceFeatureVisualization 함수 호출
            //VoiceFeatureVisualization(voiceFeatures = analyzedFeatures!!)

            // 향상된 음성 특징 시각화 구성 요소 사용
            ImprovedVoiceFeatureVisualization(voiceFeatures = analyzedFeatures!!)

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
