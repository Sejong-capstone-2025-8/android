package com.toprunner.imagestory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.ui.theme.ImageStoryTheme

class SettingsActivity : ComponentActivity() {
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ImageStoryTheme {
                SettingsScreen(
                    onBackClicked = { finish() },
                    onAccountInfoClicked = {
                        Toast.makeText(this, "계정 정보 관리 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onNoticeClicked = {
                        Toast.makeText(this, "공지사항 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    },
                    onLogoutClicked = {
                        // 로그아웃 처리 (실제 구현에서는 사용자 세션 클리어 등의 작업 수행)
                        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onEmailClicked = {
                        // 이메일 앱 열기
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@imagestory.com"))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "맞춤형으로 읽어주는 사진 속 동화 여행 문의")
                        try {
                            startActivity(Intent.createChooser(intent, "이메일 보내기"))
                        } catch (e: Exception) {
                            Toast.makeText(this, "이메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onNavigationItemClicked = { screen ->
                        navigateToScreen(screen)
                    }
                )
            }
        }
    }

    private fun navigateToScreen(screen: String) {
        when (screen) {
            "home" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            "fairytale_list" -> {
                val intent = Intent(this, FairyTaleListActivity::class.java)
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
            else -> {
                Log.d(TAG, "Unknown screen: $screen")
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit,
    onAccountInfoClicked: () -> Unit,
    onNoticeClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onEmailClicked: () -> Unit,
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
                text = "설정",
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

            Icon(
                painter = painterResource(id = R.drawable.ic_email),
                contentDescription = "이메일",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { onEmailClicked() },
                tint = Color(0xFF9C8A54)
            )
        }

        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 계정 정보 버튼
        SettingsItem(
            icon = R.drawable.ic_account,
            title = "계정 정보 관리",
            onClick = onAccountInfoClicked
        )

        // 공지사항 버튼
        SettingsItem(
            icon = R.drawable.ic_notice,
            title = "공지사항 확인하기",
            onClick = onNoticeClicked
        )

        // 로그아웃 버튼
        SettingsItem(
            icon = R.drawable.ic_logout,
            title = "로그아웃",
            onClick = onLogoutClicked
        )

        // 버전 정보
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "앱 버전: 1.0.0",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

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
                    onClick = { onNavigationItemClicked("fairytale_list") }
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
                    isSelected = true,
                    onClick = { /* 현재 화면은 클릭 불필요 */ }
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFF9C8A54),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "More",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}