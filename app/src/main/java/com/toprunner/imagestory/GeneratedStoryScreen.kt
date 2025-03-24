package com.toprunner.imagestory

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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.ui.theme.ImageStoryTheme

@Composable
fun GeneratedStoryScreen() {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 상단 헤더 (생성된 동화 + back 버튼)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = "생성된 동화",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "back",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { /* 뒤로가기 처리 */ },
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
            Image(
                painter = painterResource(id = R.drawable.example_image),
                contentDescription = "Enchanted Book",
                modifier = Modifier
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // 동화 제목 및 나레이터 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Enchanted Book",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Narrated by Missy Ginn",
                fontSize = 16.sp,
                color = Color(0xFFAA8866), // 베이지/갈색
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 오디오 진행 상태 바
        Slider(
            value = 0.43f, // 약 43% 진행 중
            onValueChange = { /* 진행 상태 변경 처리 */ },
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
                text = "1:17",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = "2:23",
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
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Star else Icons.Filled.PlayArrow,
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
                    .clickable { /* 정지 처리 */ },
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
                    .clickable { /* 목소리 선택 화면으로 이동 */ }
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
                    .clickable { /* 배경음 설정 화면으로 이동 */ }
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
                onClick = { /* 목소리 추천 기능 처리 */ },
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
                Text(
                    text = "동화 텍스트... 어쩌고 저쩌고",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Text(
                    text = "동화 텍스트...",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "동화 텍스트...",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "동화 텍스트...",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "동화 텍스트...",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
                    isSelected = true
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "동화 리스트",
                    tint = iconTint
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_bookmark,
                    text = "목소리 리스트",
                    tint = iconTint
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_music,
                    text = "음악 리스트",
                    tint = iconTint
                )
                BottomNavItem(
                    iconResId = R.drawable.ic_settings,
                    text = "설정",
                    tint = iconTint
                )
            }
        }
    }
}
//
//@Composable
//fun BottomNavItem(
//    iconResId: Int,
//    text: String,
//    tint: Color = Color(0xFFAA8866),
//    isSelected: Boolean = false
//) {
//    val textColor = if (isSelected) Color.Black else Color(0xFF666666)
//    val iconTint = if (isSelected) Color.Black else tint
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
//            textAlign = TextAlign.Center
//        )
//    }
//}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun GeneratedStoryScreenPreview() {
    ImageStoryTheme {
        GeneratedStoryScreen()
    }
}