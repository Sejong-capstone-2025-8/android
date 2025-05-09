package com.toprunner.imagestory.ui.components

import android.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.model.VoiceFeatures
import kotlin.math.min

@Composable
fun ImprovedVoiceFeatureVisualization(voiceFeatures: VoiceFeatures) {
    // 애니메이션 효과를 위한 상태 변수
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    // 컴포넌트가 처음 표시될 때 애니메이션 시작
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)  // 외부 패딩 감소
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F8F8))
            .padding(8.dp)  // 내부 패딩 감소
    ) {
        // 헤더 섹션
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4285F4)) // 구글 파랑
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "음성 분석 결과",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // 피치 정보 섹션
        val pitchProgress = (voiceFeatures.averagePitch / 350f).coerceIn(0.0, 1.0)
        val animatedPitchProgress = animatedProgress.value * pitchProgress.toFloat()

        VoiceFeatureCard(
            title = "평균 피치",
            value = "${voiceFeatures.averagePitch.toInt()} Hz",
            progress = animatedPitchProgress,
            gradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFF4285F4), Color(0xFF34A853)) // 파랑 -> 초록
            ),
            description = getPitchRangeDescription(voiceFeatures.averagePitch),
            details = getPitchDescription(voiceFeatures.averagePitch)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 변동성 정보 섹션
        val variabilityProgress = (voiceFeatures.pitchStdDev / 150f).coerceIn(0.0, 1.0)
        val animatedVariabilityProgress = animatedProgress.value * variabilityProgress.toFloat()

        VoiceFeatureCard(
            title = "피치 변동성",
            value = "${voiceFeatures.pitchStdDev.toInt()} Hz",
            progress = animatedVariabilityProgress,
            gradient = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF4285F4),
                    Color(0xFFEA4335)) // 파랑 -> 빨강
            ),
            description = getStdDevRangeDescription(voiceFeatures.pitchStdDev),
            details = getStdDevDescription(voiceFeatures.pitchStdDev)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // MFCC 히트맵 섹션
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "음색 분포 (MFCC)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4285F4)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MFCC는 소리의 음색과 특성을 나타내는 지표입니다",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                EnhancedMFCCHeatmap(
                    mfccValues = voiceFeatures.mfccValues,
                    animationProgress = animatedProgress.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // MFCC 설명 섹션
                MFCCExplanation()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 음성 특성 요약
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "음성 특성 요약",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4285F4)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = generateVoiceDescription(voiceFeatures),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF525252)
                )
            }
        }
    }
}

@Composable
fun VoiceFeatureCard(
    title: String,
    value: String,
    progress: Float,
    gradient: Brush,
    description: String,
    details: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4285F4)
                )

                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 커스텀 프로그레스 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(gradient)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = details,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EnhancedMFCCHeatmap(
    mfccValues: List<DoubleArray>,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // MFCC 히트맵
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val width = size.width
            val height = size.height

            if (mfccValues.isEmpty()) return@Canvas

            // MFCC 값 그리기
            val numFrames = min(mfccValues.size, 13) // 최대 13개 프레임만 표시
            val numCoeffs = min(mfccValues.firstOrNull()?.size ?: 0, 13) // 최대 13개 계수만 표시

            val cellWidth = width / numCoeffs
            val cellHeight = height / numFrames

            for (frameIdx in 0 until (numFrames * animationProgress).toInt()) {
                val frame = mfccValues[frameIdx]

                for (coeffIdx in 0 until numCoeffs) {
                    if (coeffIdx >= frame.size) continue

                    val value = frame[coeffIdx]
                    // 값 정규화 (-20 ~ 20 범위 가정)
                    val normalizedValue = ((value + 20.0) / 40.0).coerceIn(0.0, 1.0)

                    // 색상 계산 (파랑 -> 빨강으로 변화)
                    val color = interpolateColor(
                        Color(0xFF4285F4), // 파랑
                        Color(0xFFEA4335), // 빨강
                        normalizedValue.toFloat()
                    )

                    // 테두리가 있는 사각형 그리기
                    drawRect(
                        color = color,
                        topLeft = Offset(coeffIdx * cellWidth, frameIdx * cellHeight),
                        size = Size(cellWidth - 1, cellHeight - 1)
                    )
                }
            }

            // 프레임 및 계수 구분선
            for (i in 0..numFrames) {
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(0f, i * cellHeight),
                    end = Offset(width, i * cellHeight),
                    strokeWidth = 1f
                )
            }

            for (i in 0..numCoeffs) {
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(i * cellWidth, 0f),
                    end = Offset(i * cellWidth, height),
                    strokeWidth = 1f
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp)
                    .background(Color(0xFF2F2F2F))
            )

            Text(
                text = " 세로 축 : 시간 프레임",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .height(1.dp)
                    .width(12.dp)
                    .background(Color(0xFF2F2F2F))
            )

            Text(
                text = " 가로 축 : 계수",
                fontSize = 12.sp,
                color = Color.Gray
            )

        }

        // 히트맵 범례
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF4285F4)) // 파랑
            )

            Text(
                text = " 낮은 값",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF7B1FA2)) // 보라
            )

            Text(
                text = " 중간 값",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(10.dp))


            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFEA4335)) // 빨강
            )

            Text(
                text = " 높은 값",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MFCCExplanation() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "MFCC 계수가 나타내는 의미:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4285F4)
        )

        Spacer(modifier = Modifier.height(8.dp))

        MFCCCoefficientExplanationItem(
            index = "1",
            description = "전체적인 에너지 및 음량 크기"
        )

        MFCCCoefficientExplanationItem(
            index = "2-5",
            description = "음성의 낮은 주파수 특성 (음색 기본 특성)"
        )

        MFCCCoefficientExplanationItem(
            index = "6-9",
            description = "중간 주파수 대역의 특성 (성별, 연령 관련)"
        )

        MFCCCoefficientExplanationItem(
            index = "10-13",
            description = "높은 주파수 특성 (발음의 선명도, 특징적 음색)"
        )
    }
}

