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
    val backgroundColor = Color(0xFFFFFBF0) // Light cream background color
    var isLoading by remember { mutableStateOf(false) }
    var musics by remember { mutableStateOf<List<MusicEntity>>(emptyList()) }
    var currentPlayingMusicId by remember { mutableStateOf<Long?>(null) }

    // For demo purposes - create some sample entities
    LaunchedEffect(Unit) {
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
                created_at = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            MusicEntity(
                music_id = 3,
                title = "잔잔한 피아노",
                music_path = "/sample/path/piano.mp3",
                attribute = "calm",
                created_at = System.currentTimeMillis() - 172800000 // 2 days ago
            )
        )
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "음악 리스트",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Add music button
        Button(
            onClick = { /* Add music functionality */ },
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

        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Music list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE9D364)
                    )
                }
            } else if (musics.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(musics) { music ->
                        MusicItemCard(
                            music = music,
                            isPlaying = currentPlayingMusicId == music.music_id,
                            onClick = {
                                currentPlayingMusicId = if (currentPlayingMusicId == music.music_id) null else music.music_id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusicItemCard(music: MusicEntity, isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Music icon or playing indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFE9D364) else Color(0xFFFFEED0)),
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

            // Text info (title, genre)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = music.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "장르: ${music.attribute}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Play icon
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.ic_volume_up else R.drawable.ic_play
                ),
                contentDescription = if (isPlaying) "Playing" else "Play",
                tint = if (isPlaying) Color(0xFFE9B44C) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
