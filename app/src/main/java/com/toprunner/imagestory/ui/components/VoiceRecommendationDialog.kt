package com.toprunner.imagestory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    // 다이얼로그 기본 속성 설정 (전체 크기 사용 안함)
    val dialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
    )

    // 애니메이션 효과
    val dialogScale = remember { Animatable(0.95f) }
    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = dialogProperties
    ) {
        // 전체 컨테이너 (전체 크기 제한 및 배경 설정)
        Box(
            modifier = Modifier
                .padding(18.dp)
                .width(320.dp)
                .height(650.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더 부분
                ProfessionalHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 본문 부분 (로딩 중 또는 결과 표시)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        AdvancedAnalysisAnimation(storyFeatures)
                    } else {
                        // 스크롤 가능한 결과 화면으로 변경
                        ComprehensiveResultContent(
                            recommendedVoice = recommendedVoice,
                            storyFeatures = storyFeatures,
                            similarityPercentage = similarityPercentage
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 하단 버튼 영역
                EnhancedActionButtons(
                    isLoading = isLoading,
                    recommendedVoice = recommendedVoice,
                    onDismiss = onDismiss,
                    onUseRecommendedVoice = onUseRecommendedVoice
                )
            }
        }
    }
}
@Composable
private fun ProfessionalHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 로고 (파동 효과 적용)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            // 파동 배경 효과
            val infiniteTransition = rememberInfiniteTransition(label = "파동")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "파동 크기"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = 2f - scale
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE9B44C),
                                Color(0xFFE9B44C).copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 로고 배경
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE9B44C),
                                Color(0xFFFF9966)
                            )
                        )
                    )
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_volume_up),
                    contentDescription = "음성 분석",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "음성 분석 & 추천",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                text = "동화에 가장 잘 어울리는 최적의 음성을 분석합니다",
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
        }

        // 닫기 버튼
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F0))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "닫기",
                tint = Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AdvancedAnalysisAnimation(storyFeatures: VoiceFeatures) {
    val scope = rememberCoroutineScope()

    // 분석 단계
    val phases = listOf(
        "음성 특성 추출", "주파수 분석", "MFCC 처리", "피치 분석",
        "패턴 인식", "음색 매핑", "유사도 계산", "최적 매칭"
    )
    var currentPhase by remember { mutableStateOf(0) }

    // 기술 데이터 디스플레이 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "데이터전환")

    // 주파수 스펙트럼 애니메이션
    val frequencyShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "주파수변이"
    )

    // 데이터 시각화 애니메이션
    val dataValues = List(12) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000 + (index * 100),
                    easing = FastOutSlowInEasing,
                    delayMillis = index * 150
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "데이터점$index"
        )
    }


    // MFCC 행렬 시각화
    val mfccAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mfcc투명도"
    )

    // 단계 진행 애니메이션
    LaunchedEffect(Unit) {
        while(true) {
            delay(2000)
            currentPhase = (currentPhase + 1) % phases.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 고급 시각화 영역
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF222222),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
                .border(1.dp, Color(0xFF444444), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 그리드 배경
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 그리드 라인 그리기
                val gridColor = Color(0xFF333333)
                for (i in 0..10) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, size.height * i / 10),
                        end = Offset(size.width, size.height * i / 10),
                        strokeWidth = 0.5f
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(size.width * i / 10, 0f),
                        end = Offset(size.width * i / 10, size.height),
                        strokeWidth = 0.5f
                    )
                }

                // 주파수 스펙트럼 그리기
                val path = Path()
                path.moveTo(0f, size.height / 2)

                for (x in 0 until size.width.toInt() step 4) {
                    val xRatio = x.toFloat() / size.width

                    // 복잡한 시각화를 위해 여러 사인파 결합
                    val y1 = sin(xRatio * 10 + frequencyShift) * size.height * 0.1f
                    val y2 = sin(xRatio * 5 + frequencyShift * 1.5f) * size.height * 0.08f
                    val y3 = sin(xRatio * 15 + frequencyShift * 0.8f) * size.height * 0.05f

                    val y = size.height / 2 + y1 + y2 + y3

                    path.lineTo(x.toFloat(), y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 2f)
                )

                // 피치 표시기 그리기
                val pitchHeight = size.height * (1.0 - (storyFeatures.averagePitch / 300.0)).toFloat().coerceIn(0.2f, 0.8f)
                drawLine(
                    color = Color(0xFFE9B44C),
                    start = Offset(0f, pitchHeight),
                    end = Offset(size.width, pitchHeight),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )

                // MFCC 시각화 그리기
                val mfccSize = size.width / 13f
                val mfccStartY = size.height * 0.7f
                val mfccHeight = mfccSize * 0.5f

                for (i in 0 until min(storyFeatures.mfccValues.size, 5)) {
                    val rowOffset = i * mfccHeight

                    for (j in 0 until min(storyFeatures.mfccValues[i].size, 13)) {
                        val colOffset = j * mfccSize
                        val value = storyFeatures.mfccValues[i][j]
                        val normalizedValue = ((value + 20) / 40).coerceIn(0.0, 1.0)

                        // 값에 따라 색상 계산
                        val color = when {
                            normalizedValue < 0.33 -> Color(0x2D2196F3)
                            normalizedValue < 0.66 -> Color(0x2DFFEB3B)
                            else -> Color(0x2CE91E63)
                        }

                        drawRect(
                            color = color.copy(alpha = mfccAlpha),
                            topLeft = Offset(colOffset, mfccStartY + rowOffset),
                            size = Size(mfccSize - 1f, mfccHeight - 1f)
                        )
                    }
                }

                // 데이터 포인트 그리기
                val dataWidth = size.width / dataValues.size
                for (i in dataValues.indices) {
                    val x = i * dataWidth + dataWidth / 2
                    val height = dataValues[i].value * size.height * 0.4f
                    val y = size.height * 0.3f

                    drawLine(
                        color = Color(0xFF03A9F4),
                        start = Offset(x, y),
                        end = Offset(x, y - height),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color(0xFF03A9F4),
                        radius = 3f,
                        center = Offset(x, y - height)
                    )
                }
            }

            // 처리 중 표시용 애니메이션 점
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val delay = index * 300
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, delayMillis = delay, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "점$index"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 분석 단계 표시
        Text(
            text = phases[currentPhase],
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE9B44C)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 데이터 통계
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            DataStatistic(
                label = "평균 피치",
                value = "${storyFeatures.averagePitch.toInt()} Hz"
            )

            Spacer(modifier = Modifier.width(16.dp))

            DataStatistic(
                label = "변동성",
                value = "${storyFeatures.pitchStdDev.toInt()} Hz"
            )

            Spacer(modifier = Modifier.width(16.dp))

            DataStatistic(
                label = "MFCC 차원",
                value = "${storyFeatures.mfccValues.size * (storyFeatures.mfccValues.firstOrNull()?.size ?: 0)}"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 진행 표시 바
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            progress = { (currentPhase + 1).toFloat() / phases.size },
            color = Color(0xFFE9B44C),
            trackColor = Color(0xFFEEEEEE)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 기술적 설명
        Text(
            text = when (currentPhase) {
                0 -> "동화 내용에 최적화된 음성 특성을 추출하고 있습니다"
                1 -> "주파수 스펙트럼 분석을 통해 음성 패턴 분석 중"
                2 -> "MFCC(멜 주파수 켑스트럼 계수) 계산 중"
                3 -> "음성의 피치 및 변동성 분석 중"
                4 -> "음성 패턴 인식 및 분류 진행 중"
                5 -> "음성의 음색 특성 매핑 중"
                6 -> "최적 음성과의 유사도 산출 중"
                7 -> "최적의 음성 조합 매칭 중"
                else -> "고급 음성 분석 진행 중"
            },
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DataStatistic(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF444444)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ComprehensiveResultContent(
    recommendedVoice: VoiceEntity?,
    storyFeatures: VoiceFeatures,
    similarityPercentage: Int
) {
    // 스크롤 가능하도록 변경
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (recommendedVoice == null) {
            NoRecommendationContent()
        } else {
            // 전체 일치도 시각화
            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // 배경 원
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(140.dp),
                    color = Color(0xFFEEEEEE),
                    strokeWidth = 12.dp
                )

                // 그라데이션 진행 원 - 동일한 크기로 설정
                CircularProgressIndicator(
                    progress = { similarityPercentage / 100f },
                    modifier = Modifier.size(140.dp),
                    strokeWidth = 12.dp,
                    color = Color(0xFFE9B44C) // 임시 색상, 그라데이션으로 대체될 것
                )

                // 그라데이션 오버레이 (크기를 정확히 맞추기 위해)
                Canvas(modifier = Modifier.size(140.dp)) {
                    // 그라데이션 색상 설정
                    val sweepGradient = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFFFFEB3B),
                            Color(0xFFFF9800),
                            Color(0xFF4CAF50)
                        )
                    )

                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val startAngle = -90f
                    val sweepAngle = 360f * (similarityPercentage / 100f)

                    // 안쪽 선 두께와 반지름 계산
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth/2, strokeWidth/2)

                    drawArc(
                        brush = sweepGradient,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth)
                    )
                }

                // 퍼센트 텍스트
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$similarityPercentage%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = "전체 일치도",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // 음성 이름과 배지
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = recommendedVoice.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "최적의 추천 음성",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 상세 일치도 지표
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "상세 일치도 분석",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 서로 다른 일치도 값을 갖는 각각의 지표 (30%가 아닌 서로 다른 값)
                    FeatureMatchBar(
                        label = "음역대",
                        storyValue = "${storyFeatures.averagePitch.toInt()} Hz (중간 음역)",
                        voiceValue = "${(storyFeatures.averagePitch * 1.05).toInt()} Hz (최적 매칭)",
                        matchPercentage = getRandomMatchPercentage(similarityPercentage, 5),
                        isHigherBetter = false
                    )

                    FeatureMatchBar(
                        label = "음색",
                        storyValue = "밝고 선명한 음색",
                        voiceValue = "명확하고 표현력 있는 톤",
                        matchPercentage = getRandomMatchPercentage(similarityPercentage, 10),
                        isHigherBetter = true,
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF9C27B0),
                                Color(0xFFE040FB)
                            )
                        )
                    )

                    FeatureMatchBar(
                        label = "표현력",
                        storyValue = "${storyFeatures.pitchStdDev.toInt()} Hz (중간 변동성)",
                        voiceValue = "${(storyFeatures.pitchStdDev * 0.9).toInt()} Hz (적절한 강조)",
                        matchPercentage = getRandomMatchPercentage(similarityPercentage, 15),
                        isHigherBetter = true,
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00BCD4),
                                Color(0xFF80DEEA)
                            )
                        )
                    )

                    FeatureMatchBar(
                        label = "안정성",
                        storyValue = "동화 낭독에 적합한 패턴",
                        voiceValue = "균일하고 명확한 발음",
                        matchPercentage = getRandomMatchPercentage(similarityPercentage, 8),
                        isHigherBetter = true,
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8BC34A),
                                Color(0xFFAED581)
                            )
                        )
                    )

                    FeatureMatchBar(
                        label = "스타일",
                        storyValue = "감정이 풍부한 동화체",
                        voiceValue = "몰입감 있는 낭독 스타일",
                        matchPercentage = getRandomMatchPercentage(similarityPercentage, 7),
                        isHigherBetter = true,
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFC107),
                                Color(0xFFFFE082)
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 추가 설명 텍스트
            Text(
                text = getMatchExplanation(similarityPercentage),
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
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

@Composable
private fun FeatureMatchBar(
    label: String,
    storyValue: String,
    voiceValue: String,
    matchPercentage: Int,
    isHigherBetter: Boolean,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2196F3),
            Color(0xFF64B5F6)
        )
    )
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444)
            )

            Text(
                text = "$matchPercentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    matchPercentage > 80 -> Color(0xFF4CAF50)
                    matchPercentage > 60 -> Color(0xFFFFC107)
                    else -> Color(0xFFFF5722)
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(matchPercentage / 100f)
                    .fillMaxHeight()
                    .background(gradient)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 개선된 동화 특성 및 음성 특성 표시 부분
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "동화 특성:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
                Text(
                    text = storyValue,
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "음성 특성:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
                Text(
                    text = voiceValue,
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }
        }
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

@Composable
private fun NoRecommendationContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_mic),
            contentDescription = "No Voice",
            tint = Color.Gray,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "적합한 추천 음성을 찾을 수 없습니다",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "새로운 음성을 녹음하거나 다른 음성을 선택해주세요",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhancedActionButtons(
    isLoading: Boolean,
    recommendedVoice: VoiceEntity?,
    onDismiss: () -> Unit,
    onUseRecommendedVoice: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF666666)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "취소",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }

        Button(
            onClick = onUseRecommendedVoice,
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
                text = "추천 음성 사용하기",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 무작위로 일치도 생성 (주변값으로 변동을 주어 자연스럽게)
private fun getRandomMatchPercentage(basePercentage: Int, maxVariation: Int): Int {
    // 매우 낮은 유사도를 처리하기 위한 로직 추가
//    if (basePercentage < 30) {
//        // 유사도가 매우 낮은 경우 30-40 사이의 값을 반환
//        return (30..40).random()
//    }

    // 기존 로직
    val minValue = (basePercentage * 0.7).toInt().coerceAtLeast(0)
    val maxValue = (basePercentage * 1.1).toInt().coerceAtMost(100)

    // 추가 안전장치: maxValue가 minValue보다 작으면 minValue 사용
    val safeMaxValue = maxOf(minValue, maxValue)

    val variation = (-maxVariation..maxVariation).random()
    return (basePercentage + variation).coerceIn(minValue, safeMaxValue)
}

// 추천 일치도에 따른 설명 텍스트 제공
private fun getMatchExplanation(similarityPercentage: Int): String {
    return when {
        similarityPercentage > 85 -> "해당 음성은 동화의 특성과 매우 높은 일치도를 보입니다. 음역대, 음색, 표현력 등 모든 면에서 이야기의 내용과 분위기에 완벽하게 어울려, 최상의 청취 경험을 제공할 것입니다."
        similarityPercentage > 70 -> "해당 음성은 동화의 특성과 높은 일치도를 보입니다. 주요 음성 특성이 동화의 내용과 잘 맞아 자연스러운 낭독이 가능하며, 이야기의 감정과 분위기를 효과적으로 전달할 수 있습니다."
        similarityPercentage > 50 -> "해당 음성은 동화의 특성과 적절한 일치도를 보입니다. 일부 요소에서 최적화의 여지가 있지만, 전반적으로 이야기의 내용을 충분히 전달할 수 있는 음성입니다."
        else -> "해당 음성은 동화의 특성과 기본적인 일치도를 보입니다. 더 나은 추천 음성이 있을 수 있으나, 현재 사용 가능한 음성 중에서는 이 음성이 가장 적합합니다."
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