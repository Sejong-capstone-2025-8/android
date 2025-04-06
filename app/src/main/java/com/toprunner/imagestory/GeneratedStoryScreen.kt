package com.toprunner.imagestory

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.ui.theme.ImageStoryTheme

@Composable
fun GeneratedStoryScreen(
    fairyTaleEntity: FairyTaleEntity? = null,
    storyContent: String = "",
    storyImage: Bitmap? = null,
    isPlaying: Boolean = false,
    onPlayClicked: () -> Unit = {},
    onPauseClicked: () -> Unit = {},
    onStopClicked: () -> Unit = {},
    onVoiceSelectClicked: () -> Unit = {},
    onMusicSelectClicked: () -> Unit = {},
    onVoiceRecommendClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    val context = LocalContext.current

    // 네트워크 오류 팝업 상태 관리
    var showNetworkErrorDialog by remember { mutableStateOf(false) }

    // 진행 상태 (UI 데모용)
    var playbackProgress by remember { mutableStateOf(0.43f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 상단 헤더 (생성된 동화 + back 버튼)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "생성된 동화",
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
                fontSize = 18.sp,
                color = Color(0xFF9C8A54) // 올리브/금색
            )
        }

        // 디바이더 라인
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // 이미지 영역 (책 이미지)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (storyImage != null) {
                Image(
                    bitmap = storyImage.asImageBitmap(),
                    contentDescription = "Story Image",
                    modifier = Modifier
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.example_image),
                    contentDescription = "Default Story Image",
                    modifier = Modifier
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 동화 제목 및 나레이터 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fairyTaleEntity?.title ?: "동화 제목",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "narrated by AI Voice",
                fontSize = 16.sp,
                color = Color(0xFFAA8866), // 베이지/갈색
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 오디오 진행 상태 바
        Slider(
            value = playbackProgress,
            onValueChange = { playbackProgress = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(20.dp), // 전체 높이를 줄임
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE9D364), // 노란색 썸네일
                activeTrackColor = Color(0xFFE9D364), // 노란색 활성 트랙
                inactiveTrackColor = Color(0xFFE0E0E0), // 회색 비활성 트랙
            )
        )

        // 시간 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(playbackProgress * 100).toInt()}%",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = "2:23", // 실제 앱에서는 계산된 시간 표시
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // 재생 컨트롤 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 재생/일시정지 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .clickable { if (isPlaying) onPauseClicked() else onPlayClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Warning else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // 정지 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .clickable { onStopClicked() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Black)
                )
            }
        }

        // 목소리 설정 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 목소리 선택 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVoiceSelectClicked() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "목소리 선택",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }

            // 디바이더 라인
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 배경음 설정 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMusicSelectClicked() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "배경음 설정",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }

            // 디바이더 라인
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )


            // 목소리 추천 버튼 수정 - 더 명확한 버튼 형태로
            Button(
                onClick = { onVoiceRecommendClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF5F5F5), // 밝은 회색 배경
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 2.dp
                )
            ) {
                Text(
                    text = "목소리 추천",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            // 디바이더 라인
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 동화 텍스트 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (storyContent.isNotEmpty()) {
                    // 동화 텍스트를 줄바꿈 기준으로 분리
                    val storyLines = storyContent.split("\n")
                    storyLines.forEach { line ->
                        if (line.isNotEmpty()) {
                            Text(
                                text = line,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    // 기본 텍스트 표시
                    Text(
                        text = "동화 텍스트가 준비 중입니다...",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
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
                    isSelected = true,
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                    }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "동화 리스트",
                    tint = iconTint,
                    onClick = {
                        // 동화 목록 화면으로 이동 (FairyTaleListActivity 가정)
                        val intent = Intent(context, FairyTaleListActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "목소리 리스트",
                    tint = iconTint,
                    onClick = {
                        // 목소리 목록 화면으로 이동 (VoiceListActivity 가정)
                        val intent = Intent(context, VoiceListActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_music,
                    text = "음악 리스트",
                    tint = iconTint,
                    onClick = {
                        // 음악 목록 화면으로 이동 (MusicListActivity 가정)
                        val intent = Intent(context, MusicListActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_settings,
                    text = "설정",
                    tint = iconTint,
                    onClick = {
                        // 설정 화면으로 이동
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // 네트워크 오류 팝업 대화상자
    if (showNetworkErrorDialog) {
        Dialog(onDismissRequest = { showNetworkErrorDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,  // 네트워크 오류 아이콘으로 변경
                        contentDescription = "네트워크 오류",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "네트워크 상태를 확인하고 다시 시도해주세요",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showNetworkErrorDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE9D364),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "확인",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun BottomNavItem(
//    iconResId: Int,
//    text: String,
//    tint: Color = Color(0xFFAA8866),
//    isSelected: Boolean = false
//) {
//    val textColor = if (isSelected) Color.Black else Color(0xFF666666)
//    val iconTint = if (isSelected) Color.Black else tint
//    val fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier.padding(horizontal = 4.dp)
//    ) {
//        Icon(
//            painter = painterResource(id = iconResId),
//            contentDescription = text,
//            modifier = Modifier.size(24.dp),
//            tint = iconTint
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = text,
//            fontSize = 12.sp,
//            color = textColor,
//            textAlign = TextAlign.Center,
//            fontWeight = fontWeight
//        )
//    }
//}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun GeneratedStoryScreenPreview() {
    ImageStoryTheme {
        GeneratedStoryScreen(
            storyContent = "옛날 옛적, 깊은 숲속에 작은 마을이 있었습니다.\n이 마을에 살던 소녀는 매일 아침 숲을 거닐며 자연과 이야기를 나누곤 했습니다.\n어느 날, 소녀는 숲속에서 반짝이는 빛을 발견했습니다..."
        )
    }
}