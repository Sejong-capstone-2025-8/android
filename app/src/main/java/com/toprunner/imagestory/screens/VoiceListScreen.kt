package com.toprunner.imagestory.screens

import android.util.Log
import android.widget.Toast
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
                    name = "복제음성_${sourceVoice.title}"
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
                    .padding(horizontal = 16.dp, vertical = 16.dp), // 여기서 vertical 패딩을 줄임 (16.dp에서 8.dp로)
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "음성 리스트",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                // 녹음하기 버튼
                Button(
                    onClick = { onRecordNewVoiceClicked() },//mainactivity에서 전달 받은 콜백 사용
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                    shape = RoundedCornerShape(12.dp)
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

                // 낭독용 음성 생성 버튼
                Button(
                    onClick = { showCloneDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9D364)),
                    shape = RoundedCornerShape(12.dp)
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
                                        onClick = { }, // 더 이상 사용하지 않음
                                        onDelete = { deleteVoice(voice) },
                                        onPlayClick = { toggleVoice(voice) },
                                        onShowFeaturesClick = { loadVoiceFeatures(voice) }
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
                        if (isCloneLoading) {
                            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color(0xFFE9D364))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "낭독용 음성을 생성 중입니다...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else if (cloneVoices.isEmpty()) {
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
                                        onClick = { }, // 더 이상 사용하지 않음
                                        onDelete = { deleteVoice(voice) },
                                        onPlayClick = { toggleVoice(voice) },
                                        onShowFeaturesClick = { loadVoiceFeatures(voice) },
                                        isClonedVoice = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 전체 로딩 표시기 (클론 생성 중)
            if (isCloneLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE9D364))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "낭독용 음성을 생성 중입니다...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "잠시만 기다려 주세요.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 특징 로딩 표시기
            if (isLoadingFeatures) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "낭독용 음성 생성",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "복제할 기본 음성을 선택해주세요",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp
                    )

                    // 기본 음성 목록을 스크롤 가능한 리스트로 표시
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE9D364))
                        }
                    } else if (voices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(voices) { voice ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedVoiceForClone = voice
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedVoiceForClone?.voice_id == voice.voice_id)
                                            Color(0xFFFFEED0) else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 1.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFEED0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_mic),
                                                contentDescription = "Voice",
                                                tint = Color(0xFFE9B44C),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = voice.title,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (selectedVoiceForClone?.voice_id == voice.voice_id) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_settings), // 체크 아이콘 필요 (임시로 settings 사용)
                                                contentDescription = "Selected",
                                                tint = Color(0xFFE9B44C),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 버튼 영역
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { showCloneDialog = false }
                        ) {
                            Text("취소", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                selectedVoiceForClone?.let {
                                    showCloneDialog = false
                                    createCloneVoice(it)
                                }
                            },
                            enabled = selectedVoiceForClone != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE9D364),
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Text("음성 생성", color = if (selectedVoiceForClone != null) Color.Black else Color.Gray)
                        }
                    }
                }
            }
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
                                painter = painterResource(id = R.drawable.ic_stop),
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


@Composable
fun VoiceItemCard(
    voice: VoiceEntity,
    isPlaying: Boolean,
    onClick: () -> Unit, // 카드 전체 클릭 -> 이제 사용하지 않음
    onDelete: () -> Unit,
    onPlayClick: () -> Unit, // 음성 재생 버튼 클릭
    onShowFeaturesClick: () -> Unit, // 음성 특징 보기 버튼 클릭
    isClonedVoice: Boolean = false
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(voice.created_at))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        // clickable 제거 - 전체 카드 클릭으로 재생되지 않음
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
            // 아이콘 영역 - 클릭 시 재생
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0))
                    .clickable { onPlayClick() }, // 재생 버튼 클릭 이벤트
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voice.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

            // 음성 특징 확인 버튼 추가
            IconButton(
                onClick = { onShowFeaturesClick() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info), // 정보 아이콘 필요
                    contentDescription = "Voice Features",
                    tint = Color(0xFF9C8A54),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 삭제 아이콘
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

            // 재생 아이콘 (제거 - 왼쪽의 스피커 아이콘으로 대체됨)
        }
    }
}