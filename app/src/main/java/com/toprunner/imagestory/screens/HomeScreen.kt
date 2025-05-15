package com.toprunner.imagestory.screens

import android.graphics.Bitmap
import android.graphics.fonts.FontStyle
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.toprunner.imagestory.R
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.Font


@Composable
fun HomeScreen(
    capturedImageBitmap: Bitmap?,
    selectedTheme: String?,
    isLoading: Boolean = false,
    onTakePhotoClicked: () -> Unit,
    onPickImageClicked: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onGenerateStoryClicked: () -> Unit,

    errorMessage: String?,           // ← 에러 메시지
    onErrorDismiss: () -> Unit,      // ← 다이얼로그 닫기 콜백
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val themeOptions = remember { listOf("판타지", "사랑", "SF", "공포", "코미디","비극") }
    val themeButtonText = selectedTheme ?: "테마"

    // 테마 다이얼로그에서 현재 선택된 테마를 기억하기 위한 변수
    var tempSelectedTheme by remember { mutableStateOf(selectedTheme) }

    // 배경 그라디언트 색상 정의
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFBF0),  // 상단 색상
            Color(0xFFFFF8E5)   // 하단 색상
        )
    )

    // 이미지 선택 여부에 따른 애니메이션 효과
    val imageScale by animateFloatAsState(
        targetValue = if (capturedImageBitmap != null) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "imageScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // 상단 바 (헤더)
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
                            text = "홈",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F2E20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 메인 이미지 컨테이너
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // 내부 그림자 효과를 위한 컨테이너
                NeuomorphicBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .graphicsLayer {
                            // 스케일 애니메이션 적용
                            scaleX = imageScale
                            scaleY = imageScale
                        },
                    backgroundColor = Color(0xFFF5F5F5),
                    elevation = 8.dp,
                    cornerRadius = 24.dp
                ) {
                    if (capturedImageBitmap != null) {
                        // 이미지가 있는 경우
                        Image(
                            bitmap = capturedImageBitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // 이미지 위에 반사 효과
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.15f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = 100f
                                    )
                                )
                        )
                    } else {
                        // 이미지가 없는 경우 플레이스홀더
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFF5F5F5),
                                            Color(0xFFEEEEEE)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, 350f)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_bookmark),
                                    contentDescription = "No Image",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .alpha(0.5f),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "사진을 찍거나 선택해주세요",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 첫 번째 행: 사진 찍기, 갤러리에서 불러오기, 테마 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 사진 찍기 버튼
                    NeuomorphicButton(
                        onClick = { onTakePhotoClicked() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        backgroundColor = Color(0xFFFEE566),
                        elevation = 6.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_home),
                                contentDescription = "사진 찍기",
                                tint = Color(0xFF3F2E20),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "카메라",
                                color = Color(0xFF3F2E20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 갤러리에서 불러오기 버튼
                    NeuomorphicButton(
                        onClick = { onPickImageClicked() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        backgroundColor = Color(0xFFFEE566),
                        elevation = 6.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bookmark),
                                contentDescription = "갤러리",
                                tint = Color(0xFF3F2E20),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "갤러리",
                                color = Color(0xFF3F2E20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 테마 버튼
                    NeuomorphicButton(
                        onClick = {
                            tempSelectedTheme = selectedTheme
                            showThemeDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        backgroundColor = Color(0xFFFEE566),
                        elevation = 6.dp,
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "테마",
                                tint = Color(0xFF3F2E20),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = themeButtonText,
                                color = Color(0xFF3F2E20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 동화 생성하기 버튼
                NeuomorphicButton(
                    onClick = { onGenerateStoryClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    backgroundColor = Color(0xFFFFD166),
                    elevation = 8.dp,
                    enabled = !isLoading && capturedImageBitmap != null && selectedTheme != null
                ) {
                    Text(
                        text = "동화 생성하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (!isLoading && capturedImageBitmap != null && selectedTheme != null)
                            Color(0xFF3F2E20) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // 동화 생성 로딩 다이얼로그
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                NeuomorphicBox(
                    modifier = Modifier.width(280.dp),
                    backgroundColor = Color.White.copy(alpha = 0.95f),
                    elevation = 16.dp,
                    cornerRadius = 20.dp
                ) {
                    LoadingAnimationDialog()
                }
            }
        }
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = onErrorDismiss,
                title = { Text("오류") },
                text = {
                    // !! 를 붙여서 null 이든 아니든 화면에 뿌려보게 강제
                    Text(errorMessage!!)
                },
                confirmButton = {
                    TextButton(onClick = onErrorDismiss) {
                        Text("확인")
                    }
                }
            )
        }
        // 테마 선택 다이얼로그
        if (showThemeDialog) {
            Dialog(onDismissRequest = { showThemeDialog = false }) {
                val dialogWidth = 320.dp

                Box(
                    modifier = Modifier
                        .width(dialogWidth)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // 배경 효과 - 미묘한 그라디언트 배경
                    Canvas (
                        modifier = Modifier.matchParentSize()
                    ) {
                        // Canvas scope 내부에서 사용 가능한 함수들
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color.White.copy(alpha = 0.9f)
                                ),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width * 1.5f
                            ),
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                        )

                        // 미세한 테두리 효과
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.5f)
                                )
                            ),
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // 실제 내용
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        // 헤더 영역
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 테마 아이콘
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFFD166),
                                                Color(0xFFFEE566)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_settings),
                                    contentDescription = "테마",
                                    tint = Color(0xFF3F2E20),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 제목
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "테마 선택",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F2E20)
                                )

                                Text(
                                    text = "동화에 적용할 테마를 선택해주세요",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // 닫기 버튼
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { showThemeDialog = false }
                                    .background(
                                        Color.LightGray.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "닫기",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 구분선 - 미묘한 그라디언트 효과
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.LightGray.copy(alpha = 0.2f),
                                            Color.LightGray.copy(alpha = 0.6f),
                                            Color.LightGray.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 테마 옵션 리스트 - 향상된 UI
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(themeOptions) { theme ->
                                EnhancedThemeSelectionItem(
                                    theme = theme,
                                    isSelected = theme == tempSelectedTheme,
                                    onThemeSelected = {
                                        tempSelectedTheme = theme
                                    }
                                )
                                // 마지막 아이템이 아니면 구분선 추가
                                if (theme != themeOptions.last()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(0.5.dp)
                                            .background(
                                                color = Color.LightGray.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 버튼 영역 - 취소 및 확인 버튼 (적절한 크기로 재조정)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 취소 버튼 - 텍스트 버튼으로 변경
                            TextButton(
                                onClick = {
                                    showThemeDialog = false
                                    tempSelectedTheme = selectedTheme // 원래 선택으로 복원
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Text(
                                    text = "취소",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 확인 버튼 - 프라이머리 버튼으로 유지
                            Button(
                                onClick = {
                                    tempSelectedTheme?.let {
                                        onThemeSelected(it)
                                    }
                                    showThemeDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD166),
                                    contentColor = Color(0xFF3F2E20)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Text(
                                    text = "확인",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimationDialog() {
    // 배경 카드 스케일 애니메이션
    val cardScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 무한 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "loading rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    // 책 페이지 넘김 애니메이션
    val pageAnimation = rememberInfiniteTransition(label = "page flip")
    val pageAlpha by pageAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                0.3f at 300
                0.7f at 600
                1f at 900
                1f at 2000
                0f at 2100
                0f at 3000
            }
        ),
        label = "page_alpha"
    )

    // 밝기 파동 애니메이션
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // 진행 텍스트 애니메이션
    val progressTextAnimation = rememberInfiniteTransition(label = "progress text")
    val dotsProgress by progressTextAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "dots"
    )

    val progressText = remember(dotsProgress) {
        "동화 생성 중" + ".".repeat(dotsProgress.toInt() + 1)
    }

    NeuomorphicBox(
        modifier = Modifier
            .width(280.dp)
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            },
        backgroundColor = Color.White.copy(alpha = 0.96f),
        elevation = 24.dp,
        cornerRadius = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 회전하는 책 아이콘과 발광 효과
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // 발광 효과
                Canvas(
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(glowAlpha)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD166).copy(alpha = 0.7f),
                                Color(0xFFFFD166).copy(alpha = 0f)
                            )
                        ),
                        radius = size.width / 2,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }

                // 회전하는 책/동화 아이콘
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            rotationY = rotation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 책 표지
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD166),
                                        Color(0xFFE9B44C)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    // 책 페이지 넘기는 효과
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer {
                                alpha = pageAlpha
                                rotationY = 180f * pageAlpha
                            }
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                    )

                    // 책 제목 위치 (작은 텍스트 블록 표현)
                    Box(
                        modifier = Modifier
                            .size(36.dp, 6.dp)
                            .offset(y = (-15).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                    )

                    // 책 페이지 텍스트 묘사 (작은 선들로 표현)
                    Column(
                        modifier = Modifier
                            .width(32.dp)
                            .height(24.dp)
                            .offset(y = 8.dp)
                            .alpha(if (rotation % 360 < 180) 1f else 0f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 진행 상태 텍스트
            Text(
                text = progressText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F2E20),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 진행 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                // 무한 진행 애니메이션
                val progressAnimation = rememberInfiniteTransition(label = "progress")
                val progressWidth by progressAnimation.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "progress_width"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFD166),
                                    Color(0xFFE9B44C),
                                    Color(0xFFFFD166)
                                )
                            )
                        )
                )

                // 반짝이는 하이라이트 효과
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .offset(x = progressWidth.times(300).dp - 20.dp)
                        .alpha(0.6f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 설명 텍스트
            Text(
                text = "AI가 창의적인 동화를 만들고 있어요",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 창작 과정 작은 텍스트
            val steps = listOf("이미지 분석", "스토리 구성", "캐릭터 만들기", "이야기 완성")
            val currentStep = remember(dotsProgress) {
                steps[((dotsProgress * 5) % steps.size).toInt()]
            }

            Text(
                text = currentStep,
                color = Color(0xFFE9B44C),
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedThemeSelectionItem(
    theme: String,
    isSelected: Boolean,
    onThemeSelected: () -> Unit
) {
    val themeData = getThemeData(theme)
    val backgroundColor = when {
        isSelected -> themeData.color.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "scaleAnim",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onThemeSelected() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 테마 색상 표시 - 고급스러운 아이콘 표시
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            themeData.color,
                            themeData.color.copy(alpha = 0.8f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(46f, 46f)
                    )
                )
                .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = themeData.iconRes),
                contentDescription = theme,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            // 선택 효과
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 테마 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) themeData.color.copy(alpha = 0.9f) else Color(0xFF3F2E20)
            )

            Text(
                text = themeData.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 선택 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(themeData.color.copy(alpha = 0.2f))
                    .border(
                        width = 1.5.dp,
                        color = themeData.color,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bookmark), // 체크 아이콘
                    contentDescription = "Selected",
                    tint = themeData.color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// 테마별 상세 데이터 클래스
data class ThemeInfo(
    val color: Color,
    val iconRes: Int,
    val description: String
)

// 테마 정보 얻기
fun getThemeData(theme: String): ThemeInfo {
    return when (theme) {
        "판타지" -> ThemeInfo(
            color = Color(0xFF8C6BAF),
            iconRes = R.drawable.ic_bookmark, // 적절한 아이콘으로 교체 필요
            description = "마법과 상상의 세계로 가득한 동화"
        )
        "사랑" -> ThemeInfo(
            color = Color(0xFFEC407A),
            iconRes = R.drawable.ic_bookmark, // 적절한 아이콘으로 교체 필요
            description = "따뜻하고 감동적인 이야기"
        )
        "SF" -> ThemeInfo(
            color = Color(0xFF29B6F6),
            iconRes = R.drawable.ic_bookmark, // 적절한 아이콘으로 교체 필요
            description = "미래 세계와 과학 기술이 담긴 동화"
        )
        "공포" -> ThemeInfo(
            color = Color(0xFF78909C),
            iconRes = R.drawable.ic_bookmark, // 적절한 아이콘으로 교체 필요
            description = "스릴과 긴장감 있는 무서운 이야기"
        )
        "코미디" -> ThemeInfo(
            color = Color(0xFFFFB74D),
            iconRes = R.drawable.ic_bookmark, // 적절한 아이콘으로 교체 필요
            description = "유머러스하고 재미있는 동화"
        )
        "비극" -> ThemeInfo(
            color = Color(0xFF7986CB),
            iconRes = R.drawable.ic_bookmark,
            description = "슬프지만 깊은 교훈이 담긴 이야기"
        )
        else -> ThemeInfo(
            color = Color(0xFF9E9E9E),
            iconRes = R.drawable.ic_bookmark,
            description = "일반적인 동화 테마"
        )
    }
}

@Composable
fun NeuomorphicBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    elevation: Dp = 6.dp,
    cornerRadius: Dp = 16.dp,
    hasBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shadowElevation = elevation.toPx()
                this.shape = RoundedCornerShape(cornerRadius)
                this.clip = true
            }
            .then(
                if (hasBorder) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // 상단 반사 효과 추가
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cornerRadius)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 내용
        content()
    }
}


