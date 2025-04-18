package com.toprunner.imagestory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.toprunner.imagestory.R
import com.toprunner.imagestory.navigation.NavRoute

@Composable
fun BottomNavBar(navController: NavController) {
    // 네비게이션 아이템 정의
    val items = listOf(
        Triple(NavRoute.Home.route, R.drawable.ic_home, "홈화면"),
        Triple(NavRoute.FairyTaleList.route, R.drawable.ic_bookmark, "동화 리스트"),
        Triple(NavRoute.VoiceList.route, R.drawable.ic_mic, "음성 리스트"),
        Triple(NavRoute.MusicList.route, R.drawable.ic_music, "음악 리스트"),
        Triple(NavRoute.Settings.route, R.drawable.ic_settings, "설정")
    )

    // 현재 선택된 화면 확인
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
            //.height(60.dp),
            //.navigationBarsPadding() // 시스템 내비게이션 영역에 대한 패딩 추가
            //.background(Color.White),
        containerColor = Color.White
    ) {
        items.forEach { (route, iconResId, label) ->
            val selected = currentDestination?.hierarchy?.any { it.route == route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentDestination?.route != route) {
                        navController.navigate(route) {
                            // 시작 목적지까지 스택 정리
                            popUpTo(NavRoute.Home.route) {
                                saveState = true
                            }
                            // 동일 목적지 복제 방지
                            launchSingleTop = true
                            // 상태 복원
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = label,
                        tint = if (selected) Color.Black else Color(0xFFAA8866)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (selected) Color.Black else Color(0xFF666666),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    // 선택(하이라이트)된 아이템의 인디케이터 색상을 원하는 색으로 지정
                    indicatorColor = Color(0xFFFFC106),
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color(0xFFAA8866),
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color(0xFF666666)
                )
            )
        }
    }
}