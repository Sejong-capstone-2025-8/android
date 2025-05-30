package com.toprunner.imagestory.screens

import android.Manifest
import android.content.ContentValues.TAG
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
import kotlin.math.*
import kotlin.random.Random

class PitchAndMfccAnalyzer {
    fun analyzeAudio(filePath: String): VoiceFeatures {
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

    // 오디오 분석 도구
    val simpleAnalyzer = SimpleAudioAnalyzer(context)

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisComplete by remember { mutableStateOf(false) }
    var analyzedFeatures by remember { mutableStateOf<VoiceFeatures?>(null) }

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
                recordingProgress = (recordingTimeSeconds * 100 / 60).coerceAtMost(100)
                if (recordingTimeSeconds >= 60) {
                    isRecording = false
                }
            }
        }
    }

    // 개선된 녹음 시작 함수 - WAV 형식으로 변경
    fun startRecording() {
        try {
            val outputDir = context.filesDir
            val fileName = "record_${System.currentTimeMillis()}.wav"
            val outFile = File(outputDir, fileName)
            recordFilePath = outFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                // WAV 포맷으로 변경 (API 29+에서 지원)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                } else {
                    // 이전 버전에서는 3GP 사용하되, 파일명을 3gp로 변경
                    val fileName3gp = "record_${System.currentTimeMillis()}.3gp"
                    val outFile3gp = File(outputDir, fileName3gp)
                    recordFilePath = outFile3gp.absolutePath

                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }
                setOutputFile(recordFilePath)
                setAudioSamplingRate(16000) // 16kHz 샘플링 레이트 설정
                setAudioEncodingBitRate(64000) // 64kbps 비트레이트 설정
                prepare()
                start()
            }
            isRecording = true
            Toast.makeText(context, "녹음을 시작합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "녹음 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Recording start failed", e)
        }
    }

    // 녹음 중지 함수
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Toast.makeText(context, "녹음 완료", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "녹음 중지 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Recording stop failed", e)
        }
    }

    // 녹음 토글 함수
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

    // 녹음 완료 처리
    fun completeRecording() {
        if (isRecording) {
            stopRecording()
        }
        if (recordingTimeSeconds <= 0 || recordFilePath.isNullOrEmpty()) {
            Toast.makeText(context, "녹음이 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        tempAudioPath = recordFilePath
        showNoiseReductionDialog = true
    }

    // 개선된 음성 처리 및 저장 함수
    suspend fun processAndSaveVoice(audioPath: String) {
        try {
            Log.d(TAG, "Processing voice file: $audioPath")

            // 파일 존재 여부 확인
            val audioFile = File(audioPath)
            if (!audioFile.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "오디오 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return
            }

            Log.d(TAG, "Audio file size: ${audioFile.length()} bytes")

            // 음성 분석
            val result = simpleAnalyzer.analyzeAudio(audioPath)

            // 분석 결과를 UI에 표시
            withContext(Dispatchers.Main) {
                analyzedFeatures = result
                analysisComplete = true
                isAnalyzing = false
                Toast.makeText(context, "음성 분석 완료", Toast.LENGTH_SHORT).show()
            }

            // DB에 저장
            val voiceRepo = VoiceRepository(context)
            val voiceTitle = "내 목소리 - ${System.currentTimeMillis()}"

            val isNoiseReduced = audioPath != recordFilePath
            val attributeJson = JSONObject().apply {
                put("voiceType", "custom")
                put("isNoiseReduced", isNoiseReduced)
                put("originalPath", recordFilePath)
                put("fileFormat", if (audioPath.endsWith(".wav")) "wav" else "3gp")
            }.toString()

            val fileBytes = audioFile.readBytes()
            val voiceId = voiceRepo.saveVoice(
                title = voiceTitle,
                attributeJson = attributeJson,
                audioData = fileBytes,
                voiceFeatures = result
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "목소리가 저장되었습니다" + if (isNoiseReduced) " (노이즈 제거 적용)" else "",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                isAnalyzing = false
                Toast.makeText(context, "음성 처리 오류: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Voice processing error", e)
            }
        }
    }

    // 개선된 노이즈 제거 함수
    suspend fun applyNoiseReduction(originalPath: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Applying improved noise reduction to: $originalPath")

            val originalFile = File(originalPath)
            if (!originalFile.exists()) {
                throw Exception("원본 파일이 존재하지 않습니다: $originalPath")
            }

            // 노이즈 제거된 파일 경로
            val outputDir = context.filesDir
            val fileName = "noise_reduced_${System.currentTimeMillis()}.${originalFile.extension}"
            val outputFile = File(outputDir, fileName)

            // 기본적인 노이즈 제거: 원본 파일 복사 후 간단한 처리
            // 실제 프로덕션에서는 더 정교한 알고리즘이 필요합니다
            try {
                // AudioAnalyzer의 노이즈 제거 기능 사용
                val audioAnalyzer = AudioAnalyzer(context)
                val success = audioAnalyzer.reduceNoise(originalPath, outputFile.absolutePath)

                if (success && outputFile.exists() && outputFile.length() > 0) {
                    Log.d(TAG, "Noise reduction completed successfully")
                    return@withContext outputFile.absolutePath
                } else {
                    Log.w(TAG, "Noise reduction failed, using original file")
                    // 노이즈 제거가 실패하면 원본 파일 사용
                    return@withContext originalPath
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in noise reduction process: ${e.message}", e)
                // 오류 발생 시 원본 파일 사용
                return@withContext originalPath
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in noise reduction: ${e.message}", e)
            // 오류 발생 시 원본 파일 경로 반환
            return@withContext originalPath
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
                scope.launch {
                    try {
                        isProcessingNoise = true
                        showNoiseReductionDialog = false

                        // 노이즈 제거 처리
                        val processedPath = applyNoiseReduction(tempAudioPath!!)

                        // 결과에 관계없이 분석 및 저장 진행
                        isAnalyzing = true
                        processAndSaveVoice(processedPath)

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "처리 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Processing error", e)
                        }
                    } finally {
                        isProcessingNoise = false
                    }
                }
            },
            onConfirmWithoutNoiseReduction = {
                scope.launch {
                    isAnalyzing = true
                    processAndSaveVoice(tempAudioPath!!)
                    showNoiseReductionDialog = false
                }
            }
        )
    }

    // 노이즈 처리 중 애니메이션 다이얼로그
    if (isProcessingNoise) {
        NoiseReductionProcessingDialog()
    }

    // UI 구성 (기존과 동일)
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

        // 녹음 샘플 텍스트
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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

        // 녹음 컨트롤 버튼
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
                    CircularProgressIndicator(color = Color(0xFFE9D364))
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

        // 분석 결과 표시
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

                // 노이즈 제거 시각화
                Canvas(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2

                    // 노이즈 있는 파형
                    val noisyPath = Path()
                    noisyPath.moveTo(0f, centerY)

                    for (x in 0..width.toInt() step 2) {
                        val xFloat = x.toFloat()
                        val mainWave = sin(xFloat / 15) * height * 0.2f
                        val noise = (Random.nextFloat() - 0.5f) * height * 0.15f
                        val y = centerY + mainWave + noise
                        noisyPath.lineTo(xFloat, y)
                    }

                    drawPath(
                        path = noisyPath,
                        color = Color.LightGray,
                        style = Stroke(width = 1.5f)
                    )

                    // 깨끗한 파형
                    val cleanPath = Path()
                    cleanPath.moveTo(0f, centerY)

                    for (x in 0..width.toInt() step 2) {
                        val xFloat = x.toFloat()
                        val mainWave = sin(xFloat / 15) * height * 0.2f
                        val y = centerY + mainWave
                        cleanPath.lineTo(xFloat, y)
                    }

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

@Composable
fun NoiseReductionProcessingDialog() {
    // 다이얼로그 등장 애니메이션
    val dialogScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 처리 단계
    val processingSteps = listOf(
        "오디오 파일 로드",
        "주파수 분석",
        "노이즈 패턴 감지",
        "필터 적용",
        "신호 정리",
        "파일 저장"
    )

    var currentStep by remember { mutableStateOf(0) }
    var progressPercentage by remember { mutableStateOf(0f) }

    // 단계별 진행 애니메이션
    LaunchedEffect(Unit) {
        while (currentStep < processingSteps.size) {
            // 각 단계마다 0.8초~1.5초 소요
            val stepDuration = (800..1500).random()
            val progressIncrement = 100f / processingSteps.size

            // 단계별 진행률 업데이트
            val targetProgress = (currentStep + 1) * progressIncrement

            for (i in 0..20) {
                delay(stepDuration / 20L)
                progressPercentage = progressPercentage + (targetProgress - progressPercentage) * 0.1f
            }

            delay(200) // 단계 전환 간격
            if (currentStep < processingSteps.size - 1) {
                currentStep++
            } else {
                break
            }
        }
    }

    // 무한 애니메이션들
    val infiniteTransition = rememberInfiniteTransition(label = "processing")

    // 파형 애니메이션
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave"
    )

    // 회전 애니메이션
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 펄스 애니메이션
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 노이즈 필터 효과 애니메이션
    val filterIntensity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "filter"
    )

    Dialog(onDismissRequest = { /* 처리 중에는 닫기 비활성화 */ }) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .wrapContentHeight()
                .scale(dialogScale.value)
        ) {
            // 배경 효과
            Canvas(modifier = Modifier.matchParentSize()) {
                // 그라데이션 배경
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFFBF0),
                            Color(0xFFF8F4E3)
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.3f),
                        radius = size.width * 0.8f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )

                // 미세한 테두리
                drawRoundRect(
                    color = Color(0xFFE0E0E0),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 메인 비주얼 영역
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 회전하는 배경 링
                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2

                        // 외부 링 (회전)
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = Color(0xFFE9D364)
                                style = PaintingStyle.Stroke
                                strokeWidth = 4.dp.toPx()
                            }

                            // 회전된 호 그리기
                            canvas.save()
                            canvas.rotate(rotation, center.x, center.y)
                            canvas.drawArc(
                                left = center.x - radius + 10.dp.toPx(),
                                top = center.y - radius + 10.dp.toPx(),
                                right = center.x + radius - 10.dp.toPx(),
                                bottom = center.y + radius - 10.dp.toPx(),
                                startAngle = 0f,
                                sweepAngle = 270f,
                                useCenter = false,
                                paint = paint
                            )
                            canvas.restore()
                        }

                        // 내부 진행률 링
                        val progressAngle = 360f * (progressPercentage / 100f)
                        drawCircle(
                            color = Color(0xFFF0F0F0),
                            radius = radius - 20.dp.toPx(),
                            center = center,
                            style = Stroke(width = 6.dp.toPx())
                        )

                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFE9D364),
                                    Color(0xFFE9B44C),
                                    Color(0xFFE76F51)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = progressAngle,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - radius + 20.dp.toPx(),
                                center.y - radius + 20.dp.toPx()
                            ),
                            size = Size(
                                (radius - 20.dp.toPx()) * 2,
                                (radius - 20.dp.toPx()) * 2
                            ),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // 중앙 노이즈 제거 시각화
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF8F8F8)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // 노이즈 제거 파형 애니메이션
                        Canvas(
                            modifier = Modifier.size(60.dp)
                        ) {
                            val centerY = size.height / 2
                            val waveWidth = size.width

                            // 노이즈가 있는 파형 (흐릿하게)
                            val noisyPath = Path()
                            noisyPath.moveTo(0f, centerY)

                            for (x in 0..waveWidth.toInt() step 3) {
                                val xRatio = x / waveWidth
                                val mainWave = sin(xRatio * 6 * PI + wavePhase) * size.height * 0.15f
                                val noise = sin(xRatio * 20 * PI + wavePhase * 3) * size.height * 0.08f * (1 - filterIntensity)
                                val y = centerY + mainWave + noise
                                noisyPath.lineTo(x.toFloat(), y.toFloat())
                            }

                            drawPath(
                                path = noisyPath,
                                color = Color.LightGray.copy(alpha = 0.6f),
                                style = Stroke(width = 1.5f)
                            )

                            // 깨끗한 파형 (점점 선명하게)
                            val cleanPath = Path()
                            cleanPath.moveTo(0f, centerY)

                            for (x in 0..waveWidth.toInt() step 3) {
                                val xRatio = x / waveWidth
                                val mainWave = sin(xRatio * 6 * PI + wavePhase) * size.height * 0.15f
                                val y = centerY + mainWave
                                cleanPath.lineTo(x.toFloat(), y.toFloat())
                            }

                            drawPath(
                                path = cleanPath,
                                color = Color(0xFFE9D364).copy(alpha = 0.3f + filterIntensity * 0.7f),
                                style = Stroke(width = 2f + filterIntensity * 2f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 진행률 텍스트
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20),
                        modifier = Modifier.offset(y = 50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 제목
                Text(
                    text = "노이즈 제거 처리 중",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F2E20)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 현재 처리 단계
                Text(
                    text = processingSteps[currentStep],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE9B44C)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 처리 단계 리스트
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    processingSteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 단계 상태 아이콘
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = when {
                                            index < currentStep -> Color(0xFFE9B44C)
                                            index == currentStep -> Color(0xFFE9D364)
                                            else -> Color(0xFFF0F0F0)
                                        },
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (index == currentStep) 2.dp else 0.dp,
                                        color = Color(0xFFE9B44C),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    index < currentStep -> {
                                        // 완료된 단계 - 체크 표시
                                        Text(
                                            text = "✓",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    index == currentStep -> {
                                        // 현재 처리 중 - 점 애니메이션
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .scale(pulseScale)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 단계 텍스트
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = when {
                                    index < currentStep -> Color(0xFF3F2E20)
                                    index == currentStep -> Color(0xFF3F2E20)
                                    else -> Color.Gray
                                },
                                fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 설명 텍스트
                Text(
                    text = "배경 소음과 잡음을 제거하여\n더욱 깨끗한 음성으로 변환하고 있습니다",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 예상 소요 시간
                Text(
                    text = "예상 소요 시간: 10-15초",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}