package com.toprunner.imagestory.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.VoiceRepository
import com.toprunner.imagestory.service.TTSService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceListScreen(navController: NavController,onRecordNewVoiceClicked: () -> Unit = {}) {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFFFFBF0) // Light cream background color
    var isLoading by remember { mutableStateOf(false) }
    var voices by remember { mutableStateOf<List<VoiceEntity>>(emptyList()) }
    var currentPlayingVoiceId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    // TTSService 인스턴스를 하나 생성 (음성 재생용)
    val ttsService = remember { TTSService(context) }
    val repo = VoiceRepository(context)

    // 실제 DB에서 데이터 로드 (VoiceRepository를 사용하여 DB 연동)
    LaunchedEffect(Unit) {
        val repo = VoiceRepository(context)
        voices = repo.getAllVoices()  // 실제 DB에서 불러오는 코드
        isLoading = false
    }
    // 삭제 함수 (VoiceRepository.deleteVoice를 호출)
    fun deleteVoice(voiceId: Long) {
        scope.launch {
            val success = repo.deleteVoice(voiceId)
            if (success) {
                voices = repo.getAllVoices()
            } else {
                Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 재생/정지 토글 함수
    fun toggleVoice(voice: VoiceEntity) {
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
    LaunchedEffect(Unit) {
        voices = repo.getAllVoices()
        isLoading = false
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("목소리 리스트", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            Column {
                // 녹음하기 버튼
                Button(
                    onClick = { onRecordNewVoiceClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE9D364))
                    }
                } else if (voices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("저장된 목소리가 없습니다.\n새로운 목소리를 녹음해보세요!", textAlign = TextAlign.Center, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(voices) { voice ->
                            VoiceItemCard(
                                voice = voice,
                                isPlaying = currentPlayingVoiceId == voice.voice_id,
                                onClick = { toggleVoice(voice) },
                                onDelete = { deleteVoice(voice.voice_id) }
                            )
                        }
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
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(voice.created_at))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 영역 (녹음 아이콘, 재생 상태에 따라 표시)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_mic),
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
                Text(text = formattedDate, fontSize = 14.sp, color = Color.Gray)
            }
            // 삭제 아이콘
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 재생 아이콘 (음량 아이콘)
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.ic_volume_up else R.drawable.ic_play),
                contentDescription = if (isPlaying) "Playing" else "Play",
                tint = if (isPlaying) Color(0xFFE9B44C) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

