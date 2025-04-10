package com.toprunner.imagestory.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import com.toprunner.imagestory.navigation.NavRoute
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FairyTaleListScreen(navController: NavController) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    var isLoading by remember { mutableStateOf(false) }
    var fairyTales by remember { mutableStateOf<List<FairyTaleEntity>>(emptyList()) }

    // 샘플 데이터 로드
    LaunchedEffect(Unit) {
        isLoading = true
        // 실제 앱에서는 repository에서 데이터를 가져오는 로직으로 대체
        fairyTales = listOf(
            FairyTaleEntity(
                fairy_tales_id = 1,
                title = "마법의 숲속 모험",
                voice_id = 1,
                image_id = 1,
                text_id = 1,
                music_id = 1,
                attribute = "{\"theme\":\"fantasy\", \"audioPath\":\"sample\"}",
                created_at = System.currentTimeMillis()
            ),
            FairyTaleEntity(
                fairy_tales_id = 2,
                title = "별빛 너머의 우주여행",
                voice_id = 2,
                image_id = 2,
                text_id = 2,
                music_id = 2,
                attribute = "{\"theme\":\"sf\", \"audioPath\":\"sample\"}",
                created_at = System.currentTimeMillis() - 86400000 // 1 day ago
            )
        )
        isLoading = false
    }

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
                        FairyTaleItemCard(
                            fairyTale = fairyTale,
                            onClick = {
                                // 동화 상세 화면으로 이동
                                navController.navigate(NavRoute.GeneratedStory.createRoute(fairyTale.fairy_tales_id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FairyTaleItemCard(fairyTale: FairyTaleEntity, onClick: () -> Unit) {
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
            // 아이콘
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

            // 화살표 아이콘
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Open",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}