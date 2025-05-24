package com.toprunner.imagestory.screens

import android.Manifest
import android.content.ContentValues.TAG
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.util.SimpleAudioAnalyzer
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.ui.components.ImprovedVoiceFeatureVisualization
import com.toprunner.imagestory.util.AudioAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.sin
import kotlin.random.Random


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
    var showNoiseReductionDialog by remember { mutableStateOf(false) }
    var tempAudioPath by remember { mutableStateOf<String?>(null) }
    var isProcessingNoise by remember { mutableStateOf(false) }


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
            Toast.makeText(context, "녹음을 저장 중 입니다...", Toast.LENGTH_LONG).show()
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
        tempAudioPath = recordFilePath
        showNoiseReductionDialog = true

    }

    // 음성 처리 및 저장 함수 (노이즈 제거 적용 여부와 관계없이 공통 처리)
    suspend fun processAndSaveVoice(audioPath: String) {
        try {
            Log.d(TAG, "Processing voice file: $audioPath")

            // 파일 경로를 로그로 출력
            Log.d("VoiceRecordingScreen", "Analyzing audio file: $audioPath")

            // 음성 분석
            val result = simpleAnalyzer.analyzeAudio(audioPath)

            // 분석 결과를 UI에 표시하기 위해 상태 업데이트
            withContext(Dispatchers.Main) {
                analyzedFeatures = result
                analysisComplete = true
                isAnalyzing = false
                Toast.makeText(context, "음성 분석값 생성 중...", Toast.LENGTH_SHORT).show()
                Log.d("VoiceRecordingScreen", "Analysis complete: pitchAvg=${result.averagePitch}, stdDev=${result.pitchStdDev}")
            }

            // DB에 저장 (VoiceRepository 사용)
            val voiceRepo = VoiceRepository(context)
            val voiceTitle = "내 목소리 - ${System.currentTimeMillis()}"

            // 노이즈 제거 여부 속성에 추가
            val isNoiseReduced = audioPath != recordFilePath
            val attributeJson = JSONObject().apply {
                put("voiceType", "custom")
                put("isNoiseReduced", isNoiseReduced)
                put("originalPath", recordFilePath)
            }.toString()

            val fileBytes = File(audioPath).readBytes()
            val voiceId = voiceRepo.saveVoice(
                title = voiceTitle,
                attributeJson = attributeJson,
                audioData = fileBytes,
                voiceFeatures = result
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "목소리가 저장되었습니다 (ID: $voiceId)" +
                            if (isNoiseReduced) " (노이즈 제거 적용)" else "",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                isAnalyzing = false
                Toast.makeText(context, "녹음 분석 오류: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("VoiceRecordingScreen", "Analysis error", e)
            }
        }
    }


    // 노이즈 제거 함수
    suspend fun applyNoiseReduction(originalPath: String): String = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Applying noise reduction to: $originalPath")

            // 노이즈 제거된 파일을 저장할 새 경로 생성
            val outputDir = context.filesDir
            val fileName = "noise_reduced_${System.currentTimeMillis()}.wav"
            val outputFile = File(outputDir, fileName)

            // 여기서 실제 노이즈 제거 알고리즘을 구현
            // 간단한 시연을 위해 원본 파일을 복사하고 약간의 지연을 추가하여 처리 중인 것처럼 보이게 함
            withContext(Dispatchers.IO) {
                // 실제 구현에서는 TarsosDSP나 다른 오디오 처리 라이브러리를 사용하여
                // 노이즈 제거 알고리즘을 구현해야 함

                // 처리 중인 것처럼 보이기 위한 지연
                delay(1500)

                // 임시로 단순 복사 (실제로는 노이즈 제거된 파일이 생성되어야 함)
                File(originalPath).copyTo(outputFile, overwrite = true)
            }

            Log.d(TAG, "Noise reduction completed, saved to: ${outputFile.absolutePath}")
            return@withContext outputFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error in noise reduction: ${e.message}", e)
            throw e
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
    // 노이즈 제거 다이얼로그
    if (showNoiseReductionDialog) {
        NoiseReductionDialog(
            onDismiss = {
                showNoiseReductionDialog = false
                tempAudioPath = null
            },
            onConfirmWithNoiseReduction = {
                // 노이즈 제거 로직 실행
                scope.launch {
                    try {
                        isProcessingNoise = true

                        // 노이즈 제거 처리
                        val outputDir = context.filesDir
                        val noiseReducedFileName = "noise_reduced_${System.currentTimeMillis()}.wav"
                        val noiseReducedPath = File(outputDir, noiseReducedFileName).absolutePath

                        // 노이즈 제거 작업 (오래 걸릴 수 있으므로 IO 스레드에서 실행)
                        val audioAnalyzer = AudioAnalyzer(context)
                        val success = withContext(Dispatchers.IO) {
                            audioAnalyzer.reduceNoise(tempAudioPath!!, noiseReducedPath)
                        }

                        if (success) {
                            // 노이즈 제거 성공, 분석 및 저장 진행
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "노이즈 제거 처리 완료", Toast.LENGTH_SHORT).show()
                            }
                            isAnalyzing = true
                            processAndSaveVoice(noiseReducedPath)
                        } else {
                            // 실패 시 원본 파일로 진행
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "노이즈 제거 실패, 원본 파일로 처리합니다", Toast.LENGTH_SHORT).show()
                            }
                            isAnalyzing = true
                            processAndSaveVoice(tempAudioPath!!)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "노이즈 제거 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                            isProcessingNoise = false
                            isAnalyzing = false
                        }
                    } finally {
                        isProcessingNoise = false
                        showNoiseReductionDialog = false
                    }
                }
            },
            onConfirmWithoutNoiseReduction = {
                // 원본 파일 그대로 처리
                scope.launch {
                    isAnalyzing = true
                    processAndSaveVoice(tempAudioPath!!)
                    showNoiseReductionDialog = false
                }
            }
        )
    }
    // 노이즈 처리 중 로딩 표시
    if (isProcessingNoise) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE9D364),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "노이즈 제거 처리 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3F2E20)
                    )

                    Text(
                        text = "잠시만 기다려주세요",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )


            // 향상된 음성 특징 시각화 구성 요소 사용
            ImprovedVoiceFeatureVisualization(voiceFeatures = analyzedFeatures!!)

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun NoiseReductionDialog(
    onDismiss: () -> Unit,
    onConfirmWithNoiseReduction: () -> Unit,
    onConfirmWithoutNoiseReduction: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "노이즈 제거",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F2E20)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 노이즈 제거 아이콘 (간단한 파형 표현)
                Canvas(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2

                    // 배경 파형 (노이즈 있는 상태)
                    val noisyPath = Path()
                    noisyPath.moveTo(0f, centerY)

                    // 노이즈 있는 파형 생성
                    for (x in 0..width.toInt() step 2) {
                        val xFloat = x.toFloat()
                        // 메인 파형 + 노이즈
                        val mainWave = sin(xFloat / 15) * height * 0.2f
                        val noise = (Random.nextFloat() - 0.5f) * height * 0.15f
                        val y = centerY + mainWave + noise
                        noisyPath.lineTo(xFloat, y)
                    }

                    // 노이즈 있는 파형 그리기
                    drawPath(
                        path = noisyPath,
                        color = Color.LightGray,
                        style = Stroke(width = 1.5f)
                    )

                    // 노이즈 제거된 파형
                    val cleanPath = Path()
                    cleanPath.moveTo(0f, centerY)

                    // 노이즈 제거된 깨끗한 파형 생성
                    for (x in 0..width.toInt() step 2) {
                        val xFloat = x.toFloat()
                        // 메인 파형만
                        val mainWave = sin(xFloat / 15) * height * 0.2f
                        val y = centerY + mainWave
                        cleanPath.lineTo(xFloat, y)
                    }

                    // 노이즈 제거된 파형 그리기
                    drawPath(
                        path = cleanPath,
                        color = Color(0xFFE9D364),
                        style = Stroke(width = 2.5f)
                    )
                }

                Text(
                    text = "녹음된 음성에서 노이즈를 제거하시겠습니까?",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "노이즈 제거를 적용하면 배경 소음이 줄어들지만 처리 시간이 조금 더 소요됩니다.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onConfirmWithNoiseReduction,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE9D364)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "노이즈 제거",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onConfirmWithoutNoiseReduction,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "원본 유지",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
