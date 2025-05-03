package com.toprunner.imagestory.screens

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.data.entity.MusicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicManagerScreen() {
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

    // Initial load from DB
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
                                .menuAnchor()
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
                                    musicDao.insertMusic(
                                        MusicEntity(
                                            title = inputTitle.ifBlank { filename },
                                            music_path = outFile.absolutePath,
                                            attribute = inputAttr,
                                            created_at = System.currentTimeMillis()
                                        )
                                    )
                                    musics = musicDao.getAllMusic()
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
                        text = "음악 리스트",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
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
                                isSelected = false,
                                onClick = { /* Nothing to do here */ },
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