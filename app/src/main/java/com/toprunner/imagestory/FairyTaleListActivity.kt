package com.toprunner.imagestory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FairyTaleListActivity : ComponentActivity() {
    private val fairyTaleRepository by lazy { FairyTaleRepository(this) }
    private var fairyTales by mutableStateOf<List<FairyTaleEntity>>(emptyList())
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadFairyTales()

        setContent {
            ImageStoryTheme {
                FairyTaleListScreen(
                    fairyTales = fairyTales,
                    isLoading = isLoading,
                    onBackClicked = { finish() },
                    onFairyTaleClicked = { fairyTaleId ->
                        navigateToStoryScreen(fairyTaleId)
                    },
                    onNavigationItemClicked = { screen ->
                        navigateToScreen(screen)
                    }
                )
            }
        }
    }

    private fun loadFairyTales() {
        isLoading = true
        lifecycleScope.launch {
            try {
                fairyTales = fairyTaleRepository.getAllFairyTales().sortedByDescending { it.created_at }
                isLoading = false
            } catch (e: Exception) {
                Log.e("FairyTaleListActivity", "Error loading fairy tales: ${e.message}", e)
                Toast.makeText(this@FairyTaleListActivity, "동화 목록을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    private fun navigateToStoryScreen(fairyTaleId: Long) {
        val intent = Intent(this, GeneratedStoryActivity::class.java).apply {
            putExtra("STORY_ID", fairyTaleId)
        }
        startActivity(intent)
    }

    private fun navigateToScreen(screen: String) {
        when (screen) {
            "home" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            "voice_list" -> {
                val intent = Intent(this, VoiceListActivity::class.java)
                startActivity(intent)
                finish()
            }
            "music_list" -> {
                val intent = Intent(this, MusicListActivity::class.java)
                startActivity(intent)
                finish()
            }
            "settings" -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> {
                Log.d("Navigation", "Unknown screen: $screen")
            }
        }
    }
}

@Composable
fun FairyTaleListScreen(
    fairyTales: List<FairyTaleEntity>,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onFairyTaleClicked: (Long) -> Unit,
    onNavigationItemClicked: (String) -> Unit
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경

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
                text = "동화 리스트",
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

        // 동화 목록
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
            } else if (fairyTales.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bookmark),
                        contentDescription = "No fairy tales",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "저장된 동화가 없습니다.\n새로운 동화를 만들어보세요!",
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
                    items(fairyTales) { fairyTale ->
                        FairyTaleItem(
                            fairyTale = fairyTale,
                            onClick = { onFairyTaleClicked(fairyTale.fairy_tales_id) }
                        )
                    }
                }
            }
        }

        // 하단 네비게이션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconTint = Color(0xFFAA8866) // 베이지/갈색

                BottomNavItem(
                    iconResId = R.drawable.ic_home,
                    text = "홈화면",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("home") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "동화 리스트",
                    tint = iconTint,
                    isSelected = true,
                    onClick = { /* 현재 화면은 클릭 불필요 */ }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "목소리 리스트",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("voice_list") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_music,
                    text = "음악 리스트",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("music_list") }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_settings,
                    text = "설정",
                    tint = iconTint,
                    onClick = { onNavigationItemClicked("settings") }
                )
            }
        }
    }
}

@Composable
fun FairyTaleItem(
    fairyTale: FairyTaleEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(fairyTale.created_at))

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
            // 마이크 아이콘 (동화 아이콘)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFEED0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bookmark),
                    contentDescription = "Fairy Tale",
                    tint = Color(0xFFE9B44C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보 (제목, 날짜)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fairyTale.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // 우측 화살표
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Open",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}