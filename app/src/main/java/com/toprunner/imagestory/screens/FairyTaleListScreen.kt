package com.toprunner.imagestory.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.viewmodel.FairyTaleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairyTaleListScreen(
    navController: NavController,
    viewModel: FairyTaleViewModel? = null
) {
    val backgroundColor = Color(0xFFFFFBF0) // 밝은 크림색 배경
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 뷰모델 생성 또는 재사용
    val fairyTaleViewModel = viewModel ?: remember {
        FairyTaleViewModel(FairyTaleRepository(context))
    }

    val fairyTales by fairyTaleViewModel.fairyTales.collectAsState()
    val isLoading by fairyTaleViewModel.isLoading.collectAsState()
    val isCreatingNewStory by fairyTaleViewModel.isCreatingNewStory.collectAsState()

    // 데이터 로드
    LaunchedEffect(Unit) {
        fairyTaleViewModel.loadFairyTales()
    }

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fairyTaleToDelete by remember { mutableStateOf<Long?>(null) }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && fairyTaleToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                fairyTaleToDelete = null
            },
            title = { Text("동화 삭제") },
            text = { Text("이 동화를 삭제하시겠습니까? 삭제한 동화는 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 삭제 실행
                        fairyTaleToDelete?.let { id ->
                            scope.launch {
                                fairyTaleViewModel.deleteFairyTale(id)
                            }
                        }
                        showDeleteDialog = false
                        fairyTaleToDelete = null
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        fairyTaleToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
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
                        text = "동화 리스트",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
                    )
                }
            }
        }


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
                    items(fairyTales, key = { it.fairy_tales_id }) { fairyTale ->
                        FairyTaleItemCard(
                            fairyTale = fairyTale,
                            onClick = {
                                navController.navigate(NavRoute.GeneratedStory.createRoute(fairyTale.fairy_tales_id))
                            },
                            onDelete = {
                                // 삭제 확인 다이얼로그 표시
                                fairyTaleToDelete = fairyTale.fairy_tales_id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // 새 동화 생성 중 오버레이
            if (isCreatingNewStory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "추천된 음성으로 동화를 생성하는 중입니다...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }

    }
}