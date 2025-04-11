package com.toprunner.imagestory.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.toprunner.imagestory.data.entity.MusicEntity

@Composable
fun MusicListScreen(navController: NavController) {
    val backgroundColor = Color(0xFFFFFBF0)
    var isLoading by remember { mutableStateOf(false) }
    var musics by remember { mutableStateOf<List<MusicEntity>>(emptyList()) }
    val context = LocalContext.current

    // 예제: 실제 데이터 대신 repository 를 통한 로딩을 구현해야 함.
    LaunchedEffect(Unit) {
        // 실제 음악 리스트 로드 로직 (여기서는 샘플 데이터 사용)
        musics = listOf(
            MusicEntity(
                music_id = 1,
                title = "평화로운 숲",
                music_path = "/sample/path/forest.mp3",
                attribute = "peaceful",
                created_at = System.currentTimeMillis()
            ),
            MusicEntity(
                music_id = 2,
                title = "신나는 모험",
                music_path = "/sample/path/adventure.mp3",
                attribute = "exciting",
                created_at = System.currentTimeMillis() - 86400000
            ),
            MusicEntity(
                music_id = 3,
                title = "잔잔한 피아노",
                music_path = "/sample/path/piano.mp3",
                attribute = "calm",
                created_at = System.currentTimeMillis() - 172800000
            )
        )
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "음악 리스트",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.5.dp)
        // 음악 추가 버튼
        Button(
            onClick = {
                // 디바이스의 오디오 파일 선택 로직 (파일 피커 사용)
                // 예: ActivityResultLauncher 호출하여 선택된 파일을 repository에 저장
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD166)
            ),
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
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE9D364))
                }
            } else if (musics.isEmpty()) {
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
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(musics) { music ->
                        MusicItemCard(
                            music = music,
                            isPlaying = false,  // 재생 상태는 실제 구현 필요
                            onClick = {
                                // 재생 기능 구현 또는 상세 음악 화면 이동
                            },
                            onDelete = {
                                // 음악 삭제 기능 호출 (MusicRepository.deleteMusic(music.music_id))
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MusicItemCard(music: MusicEntity, isPlaying: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEED0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_music
                    ),
                    contentDescription = if (isPlaying) "Playing" else "Music",
                    tint = if (isPlaying) Color.Black else Color(0xFFE9B44C),
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
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.ic_volume_up else R.drawable.ic_play),
                contentDescription = if (isPlaying) "Playing" else "Play",
                tint = if (isPlaying) Color(0xFFE9B44C) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

