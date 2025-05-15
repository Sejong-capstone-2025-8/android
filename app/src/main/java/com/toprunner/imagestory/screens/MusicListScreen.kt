package com.toprunner.imagestory.screens

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.MusicEntity
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.GeneratedStoryViewModel
import com.toprunner.imagestory.repository.FairyTaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
    navController: NavController,
    viewModel: GeneratedStoryViewModel? = null,
    storyId: Long = 0L,
    onNavigateToStory: (Long) -> Unit = {}
) {
    val backgroundColor = Color(0xFFFFFBF0)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Room DB & DAO
    val db = remember { AppDatabase.getInstance(context) }
    val musicDao = remember { db.musicDao() }

    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var musics by remember { mutableStateOf<List<MusicEntity>>(emptyList()) }

    // Playback state
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingId by remember { mutableStateOf<Long?>(null) }

    // Dialog state for input
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputAttr by remember { mutableStateOf("") }
    var genreExpanded by remember { mutableStateOf(false) }
    val genres = listOf("판타지", "사랑", "SF", "공포", "코미디")

    // 생성된 동화화면에서 선택된 음악
    var selectedMusicPath by remember { mutableStateOf<String?>(null) }
    var selectedMusicId by remember { mutableStateOf<Long?>(null) }

    // DB 초기 로딩
    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            musics = musicDao.getAllMusic()
        }
        isLoading = false
    }

    // File picker
    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingUri = it
            inputTitle = ""
            inputAttr = ""
            genreExpanded = false
            showDialog = true
        }
    }

    // 음악 선택 완료 및 이전 화면으로 돌아가기
    fun completeMusicSelection() {
        if (selectedMusicPath != null && viewModel != null) {
            try {
                // 음악 파일 존재 확인
                val file = File(selectedMusicPath!!)
                if (!file.exists()) {
                    Toast.makeText(context, "음악 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                // 뷰모델에 선택된 배경음 경로 설정
                viewModel.setBackgroundMusicPath(selectedMusicPath!!)
                Log.d("MusicListScreen", "배경음 설정 완료: $selectedMusicPath")

                // 토스트 메시지로 사용자에게 알림
                Toast.makeText(context, "배경음이 설정되었습니다.", Toast.LENGTH_SHORT).show()

                // Save BGM path to story attributes if needed - This is optional but helps persistence
                if (storyId > 0) {
                    // You can add this logic to save BGM path to story attributes
                    scope.launch {
                        try {
                            // This is optional - save BGM path to story attributes for persistence
                            val fairyTaleRepository = FairyTaleRepository(context)
                            val story = fairyTaleRepository.getFairyTaleById(storyId)?.first
                            if (story != null) {
                                val attributeJson = JSONObject(story.attribute)
                                attributeJson.put("bgmPath", selectedMusicPath)
                                val updatedStory = story.copy(attribute = attributeJson.toString())
                                fairyTaleRepository.updateFairyTale(updatedStory)
                                Log.d("MusicListScreen", "Saved BGM path to story attributes")
                            }
                        } catch (e: Exception) {
                            Log.e("MusicListScreen", "Error saving BGM path to attributes: ${e.message}")
                        }
                    }
                }

                // 동화 화면으로 다시 이동 (storyId가 유효한 경우)
                if (storyId > 0) {
                    // Use a clear navigation command to replace current page with new parameters
                    navController.navigate(NavRoute.GeneratedStory.createRoute(storyId, selectedMusicPath)) {
                        // This removes the current screen from back stack to avoid duplication
                        popUpTo(navController.currentBackStackEntry?.destination?.route ?: "") {
                            inclusive = true
                        }
                    }
                } else {
                    navController.navigateUp()
                }
            } catch (e: Exception) {
                Log.e("MusicListScreen", "배경음 설정 오류: ${e.message}", e)
                Toast.makeText(context, "배경음 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 음악이 선택되지 않은 경우
            Toast.makeText(context, "배경음을 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
    // AlertDialog for title & genre input
    if (showDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false; pendingUri = null },
            title = { Text("음악 정보 입력") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("제목") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    // ExposedDropdownMenuBox 로 장르 선택
                    ExposedDropdownMenuBox(
                        expanded = genreExpanded,
                        onExpandedChange = { genreExpanded = !genreExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = inputAttr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("장르") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()      // 메뉴 위치 고정
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = genreExpanded,
                            onDismissRequest = { genreExpanded = false }
                        ) {
                            genres.forEach { genre ->
                                DropdownMenuItem(
                                    text = { Text(genre) },
                                    onClick = {
                                        inputAttr = genre
                                        genreExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        pendingUri?.let { uri ->
                            scope.launch {
                                isLoading = true
                                withContext(Dispatchers.IO) {
                                    // Copy to internal storage
                                    val filename = "music_${System.currentTimeMillis()}.mp3"
                                    val outFile = File(context.filesDir, filename)
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        outFile.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    // Save to DB with user input
                                    val musicId = musicDao.insertMusic(
                                        MusicEntity(
                                            title = inputTitle.ifBlank { filename },
                                            music_path = outFile.absolutePath,
                                            attribute = inputAttr,
                                            created_at = System.currentTimeMillis()
                                        )
                                    )
                                    musics = musicDao.getAllMusic()

                                    // Set as selected music if adding for first time
                                    if (selectedMusicId == null) {
                                        selectedMusicPath = outFile.absolutePath
                                        selectedMusicId = musicId
                                    }
                                }
                                isLoading = false
                            }
                        }
                        pendingUri = null
                    },
                    enabled = inputTitle.isNotBlank() && inputAttr.isNotBlank()
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    pendingUri = null
                }) {
                    Text("취소")
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "배경음 선택",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 뒤로가기 버튼
            Text(
                text = "뒤로",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { navController.navigateUp() },
                fontSize = 16.sp,
                color = Color(0xFF9C8A54)
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.5.dp)

        // 현재 선택된 배경음 표시
        if (selectedMusicId != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7F7)),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "선택된 배경음",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 선택된 음악의 제목 표시
                    val selectedMusic = musics.find { it.music_id == selectedMusicId }
                    Text(
                        text = selectedMusic?.title ?: "알 수 없는 음악",
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Add Music Button
        Button(
            onClick = { pickAudioLauncher.launch("audio/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music),
                contentDescription = "Add Music",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "음악 추가하기",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        // 선택 완료 버튼
        Button(
            onClick = { completeMusicSelection() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9ED8D8),
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedMusicId != null
        ) {
            Text(
                text = "선택 완료",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE9D364))
                    }
                }
                musics.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_music),
                            contentDescription = "No music",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "저장된 음악이 없습니다.\n새로운 음악을 추가해보세요!",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(musics, key = { it.music_id }) { music ->
                            MusicItemCard(
                                music = music,
                                isPlaying = playingId == music.music_id,
                                isSelected = selectedMusicId == music.music_id,
                                onClick = {
                                    // 음악 선택 로직
                                    selectedMusicPath = music.music_path
                                    selectedMusicId = music.music_id

                                    // 기존 재생 중인 음악 중지
                                    if (playingId != null) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        playingId = null
                                    }
                                },
                                onPlayClick = {
                                    if (playingId == music.music_id) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        playingId = null
                                    } else {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(music.music_path)
                                            prepare()
                                            start()
                                            setOnCompletionListener {
                                                it.release()
                                                mediaPlayer = null
                                                playingId = null
                                            }
                                        }
                                        playingId = music.music_id
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        if (playingId == music.music_id) {
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            playingId = null
                                        }

                                        // 선택된 음악이 삭제되면 선택 취소
                                        if (selectedMusicId == music.music_id) {
                                            selectedMusicId = null
                                            selectedMusicPath = null
                                        }

                                        withContext(Dispatchers.IO) {
                                            musicDao.deleteMusic(music.music_id)
                                            musics = musicDao.getAllMusic()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Clean up on leaving screen
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}

@Composable
fun MusicItemCard(
    music: MusicEntity,
    isPlaying: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBackground = when {
        isSelected -> Color(0xFFE0F2F1)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 플레이 버튼 영역
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE76F51) else Color(0xFFFFEED0))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_music
                    ),
                    contentDescription = if (isPlaying) "Playing" else "Music",
                    tint = if (isPlaying) Color.White else Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "장르: ${music.attribute}", fontSize = 14.sp, color = Color.Gray)
            }

            // 선택 상태 표시
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bookmark),
                    contentDescription = "Selected",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 삭제 버튼
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() }
            )
        }
    }
}