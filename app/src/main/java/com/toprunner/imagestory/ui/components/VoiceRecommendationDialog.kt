package com.toprunner.imagestory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.VoiceEntity
import com.toprunner.imagestory.model.VoiceFeatures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.*
import kotlin.random.Random

@Composable
fun VoiceRecommendationDialog(
    isLoading: Boolean,
    storyFeatures: VoiceFeatures,
    recommendedVoice: VoiceEntity?,
    similarityPercentage: Int,
    onDismiss: () -> Unit,
    onUseRecommendedVoice: () -> Unit

) {
    // 다이얼로그 속성 설정 - 단순화
    val dialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
    )

    // 단순화된 진입 애니메이션 (성능 개선)
    val dialogScale = remember { Animatable(0.95f) }
    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = dialogProperties
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .width(320.dp)
                .height(if (isLoading) 420.dp else 560.dp)
                .graphicsLayer {
                    scaleX = dialogScale.value
                    scaleY = dialogScale.value
                }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            // 메인 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 애니메이션이 제거된 단순화된 헤더
                ImprovedHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(16.dp))

                // 단순한 구분선
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 메인 컨텐츠 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        FileAnalysisAnimation()
                    } else {
                        ResultContent(recommendedVoice, similarityPercentage)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 개선된 버튼 영역
                ImprovedActionButtons(
                    isLoading = isLoading,
                    recommendedVoice = recommendedVoice,
                    onDismiss = onDismiss,
                    onUseRecommendedVoice = onUseRecommendedVoice
                )
            }
        }
    }
}

// 개선된 헤더 컴포넌트 - 애니메이션 제거
@Composable
private fun ImprovedHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 애니메이션이 제거된 단순한 아이콘
        Icon(
            painter = painterResource(id = R.drawable.ic_volume_up),
            contentDescription = "음성 추천",
            tint = Color(0xFFE9B44C),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "음성 추천",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F2E20)
            )

            Text(
                text = "동화에 가장 적합한 음성을 찾아줍니다",
                fontSize = 12.sp,
                color = Color(0xFF6B5B4E)
            )
        }

        // 닫기 버튼
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "닫기",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// NEW: 파일 분석 애니메이션 컴포넌트
@Composable
private fun FileAnalysisAnimation() {
    val scope = rememberCoroutineScope()

    // 분석 단계 관련 상태값
    val phases = listOf("음성 파일 로드", "파형 분석", "피치 추출", "음색 분석", "최적 매칭")
    var currentPhase by remember { mutableStateOf(0) }

    // 페이징 애니메이션
    var currentPage by remember { mutableStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "fileTransition")

    // 파일 이동 애니메이션
    val horizontalOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fileMovement"
    )

    // 파일 회전 애니메이션
    val fileRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fileRotation"
    )

    // 파일 오프셋 계산 (페이지별)
    val fileOffset = remember(currentPage, horizontalOffset) {
        when {
            horizontalOffset < 100 -> horizontalOffset // 초기 이동
            horizontalOffset < 200 -> 100f // 정지 및 분석
            else -> horizontalOffset // 다음 위치로 이동
        }
    }

    // 스캔 라인 애니메이션
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    // 단계 자동 변경
    LaunchedEffect(Unit) {
        while (true) {
            delay(1800)
            currentPage = (currentPage + 1) % 3
            currentPhase = (currentPhase + 1) % phases.size
        }
    }

    // 파동 배경 애니메이션
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "wavePhase"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 분석 시각화 영역
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            // 파동 배경 효과
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // 배경 그리드 효과
                for (i in 0..10) {
                    val y = height * i / 10
                    drawLine(
                        color = Color(0xFFEAEAEA),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                for (i in 0..10) {
                    val x = width * i / 10
                    drawLine(
                        color = Color(0xFFEAEAEA),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                }

                // 파형 효과 (음성 파형 시각화)
                val path = Path()
                path.moveTo(0f, height / 2)

                for (x in 0..width.toInt() step 5) {
                    val xRatio = x.toFloat() / width
                    val yOffset = sin(xRatio * 10 + wavePhase) * height * 0.15f
                    val y = height / 2 + yOffset
                    path.lineTo(x.toFloat(), y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFFE9B44C).copy(alpha = 0.5f),
                    style = Stroke(width = 2f)
                )
            }

            // 파일 객체들 애니메이션
            Box(modifier = Modifier.fillMaxSize()) {
                // 첫 번째 파일 (음성 파일)
                FileIcon(
                    offset = Offset(fileOffset - 50f, 90f),
                    rotation = fileRotation,
                    backgroundColor = Color(0xFFE9D364),
                    iconRes = R.drawable.ic_mic,
                    opacity = if (currentPage == 0) 1f else 0.4f
                )

                // 두 번째 파일 (분석 중인 파일)
                FileIcon(
                    offset = Offset(90f, 90f),
                    rotation = 0f,
                    backgroundColor = Color(0xFF9ED8D8),
                    iconRes = R.drawable.ic_mic,
                    opacity = 1f
                )

                // 스캔 라인 효과
                if (currentPage == 1) {
                    Box(
                        modifier = Modifier
                            .offset(x = 90.dp, y = (40 + scanProgress * 100).dp)
                            .width(100.dp)
                            .height(2.dp)
                            .background(
                                color = Color(0xFFE9B44C).copy(alpha = 0.7f)
                            )
                    )
                }

                // 세 번째 파일 (처리된 파일)
                FileIcon(
                    offset = Offset(fileOffset + 130f, 90f),
                    rotation = -fileRotation,
                    backgroundColor = Color(0xFFE76F51),
                    iconRes = R.drawable.ic_mic,
                    opacity = if (currentPage == 2) 1f else 0.4f
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 현재 처리 단계 표시
        Text(
            text = phases[currentPhase],
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE9B44C)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 진행 표시줄
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFE9B44C),
            trackColor = Color(0xFFEEEEEE),
            progress = { (currentPhase + 1).toFloat() / phases.size }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 설명 텍스트
        Text(
            text = "동화에 가장 어울리는 음성을 찾고 있습니다",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// 파일 아이콘 컴포넌트
@Composable
private fun FileIcon(
    offset: Offset,
    rotation: Float,
    backgroundColor: Color,
    iconRes: Int,
    opacity: Float = 1f
) {
    Box(
        modifier = Modifier
            .offset(x = offset.x.dp, y = offset.y.dp)
            .size(50.dp)
            .graphicsLayer {
                this.rotationZ = rotation
                this.alpha = opacity
            }
    ) {
        // 파일 배경
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 파일 아이콘
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "File",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // 파일 끝 페이지 효과 (접힌 모서리)
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp))
                .background(Color.White.copy(alpha = 0.7f))
        )
    }
}

// 결과 컨텐츠 - 개선된 UI
@Composable
private fun ResultContent(
    recommendedVoice: VoiceEntity?,
    similarityPercentage: Int
) {
    if (recommendedVoice == null) {
        // 추천 음성이 없는 경우
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = "음성 없음",
                tint = Color.Gray,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "적합한 음성을 찾을 수 없습니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F2E20)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "새로운 음성을 녹음하거나 다른 음성을 선택해보세요",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // 추천 음성이 있는 경우
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 일치도 표시 - 단순화된 원형 프로그레스
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // 백그라운드 서클
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(130.dp),
                    color = Color(0xFFEEEEEE),
                    strokeWidth = 12.dp
                )

                // 일치도 서클
                CircularProgressIndicator(
                    progress = { similarityPercentage / 100f },
                    modifier = Modifier.size(130.dp),
                    color = Color(0xFFE9B44C),
                    strokeWidth = 12.dp
                )

                // 퍼센트 텍스트
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$similarityPercentage%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9B44C)
                    )

                    Text(
                        text = "일치도",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 추천 음성 정보 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9E7))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "추천 음성",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF6B5B4E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = recommendedVoice.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "정보",
                            tint = Color(0xFFE9B44C),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = getVoiceDescription(similarityPercentage),
                            fontSize = 12.sp,
                            color = Color(0xFF6B5B4E),
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 음성 태그 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceTag(text = "최적")
                        VoiceTag(text = getVoicePitchTag(recommendedVoice))
                        VoiceTag(text = "낭독용")
                    }
                }
            }
        }
    }
}

