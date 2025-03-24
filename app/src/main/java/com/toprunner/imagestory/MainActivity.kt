package com.toprunner.imagestory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.ui.theme.ImageStoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageStoryTheme {
                StoryApp()
            }
        }
    }
}

@Composable
fun StoryApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바 (헤더) - 흠 문자와 Close 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "홈",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.5.dp,
            color = Color(0xFFE0E0E0)
        )

        // 메인 이미지 (타워/교회 건물)
        Image(
            painter = painterResource(id = R.drawable.example_image),
            contentDescription = "Tower Building",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentScale = ContentScale.Crop
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.5.dp,
            color = Color(0xFFE0E0E0)
        )

        // 한글 네비게이션 탭 (홈, 바다, 사파리 등)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavTab("홈", isSelected = true)
            NavTab("바다", isSelected = false)
            NavTab("사파리", isSelected = false)
            NavTab("우주", isSelected = false)
            NavTab("사막", isSelected = false)
            NavTab("정글", isSelected = false)
        }

        // 구분선
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.5.dp,
            color = Color(0xFFE0E0E0)
        )

        // 노란색 액션 버튼들
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly, // 변경됨: 세로 방향 중앙 정렬
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 사진 찍기 버튼
            Button(
                onClick = { /* 첫 번째 액션 처리 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE566) // 밝은 노란색
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Text(
                    text = "사진 찍기",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight(900),
                        color = Color(0xFF1C1C0D),

                        textAlign = TextAlign.Center,
                    )
                )
            }

            // 동화 생성하기 버튼
            Button(
                onClick = { /* 두 번째 액션 처리 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE566) // 밝은 노란색
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Text(
                    text = "동화 생성하기",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight(900),
                        color = Color(0xFF1C1C0D),

                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.5.dp,
            color = Color(0xFFE0E0E0)
        )

        // 하단 네비게이션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘들의 색상은 갈색/베이지 색상으로 설정
                val iconTint = Color(0xFFAA8866)

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
                    text = "배경음 리스트",
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

@Composable
fun NavTab(text: String, isSelected: Boolean = false) {
    val textColor = if (isSelected) Color.Black else Color(0xFFAA8866) // 선택된 탭은 검정, 아닌 것은 갈색/베이지
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = fontWeight,
            color = textColor
        )

        // 선택된 탭 아래에 강조선 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconResId: Int,
    text: String,
    tint: Color = Color(0xFFAA8866),
    isSelected: Boolean = false
) {
    val textColor = if (isSelected) Color.Black else Color(0xFF666666)
    val iconTint = if (isSelected) Color.Black else tint
    val fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            fontWeight = fontWeight // 추가됨: 텍스트를 굵게 표시


        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun StoryAppPreview() {
    ImageStoryTheme {
        StoryApp()
    }
}