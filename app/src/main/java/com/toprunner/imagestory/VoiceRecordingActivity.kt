package com.toprunner.imagestory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import com.toprunner.imagestory.util.VoiceFeaturesUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VoiceRecordingActivity : ComponentActivity() {
    private val TAG = "VoiceRecordingActivity"
    private val voiceRepository by lazy { VoiceRepository(this) }
    private val voiceFeaturesUtil by lazy { VoiceFeaturesUtil() }

    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var isRecording by mutableStateOf(false)
    private var recordingProgress by mutableStateOf(0)
    private var recordingTimeSeconds by mutableStateOf(0)
    private var sampleText by mutableStateOf("안녕하세요. 이 텍스트를 읽어주세요. 이 녹음은 당신의 목소리로 동화를 읽어주기 위한 샘플로 사용됩니다. 천천히 또박또박 읽어주세요.")

    // 녹음 권한 요청
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되었을 때 녹음 시작
            startRecording()
        } else {
            Toast.makeText(this, "음성 녹음을 위해 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ImageStoryTheme {
                VoiceRecordingScreen(
                    isRecording = isRecording,
                    recordingProgress = recordingProgress,
                    recordingTimeSeconds = recordingTimeSeconds,
                    sampleText = sampleText,
                    onBackClicked = { onBackPressed() },
                    onStartRecordingClicked = { checkPermissionAndStartRecording() },
                    onStopRecordingClicked = { stopRecording() },
                    onCompleteClicked = { saveRecordingAndFinish() }
                )
            }
        }
    }

    private fun checkPermissionAndStartRecording() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startRecording() {
        try {
            // 이전 레코더 해제
            releaseRecorder()

            // 녹음 파일 생성
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val recordingFileName = "VOICE_${timeStamp}.m4a"
            val recordingDir = File(filesDir, "voice_recordings").apply {
                if (!exists()) mkdirs()
            }
            recordingFile = File(recordingDir, recordingFileName)

            // 레코더 설정
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(recordingFile?.absolutePath)

                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(TAG, "prepare() failed: ${e.message}", e)
                    throw e
                }

                start()
            }

            // 녹음 상태 업데이트
            isRecording = true
            recordingTimeSeconds = 0
            recordingProgress = 0

            // 녹음 시간 업데이트를 위한 코루틴
            lifecycleScope.launch {
                while (isRecording) {
                    delay(1000) // 1초마다 업데이트
                    recordingTimeSeconds++
                    recordingProgress = (recordingTimeSeconds * 100 / 60).coerceAtMost(100) // 최대 60초 기준

                    // 60초 이상이면 자동으로 녹음 중지
                    if (recordingTimeSeconds >= 60) {
                        stopRecording()
                    }
                }
            }

            Toast.makeText(this, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "startRecording error: ${e.message}", e)
            Toast.makeText(this, "녹음을 시작할 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            isRecording = false
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                recorder?.apply {
                    stop()
                    release()
                }
                recorder = null
                isRecording = false

                Toast.makeText(this, "녹음이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "stopRecording error: ${e.message}", e)
                Toast.makeText(this, "녹음을 중지하는데 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRecordingAndFinish() {
        if (isRecording) {
            stopRecording()
        }

        if (recordingFile != null && recordingFile!!.exists() && recordingFile!!.length() > 0) {
            lifecycleScope.launch {
                try {
                    // 녹음 파일 읽기
                    val audioData = recordingFile!!.readBytes()

                    // 목소리 특성 분석 (실제로는 더 복잡한 분석 필요)
                    // 여기서는 간단한 더미 데이터를 사용
                    val voiceFeatures = VoiceFeatures(
                        averagePitch = 120.0 + (Math.random() * 40), // 랜덤 피치
                        pitchStdDev = 10.0 + (Math.random() * 10),   // 랜덤 표준편차
                        mfccValues = listOf(DoubleArray(13) { Math.random() })
                    )

                    // 목소리 제목 생성
                    val timeStamp = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date())
                    val title = "내 목소리 - $timeStamp"

                    // 속성 정보
                    val attributeJson = """
                        {
                            "voiceType": "custom",
                            "elevenlabsVoiceId": "custom"
                        }
                    """.trimIndent()

                    // 저장
                    val voiceId = voiceRepository.saveVoice(
                        title = title,
                        attributeJson = attributeJson,
                        audioData = audioData,
                        voiceFeatures = voiceFeatures
                    )

                    Toast.makeText(this@VoiceRecordingActivity, "목소리가 저장되었습니다.", Toast.LENGTH_SHORT).show()

                    // 목소리 리스트 화면으로 이동
                    val intent = Intent(this@VoiceRecordingActivity, VoiceListActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving voice: ${e.message}", e)
                    Toast.makeText(this@VoiceRecordingActivity, "목소리 저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "유효한 녹음 파일이 없습니다. 녹음을 먼저 완료해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun releaseRecorder() {
        try {
            recorder?.apply {
                if (isRecording) {
                    stop()
                }
                release()
            }
            recorder = null
            isRecording = false
        } catch (e: Exception) {
            Log.e(TAG, "releaseRecorder error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRecorder()
    }
}

@Composable
fun VoiceRecordingScreen(
    isRecording: Boolean,
    recordingProgress: Int,
    recordingTimeSeconds: Int,
    sampleText: String,
    onBackClicked: () -> Unit,
    onStartRecordingClicked: () -> Unit,
    onStopRecordingClicked: () -> Unit,
    onCompleteClicked: () -> Unit
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    val scrollState = rememberScrollState()

    // 녹음 시간 포맷
    val minutes = recordingTimeSeconds / 60
    val seconds = recordingTimeSeconds % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
                    .clickable { onBackClicked() },
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

        // 녹음 컨트롤 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 녹음 시작/정지 버튼
            FloatingActionButton(
                onClick = {
                    if (isRecording) onStopRecordingClicked() else onStartRecordingClicked()
                },
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
            onClick = { onCompleteClicked() },
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