// 음성 태그 컴포넌트
@Composable
private fun VoiceTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFEED0))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE9B44C)
        )
    }
}

// 개선된 버튼 영역
@Composable
private fun ImprovedActionButtons(
    isLoading: Boolean,
    recommendedVoice: VoiceEntity?,
    onDismiss: () -> Unit,
    onUseRecommendedVoice: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 취소 버튼
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "취소",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        // 사용 버튼
        Button(
            onClick = onUseRecommendedVoice,  // 여기서는 전달받은 콜백 함수만 사용
            modifier = Modifier
                .weight(2f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE9D364),
                contentColor = Color.Black,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && recommendedVoice != null
        ) {
            Text(
                text = "추천 음성 동화 생성",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 음성 특성에 따른 설명 생성 함수
private fun getVoiceDescription(similarity: Int): String {
    return when {
        similarity > 90 -> "이 음성은 동화의 내용과 분위기에 완벽하게 어울리는 최적의 목소리입니다. 부드러운 발음과 안정적인 톤으로 아이들이 듣기에 매우 적합합니다."
        similarity > 70 -> "이 음성은 동화의 내용과 분위기에 매우 잘 어울리는 목소리입니다. 자연스러운 억양과 표현력이 이야기의 감정을 잘 전달합니다."
        else -> "이 음성은 동화의 내용과 분위기에 어울리는 목소리입니다. 안정적인 음색으로 듣기 편안한 낭독이 가능합니다."
    }
}

// 음성 피치 태그 생성 함수
private fun getVoicePitchTag(voice: VoiceEntity): String {
    try {
        val attributeJson = JSONObject(voice.attribute)
        val averagePitch = attributeJson.optDouble("averagePitch", 0.0)

        return when {
            averagePitch > 180 -> "높은 음역"
            averagePitch > 140 -> "중간 음역"
            averagePitch > 0 -> "낮은 음역"
            else -> "부드러운 음색"
        }
    } catch (e: Exception) {
        return "부드러운 음색"
    }
}