@Composable
fun NeuomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFEE566),
    contentColor: Color = Color(0xFF3F2E20),
    elevation: Dp = 6.dp,
    enabled: Boolean = true,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val buttonAlpha = if (enabled) 1f else 0.6f
    val interactionSource = remember { MutableInteractionSource() }

    // 버튼이 눌렸는지 상태 확인
    val isPressed by interactionSource.collectIsPressedAsState()

    // 눌렸을 때 크기 변화
    val scale = if (isPressed) 0.97f else 1f

    // 버튼 눌림 애니메이션 - 부드러운 스프링 애니메이션 적용
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    // 눌렸을 때 그림자 변화 (더 작은 그림자)
    val buttonElevation = if (isPressed) elevation / 2 else elevation

    NeuomorphicBox(
        modifier = modifier
            .alpha(buttonAlpha)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 리플 효과 제거
                enabled = enabled,
                onClick = onClick
            ),
        backgroundColor = backgroundColor,
        elevation = if (enabled) buttonElevation else 0.dp,
        cornerRadius = cornerRadius
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

// 물결 효과를 가진 그라데이션 배경을 위한 컴포넌트
@Composable
fun WaveBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color = primaryColor.copy(alpha = 0.5f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave transition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave phase"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val waveHeight = canvasHeight * 0.05f
        val waveMidpoint = canvasHeight * 0.8f

        // 배경 그라디언트
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.05f),
                    primaryColor.copy(alpha = 0.02f)
                )
            )
        )

        // 부드러운 웨이브 효과
        val path = Path()
        path.moveTo(0f, canvasHeight)

        for (x in 0..canvasWidth.toInt() step 5) {
            val xRatio = x.toFloat() / canvasWidth
            val wavePhaseShift = (wavePhase + xRatio * 4f * PI.toFloat()) % (2f * PI.toFloat())
            val y = waveMidpoint + kotlin.math.sin(wavePhaseShift) * waveHeight
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(canvasWidth, canvasHeight)
        path.lineTo(0f, canvasHeight)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.2f),
                    secondaryColor.copy(alpha = 0f)
                ),
                startY = waveMidpoint - waveHeight,
                endY = canvasHeight
            )
        )
    }
}

// 테마 선택 컨펌 버튼과 하단의 액션 영역
@Composable
fun ThemeSelectionActions(
    selectedTheme: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 취소 버튼
            TextButton(
                onClick = onCancel,
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text(
                    text = "취소",
                    fontSize = 15.sp
                )
            }

            // 확인 버튼
            val confirmButtonColor = selectedTheme?.let { getThemeData(it).color } ?: Color(0xFFFFD166)

            Button(
                onClick = onConfirm,
                enabled = selectedTheme != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmButtonColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 0.dp
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "확인",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// 모든 상수와 파이(PI) 값 정의
private const val PI = Math.PI