@Composable
fun MFCCCoefficientExplanationItem(
    index: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "계수 $index:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
    }
}

// 색상 보간 함수
fun DrawScope.interpolateColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = 1f
    )
}

// 피치 범위 설명 생성
fun getPitchRangeDescription(pitch: Double): String {
    return when {
        pitch < 85 -> "매우 낮은 음역대"
        pitch < 125 -> "낮은 음역대"
        pitch < 165 -> "중간-낮은 음역대"
        pitch < 210 -> "중간 음역대"
        pitch < 235 -> "중간-높은 음역대"
        pitch < 265 -> "높은 음역대"
        else -> "매우 높은 음역대"
    }
}

// 피치 상세 설명 생성
fun getPitchDescription(pitch: Double): String {
    return when {
        pitch < 85 -> "깊고 울림이 있는 목소리 (매우 낮은 남성 목소리)"
        pitch < 125 -> "낮고 안정감 있는 목소리 (낮은 남성 목소리)"
        pitch < 165 -> "차분하고 신뢰감 있는 목소리 (일반적인 남성 목소리)"
        pitch < 210 -> "균형 잡힌 중성적인 목소리 (중성적인 목소리)"
        pitch < 235 -> "밝고 선명한 목소리 (일반적인 여성 목소리)"
        pitch < 265 -> "높고 경쾌한 목소리 (높은 여성 목소리)"
        else -> "매우 높고 가벼운 목소리 (아동 또는 매우 높은 여성 목소리)"
    }
}

// 표준편차 범위 설명 생성
fun getStdDevRangeDescription(stdDev: Double): String {
    return when {
        stdDev < 50 -> "낮은 변동성"
        stdDev < 90 -> "중간 변동성"
        stdDev < 120 -> "높은 변동성"
        else -> "매우 높은 변동성"
    }
}

// 표준편차 상세 설명 생성
fun getStdDevDescription(stdDev: Double): String {
    return when {
        stdDev < 50 -> "일정하고 안정적인 억양으로 차분한 동화 낭독에 적합합니다."
        stdDev < 90 -> "자연스러운 억양 변화로 대부분의 동화 낭독에 적합합니다."
        stdDev < 120 -> "표현력이 풍부한 억양으로 감정적인 장면이 있는 동화에 적합합니다."
        else -> "극적인 변화가 큰 억양으로 모험적이고 흥미진진한 동화에 적합합니다."
    }
}

// 종합적인 음성 설명 생성
fun generateVoiceDescription(features: VoiceFeatures): String {
    val pitchCategory = when {
        features.averagePitch < 165 -> "낮은"
        features.averagePitch < 210 -> "중간"
        else -> "높은"
    }

    val variability = when {
        features.pitchStdDev < 50 -> "안정적이고 일정한"
        features.pitchStdDev < 90 -> "자연스러운"
        else -> "표현력이 풍부한"
    }

    val suitableTheme = when {
        features.averagePitch < 150 && features.pitchStdDev > 90 -> "판타지나 모험"
        features.averagePitch > 230 && features.pitchStdDev < 90 -> "따뜻한 동화나 사랑"
        features.averagePitch < 160 && features.pitchStdDev < 50 -> "차분한 교훈적 이야기"
        features.averagePitch > 210 && features.pitchStdDev > 120 -> "흥미진진한 모험"
        features.averagePitch < 130 && features.pitchStdDev > 70 -> "비극적이거나 깊은 감정의 이야기" // 비극 관련 내용 추가

        else -> "다양한 장르의"
    }

    return "이 음성은 ${pitchCategory} 음역대와 ${variability} 억양을 가지고 있어 " +
            "${suitableTheme} 테마의 동화 낭독에 적합합니다. 음색 패턴을 분석한 결과, " +
            "청자에게 몰입감 있는 경험을 제공할 수 있는 특성을 갖추고 있습니다."
}