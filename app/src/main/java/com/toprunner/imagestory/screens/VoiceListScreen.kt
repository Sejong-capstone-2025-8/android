package com.toprunner.imagestory.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.service.TTSService
import com.toprunner.imagestory.service.VoiceCloneService
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.ui.components.ImprovedVoiceFeatureVisualization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke

import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceListScreen(
    navController: NavController,
    onRecordNewVoiceClicked: () -> Unit = {

    }
) {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFFFFBF0)
    var isLoading by remember { mutableStateOf(false) }
    var isCloneLoading by remember { mutableStateOf(false) }
    var voices by remember { mutableStateOf<List<VoiceEntity>>(emptyList()) }
    var cloneVoices by remember { mutableStateOf<List<VoiceEntity>>(emptyList()) }
    var currentPlayingVoiceId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var voiceToDelete by remember { mutableStateOf<VoiceEntity?>(null) }

    // 제목 편집 다이얼로그 상태
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var voiceToEdit by remember { mutableStateOf<VoiceEntity?>(null) }


    // 클론 음성 생성 관련 다이얼로그 상태
    var showCloneDialog by remember { mutableStateOf(false) }
    var selectedVoiceForClone by remember { mutableStateOf<VoiceEntity?>(null) }

    // 음성 특징 다이얼로그 상태
    var showFeaturesDialog by remember { mutableStateOf(false) }
    var selectedVoiceFeatures by remember { mutableStateOf<VoiceFeatures?>(null) }
    var selectedVoiceTitle by remember { mutableStateOf("") }
    var isLoadingFeatures by remember { mutableStateOf(false) }

    // TTSService 인스턴스를 하나 생성 (음성 재생용)
    val ttsService = remember { TTSService(context) }
    val repo = VoiceRepository(context)
    val voiceCloneService = remember { VoiceCloneService(context) }

    // 실제 DB에서 데이터 로드 (VoiceRepository를 사용하여 DB 연동)
    LaunchedEffect(Unit) {
        isLoading = true
        voices = repo.getAllVoices().filter { voice ->
            try {
                val attributeJson = JSONObject(voice.attribute)
                !attributeJson.optBoolean("isClone", false)
            } catch (e: Exception) {
                true // JSON 파싱 오류 시 기본 음성으로 취급
            }
        }

        cloneVoices = repo.getAllVoices().filter { voice ->
            try {
                val attributeJson = JSONObject(voice.attribute)
                attributeJson.optBoolean("isClone", false)
            } catch (e: Exception) {
                false // JSON 파싱 오류 시 클론 음성이 아닌 것으로 취급
            }
        }
        isLoading = false
    }

    // 삭제 함수 (VoiceRepository.deleteVoice를 호출)
    fun deleteVoice(voice: VoiceEntity) {
        // 삭제 확인 다이얼로그 표시
        voiceToDelete = voice
        showDeleteDialog = true
    }

    // 실제 삭제 수행 함수
    fun performDelete(voiceId: Long) {
        scope.launch {
            // 재생 중이면 중지
            if (currentPlayingVoiceId == voiceId) {
                ttsService.stopAudio()
                currentPlayingVoiceId = null
            }

            val success = repo.deleteVoice(voiceId)
            if (success) {
                // 기본 음성 및 클론 음성 목록 갱신
                voices = repo.getAllVoices().filter { voice ->
                    try {
                        val attributeJson = JSONObject(voice.attribute)
                        !attributeJson.optBoolean("isClone", false)
                    } catch (e: Exception) {
                        true
                    }
                }

                cloneVoices = repo.getAllVoices().filter { voice ->
                    try {
                        val attributeJson = JSONObject(voice.attribute)
                        attributeJson.optBoolean("isClone", false)
                    } catch (e: Exception) {
                        false
                    }
                }

                Toast.makeText(context, "음성이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 제목 업데이트 함수 (새로 추가)
    fun updateVoiceTitle(voice: VoiceEntity, newTitle: String) {
        scope.launch {
            try {
                val success = repo.updateVoiceTitle(voice.voice_id, newTitle)
                if (success) {
                    // 성공 시 목록 갱신
                    Toast.makeText(context, "제목이 변경되었습니다.", Toast.LENGTH_SHORT).show()

                    // 기본 음성 목록 갱신
                    voices = repo.getAllVoices().filter { v ->
                        try {
                            val attributeJson = JSONObject(v.attribute)
                            !attributeJson.optBoolean("isClone", false)
                        } catch (e: Exception) {
                            true
                        }
                    }

                    // 낭독용 음성 목록 갱신
                    cloneVoices = repo.getAllVoices().filter { v ->
                        try {
                            val attributeJson = JSONObject(v.attribute)
                            attributeJson.optBoolean("isClone", false)
                        } catch (e: Exception) {
                            false
                        }
                    }
                } else {
                    Toast.makeText(context, "제목 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 재생/정지 토글 함수
    fun toggleVoice(voice: VoiceEntity) = scope.launch {
        // 현재 재생 중인 목소리가 있다면 정지
        if (currentPlayingVoiceId != null && currentPlayingVoiceId != voice.voice_id) {
            ttsService.stopAudio()
            currentPlayingVoiceId = null
        }
        // 만약 선택된 목소리가 현재 재생 중이면 정지, 아니면 재생
        if (currentPlayingVoiceId == voice.voice_id) {
            // 정지
            val paused = ttsService.pauseAudio()
            if (paused) {
                currentPlayingVoiceId = null
            }
        } else {
            val success = ttsService.playAudio(voice.voice_path)
            if (success) {
                currentPlayingVoiceId = voice.voice_id
            } else {
                Toast.makeText(context, "음성 재생에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 음성 특징 로드 함수
    fun loadVoiceFeatures(voice: VoiceEntity) {
        scope.launch {
            isLoadingFeatures = true
            selectedVoiceTitle = voice.title

            try {
                Log.d("VoiceListScreen", "Loading voice features for voice ID: ${voice.voice_id}, Title: ${voice.title}")

                // 속성 JSON 로깅
                Log.d("VoiceListScreen", "Voice attribute: ${voice.attribute}")
                // VoiceRepository에서 음성 특징 로드
                withContext(Dispatchers.IO) {



                    // voiceRepository.getVoiceFeatures 메소드가 있는 경우
                    val features = repo.getVoiceFeatures(voice.voice_id)

                    withContext(Dispatchers.Main) {
                        selectedVoiceFeatures = features
                        showFeaturesDialog = true
                        isLoadingFeatures = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "음성 특징을 불러오는데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    isLoadingFeatures = false
                }
            }
        }
    }

    // 클론 음성 생성 함수
    fun createCloneVoice(sourceVoice: VoiceEntity) {
        scope.launch {
            isCloneLoading = true
            try {
                // 예시 텍스트 - 짧은 동화 텍스트
                val sampleText = "옛날 옛적에 작은 마을에 착한 아이가 살았습니다. 어느 날 숲속에서 길을 잃은 작은 새를 발견했어요. 아이는 새를 집으로 데려와 정성껏 돌봐주었답니다."

                // 기존 음성 파일을 voiceCloneService함수를 통해 클론 음성 생성
                val result = voiceCloneService.cloneVoice(
                    sourceVoicePath = sourceVoice.voice_path,
                    sampleText = sampleText,
                    name = "낭독용_${sourceVoice.title}"
                )

                if (result.first) {
                    // 성공: 새 클론 음성을 낭독용 음성 리스트에 추가
                    cloneVoices = repo.getAllVoices().filter { voice ->
                        try {
                            val attributeJson = JSONObject(voice.attribute)
                            attributeJson.optBoolean("isClone", false)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    Toast.makeText(context, "음성이 성공적으로 복제되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 실패: 오류 메시지 표시
                    Toast.makeText(context, "음성 복제 실패: ${result.second}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "음성 복제 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isCloneLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                // 네오모픽 스타일 헤더
                NeuomorphicBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFFFFBF0),
                    elevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "음성 리스트",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F2E20)
                        )
                    }
                }
            }

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                // 녹음하기 버튼
                NeuomorphicButton(
                    onClick = { onRecordNewVoiceClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    backgroundColor = Color(0xFFFFD165),
                    cornerRadius = 12.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = "녹음하기",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "녹음하기",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                // 낭독용 음성 생성 버튼
                NeuomorphicButton(
                    onClick = { showCloneDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .height(48.dp),
                    backgroundColor = Color(0xFFFFC52C),
                    cornerRadius = 12.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_up),
                            contentDescription = "낭독용 음성 생성",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "낭독용 음성 생성",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                // 2개의 섹션으로 나누기 위한 Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 음성 목록 헤더
                        Text(
                            text = "기본 음성",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // 기본 음성 목록
                        if (isLoading) {
                            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE9D364))
                            }
                        } else if (voices.isEmpty()) {
                            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                Text("저장된 음성이 없습니다.\n새로운 음성을 녹음해보세요!", textAlign = TextAlign.Center, color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(vertical = 4.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                //기본 음성 목록
                                items(voices) { voice ->
                                    VoiceItemCard(
                                        voice = voice,
                                        isPlaying = currentPlayingVoiceId == voice.voice_id,
                                        onClick = { },
                                        onDelete = { deleteVoice(voice) },
                                        onPlayClick = { toggleVoice(voice) },
                                        onShowFeaturesClick = { loadVoiceFeatures(voice) },
                                        onEditTitleClick = { // 제목 편집 콜백 추가
                                            voiceToEdit = voice
                                            showEditTitleDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 클론 음성 목록 헤더
                        Text(
                            text = "낭독용 음성",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // 클론 음성 목록
                        if (cloneVoices.isEmpty()) {
                            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                Text("저장된 낭독용 음성이 없습니다.\n낭독용 음성 생성 버튼으로 음성을 복제해보세요!", textAlign = TextAlign.Center, color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(vertical = 4.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(cloneVoices) { voice ->
                                    VoiceItemCard(
                                        voice = voice,
                                        isPlaying = currentPlayingVoiceId == voice.voice_id,
                                        onClick = { },
                                        onDelete = { deleteVoice(voice) },
                                        onPlayClick = { toggleVoice(voice) },
                                        onShowFeaturesClick = { loadVoiceFeatures(voice) },
                                        isClonedVoice = true,
                                        onEditTitleClick = { // 제목 편집 콜백 추가
                                            voiceToEdit = voice
                                            showEditTitleDialog = true
                                        }
                                    )
                                }
                            }
                        }

                    }
                }
            }

            // 낭독용 음성 생성 로딩 다이얼로그
            if (isCloneLoading) {
                Dialog(onDismissRequest = {}) {
                    PremiumVoiceCloneLoadingDialog()
                }
            }



            // 특징 로딩 표시기
            // 음성 특징 로딩 표시기
            if (isLoadingFeatures) {
                Dialog(onDismissRequest = {}) { // 로딩 중에는 닫기 비활성화
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 파형 애니메이션
                            SoundWaveAnimation(
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "음성 특징 분석 중",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "목소리의 고유한 특성을 분석하고 있습니다",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF4285F4),
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }
            // 제목 편집 다이얼로그
            if (showEditTitleDialog && voiceToEdit != null) {
                EditTitleDialog(
                    currentTitle = voiceToEdit!!.title,
                    onDismiss = {
                        showEditTitleDialog = false
                        voiceToEdit = null
                    },
                    onConfirm = { newTitle ->
                        voiceToEdit?.let { voice ->
                            updateVoiceTitle(voice, newTitle)
                        }
                        showEditTitleDialog = false
                        voiceToEdit = null
                    }
                )
            }
            // 삭제 확인 다이얼로그
            if (showDeleteDialog && voiceToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        voiceToDelete = null
                    },
                    title = { Text("음성 삭제") },
                    text = {
                        Text(
                            "\"${voiceToDelete?.title ?: ""}\" 음성을 삭제하시겠습니까?\n삭제한 음성은 복구할 수 없습니다."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                voiceToDelete?.let { voice ->
                                    performDelete(voice.voice_id)
                                }
                                showDeleteDialog = false
                                voiceToDelete = null
                            }
                        ) {
                            Text("삭제", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                voiceToDelete = null
                            }
                        ) {
                            Text("취소")
                        }
                    }
                )
            }
        }
    }

    // 클론 음성 생성 다이얼로그
    if (showCloneDialog) {
        Dialog(onDismissRequest = { showCloneDialog = false }) {
            EnhancedVoiceCloneDialog(
                voices = voices,
                selectedVoice = selectedVoiceForClone,
                onVoiceSelected = { selectedVoiceForClone = it },
                onConfirm = {
                    selectedVoiceForClone?.let {
                        showCloneDialog = false
                        createCloneVoice(it)
                    }
                },
                onDismiss = { showCloneDialog = false }
            )
        }

    }// 음성 특징 다이얼로그
    if (showFeaturesDialog && selectedVoiceFeatures != null) {
        Dialog(onDismissRequest = { showFeaturesDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "음성 특징 정보",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { showFeaturesDialog = false }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = selectedVoiceTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9C8A54),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 음성 특징 시각화 컴포넌트
                    ImprovedVoiceFeatureVisualization(voiceFeatures = selectedVoiceFeatures!!)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showFeaturesDialog = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE9D364)
                        )
                    ) {
                        Text("확인", color = Color.Black)
                    }
                }
            }
        }
    }
}

// 파형 애니메이션 컴포넌트
@Composable
fun SoundWaveAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sound wave")
    val waveBarCount = 5

    // 각 파형 바의 높이 애니메이션
    val waveHeights = List(waveBarCount) { index ->
        val delay = index * 100 // 각 바마다 약간의 딜레이
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.3f at 0
                    0.9f at 200
                    0.3f at 400
                    0.7f at 600
                    0.3f at 800
                    0.5f at 1000
                    0.3f at 1200
                },
                initialStartOffset = StartOffset(delay)
            ),
            label = "wave$index"
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            waveHeights.forEachIndexed { index, heightFactor ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .width(8.dp)
                        .fillMaxHeight(heightFactor.value)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4285F4), // 파랑
                                    Color(0xFFEA4335)  // 빨강
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun EnhancedVoiceCloneDialog(
    voices: List<VoiceEntity>,
    selectedVoice: VoiceEntity?,
    onVoiceSelected: (VoiceEntity) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // 애니메이션 효과
    val cardScale = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 배경 그라데이션 애니메이션 효과
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundAnimation")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "gradientRotation"
    )

    Box(
        modifier = Modifier
            .width(340.dp)
            .height(540.dp)
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            }
    ) {
        // 세련된 배경 효과
        Canvas(modifier = Modifier.matchParentSize()) {
            // 배경 그라데이션
            val radius = size.width * 1.5f
            val gradientCenterX = size.width / 2 + radius * cos(Math.toRadians(gradientAngle.toDouble())).toFloat() * 0.25f
            val gradientCenterY = size.height / 2 + radius * sin(Math.toRadians(gradientAngle.toDouble())).toFloat() * 0.25f

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFFFFBF0),
                        Color(0xFFF8F4E3)
                    ),
                    center = Offset(gradientCenterX, gradientCenterY),
                    radius = radius
                ),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                alpha = 0.95f
            )

            // 세련된 테두리
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFEEE8D5)
                    )
                ),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 상단 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 헤더 아이콘
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE9D364),
                                    Color(0xFFE9B44C)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_volume_up),
                        contentDescription = "Voice Clone",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    // 애니메이션 효과 (작은 파동)
                    val waveAlpha = remember { Animatable(0.6f) }
                    LaunchedEffect(Unit) {
                        while(true) {
                            waveAlpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(1000, easing = LinearEasing)
                            )
                            waveAlpha.snapTo(0.6f)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .scale(1.3f)
                            .alpha(waveAlpha.value)
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = waveAlpha.value),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "낭독용 음성 생성",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
                    )

                    Text(
                        text = "선택한 음성을 기반으로 낭독에 최적화된 음성을 만듭니다",
                        fontSize = 13.sp,
                        color = Color(0xFF6B5B4E)
                    )
                }

                // 닫기 버튼
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE).copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color(0xFF9C8A54),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFEEEEEE).copy(alpha = 0.5f),
                                Color(0xFF9C8A54).copy(alpha = 0.3f),
                                Color(0xFFEEEEEE).copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 설명 텍스트
            Text(
                text = "낭독용 음성 생성 과정",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3F2E20)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 단계별 안내
            StepsProgressView()

            Spacer(modifier = Modifier.height(24.dp))

            // 음성 선택 타이틀
            Text(
                text = "복제할 기본 음성 선택",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3F2E20)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 음성 목록 (스크롤 가능)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEEE8D5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                if (voices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "사용 가능한 음성이 없습니다.\n먼저 음성을 녹음해주세요.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        items(voices) { voice ->
                            EnhancedVoiceSelectionItem(
                                voice = voice,
                                isSelected = voice.voice_id == selectedVoice?.voice_id,
                                onSelected = { onVoiceSelected(voice) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 취소 버튼
                NeuomorphicButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    backgroundColor = Color(0xFFF5F5F5),
                    cornerRadius = 12.dp
                ) {
                    Text(
                        text = "취소",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                // 생성 버튼
                NeuomorphicButton(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    backgroundColor = Color(0xFFE9D364),
                    enabled = selectedVoice != null,
                    cornerRadius = 12.dp
                ) {
                    Text(
                        text = "음성 생성",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (selectedVoice != null) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StepsProgressView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 4단계 과정 표시
        val steps = listOf(
            "원본 분석" to "음성의 고유한 특성을 파악합니다",
            "모델 적용" to "낭독에 최적화된 모델로 변환합니다",
            "음성 합성" to "자연스러운 낭독 음성을 생성합니다",
            "최종 조정" to "이야기 낭독에 최적화합니다"
        )

        steps.forEachIndexed { index, (title, desc) ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    // 단계 번호 원
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE9D364),
                                        Color(0xFFE9B44C)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 단계 이름 (제한된 공간에서는 숨김)
                    if (index == 0 || index == 3) { // 첫번째와 마지막 단계만 표시
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3F2E20),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 단계 연결선
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .align(Alignment.CenterVertically)
                        .background(
                            Color(0xFFE9D364).copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun EnhancedVoiceSelectionItem(
    voice: VoiceEntity,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFEF9E7) else Color.Transparent
    val borderColor = if (isSelected) Color(0xFFE9B44C) else Color.Transparent

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 눌렸을 때 크기 변화
    val scale = if (isPressed) 0.98f else 1f

    // 애니메이션
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemScale"
    )

    // 선택 애니메이션
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectionScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onSelected() }
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 음성 아이콘
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEED0))
                        .graphicsLayer {
                            scaleX = selectionScale
                            scaleY = selectionScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Voice",
                        tint = Color(0xFFE9B44C),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 음성 정보
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voice.title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF3F2E20)
                    )

                    // 녹음 날짜 포맷팅
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(voice.created_at))

                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 선택 표시
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9D364))
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bookmark), // 체크 아이콘으로 변경
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceItemCard(
    voice: VoiceEntity,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPlayClick: () -> Unit,
    onShowFeaturesClick: () -> Unit,
    onEditTitleClick: () -> Unit,  // 제목 편집 콜백 추가
    isClonedVoice: Boolean = false
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(voice.created_at))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isClonedVoice) Color(0xFFFEF9E7) else Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 재생 버튼 (기존 코드 유지)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) {
                            R.drawable.ic_pause
                        } else if (isClonedVoice) {
                            R.drawable.ic_volume_up
                        } else {
                            R.drawable.ic_mic
                        }
                    ),
                    contentDescription = if (isPlaying) "Playing" else "Voice",
                    tint = if (isPlaying) Color.Black else Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // 제목 및 정보 (제목에 클릭 이벤트 추가)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onEditTitleClick() } // 제목 클릭 시 편집 기능 실행
                ) {
                    Text(
                        text = voice.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 편집 아이콘 추가
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit), // 편집 아이콘 필요
                        contentDescription = "편집",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isClonedVoice) {
                        Text(
                            text = "낭독용 음성",
                            fontSize = 12.sp,
                            color = Color(0xFFE9B44C),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFEED0),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = formattedDate, fontSize = 14.sp, color = Color.Gray)
                }
            }

            // 음성 특징 확인 버튼 (기존 코드 유지)
            IconButton(
                onClick = { onShowFeaturesClick() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Voice Features",
                    tint = Color(0xFF9C8A54),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 삭제 아이콘 (기존 코드 유지)
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 제목 편집 다이얼로그 컴포넌트
@Composable
fun EditTitleDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("음성 제목 수정") },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("새 제목") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle)
                    }
                },
                enabled = newTitle.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun PremiumVoiceCloneLoadingDialog() {
    // 다이얼로그 등장 애니메이션
    val dialogScale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 배경 그라데이션 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "bgRotation")
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 진행률 텍스트 애니메이션
    val progressState = remember { mutableStateOf(0) }
    val steps = listOf(
        "음성 특성 분석 중",
        "음색 정보 추출 중",
        "낭독 모델 적용 중",
        "음성 합성 처리 중",
        "최종 품질 향상 중"
    )

    LaunchedEffect(Unit) {
        while(true) {
            delay(2000)
            progressState.value = (progressState.value + 1) % steps.size
        }
    }

    // 물결 애니메이션 효과
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "wave1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave2"
    )

    // 파티클 효과 애니메이션
    val particlePositions = remember {
        List(20) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val distance = Random.nextFloat() * 0.5f + 0.5f
            val speed = Random.nextFloat() * 0.3f + 0.1f
            Triple(angle, distance, speed)
        }
    }

    val particleOffsets = particlePositions.map { (angle, distance, speed) ->
        val particleAnimation = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (5000 * speed).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle"
        )

        val progress = particleAnimation.value
        val radius = progress * distance * 100f
        val x = cos(angle) * radius
        val y = sin(angle) * radius

        Offset(x, y)
    }

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(420.dp)
            .graphicsLayer {
                scaleX = dialogScale.value
                scaleY = dialogScale.value
            }
    ) {
        // 배경 그라데이션 캔버스
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 회전하는 배경 그라데이션
            val radius = size.width * 1.5f
            val centerX = size.width / 2 + radius * cos(Math.toRadians(gradientRotation.toDouble())).toFloat() * 0.1f
            val centerY = size.height / 2 + radius * sin(Math.toRadians(gradientRotation.toDouble())).toFloat() * 0.1f

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFFFFBF0),
                        Color(0xFFFAF2DC)
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                cornerRadius = CornerRadius(24.dp.toPx()),
                alpha = 0.97f
            )

            // 반짝이는 테두리
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFEED8A3),
                        Color.White
                    ),
                    startY = 0f,
                    endY = size.height * (0.5f + 0.5f * sin(gradientRotation * 0.01f))
                ),
                cornerRadius = CornerRadius(24.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 메인 아이콘
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // 파동 효과
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.width / 2

                    // 메인 파동
                    for (i in 0 until 3) {
                        val phase = when (i) {
                            0 -> wavePhase1
                            1 -> wavePhase1 + PI.toFloat() * 0.6f
                            else -> wavePhase1 + PI.toFloat() * 1.2f
                        }

                        val waveProgress = (sin(phase) + 1) / 2
                        val radius = maxRadius * (0.5f + waveProgress * 0.5f)

                        drawCircle(
                            color = Color(0xFFFFD166).copy(alpha = 0.2f * (1 - waveProgress)),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // 파티클 효과
                    particleOffsets.forEachIndexed { index, offset ->
                        val particleSize = 4.dp.toPx() * (0.5f + Random.nextFloat() * 0.5f)
                        val particleAlpha = 0.6f - (offset.x.pow(2) + offset.y.pow(2)).pow(0.5f) / 100f

                        drawCircle(
                            color = Color(0xFFFFD166).copy(alpha = particleAlpha.coerceIn(0f, 0.6f)),
                            radius = particleSize / 2,
                            center = Offset(
                                center.x + offset.x,
                                center.y + offset.y
                            )
                        )
                    }
                }

                // 회전하는 음성 아이콘
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            rotationZ = gradientRotation * 0.2f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD166),
                                    Color(0xFFE9B44C)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFEED8A3),
                                    Color.White
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 음파 효과
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 5) {
                            val delay = i * 100
                            val barHeight by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = keyframes {
                                        durationMillis = 1000
                                        0.3f at 0 with LinearEasing
                                        0.8f at 200 with LinearEasing
                                        0.4f at 400 with LinearEasing
                                        0.7f at 600 with LinearEasing
                                        0.3f at 1000 with LinearEasing
                                    },
                                    initialStartOffset = StartOffset(delay)
                                ),
                                label = "bar$i"
                            )

                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(barHeight)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 로딩 텍스트
            Text(
                text = "낭독용 음성 생성 중",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F2E20)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 단계 표시
            Text(
                text = steps[progressState.value],
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE9B44C)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 진행 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                // 물결 효과가 있는 진행 바
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    val width = size.width
                    val height = size.height

                    // 기본 진행 정도 계산 (0-5 단계 중 현재 몇 단계인지)
                    val baseProgress = (progressState.value + 1).toFloat() / steps.size.toFloat()

                    // 물결 효과 추가
                    val path = Path()
                    path.moveTo(0f, 0f)
                    path.lineTo(0f, height)

                    val waveWidth = 20.dp.toPx()
                    val progressWidth = width * baseProgress

                    // 물결이 있는 아래쪽 경계선 그리기
                    for (x in 0 until progressWidth.toInt() step 2) {
                        val wavePhaseShift = (wavePhase2 + x.toFloat() / waveWidth) % (2 * PI.toFloat())
                        val y = height - sin(wavePhaseShift) * 3.dp.toPx()
                        path.lineTo(x.toFloat(), y)
                    }

                    // 나머지 경계 완성
                    path.lineTo(progressWidth, 0f)
                    path.close()

                    // 그라데이션으로 채우기
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD166),
                                Color(0xFFE9B44C),
                                Color(0xFFFFD166)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(progressWidth, 0f)
                        )
                    )

                    // 반짝이는 효과 추가
                    val glowPosition = (width * baseProgress - 20.dp.toPx()) *
                            (0.5f + 0.5f * sin(gradientRotation * 0.01f))

                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.8f),
                                Color.Transparent
                            ),
                            startX = glowPosition - 20.dp.toPx(),
                            endX = glowPosition + 20.dp.toPx()
                        ),
                        size = Size(40.dp.toPx(), height)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 프로세스 설명
            Text(
                text = "낭독용 음성 생성은 약 1분 정도 소요됩니다",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 단계적 프로세스 설명
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    steps.forEachIndexed { index, step ->
                        val isComplete = index < progressState.value
                        val isCurrent = index == progressState.value

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            // 단계 상태 표시
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isComplete) Color(0xFFE9B44C)
                                        else if (isCurrent) Color(0xFFFFD166)
                                        else Color(0xFFEEEEEE)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) Color(0xFFE9B44C) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isComplete) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bookmark), // 체크 아이콘으로 대체
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 단계 이름
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isComplete) Color(0xFF3F2E20).copy(alpha = 0.7f)
                                else if (isCurrent) Color(0xFF3F2E20)
                                else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}