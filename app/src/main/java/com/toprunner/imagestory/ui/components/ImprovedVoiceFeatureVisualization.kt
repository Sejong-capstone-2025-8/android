package com.toprunner.imagestory.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.model.VoiceFeatures
import kotlin.math.min

@Composable
fun ImprovedVoiceFeatureVisualization(voiceFeatures: VoiceFeatures) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        // 헤더
        Text(
            text = "음성 특징 분석 결과",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 평균 피치 섹션
        VoiceFeatureItem(
            title = "평균 피치",
            value = "${voiceFeatures.averagePitch.toInt()} Hz",
            progress = (voiceFeatures.averagePitch / 300f).coerceIn(0.0, 1.0),
            progressColor = Color(0xFF4A90E2),
            description = getPitchDescription(voiceFeatures.averagePitch)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 피치 변동성 섹션
        VoiceFeatureItem(
            title = "피치 변동성",
            value = "${voiceFeatures.pitchStdDev.toInt()} Hz",
            progress = (voiceFeatures.pitchStdDev / 40f).coerceIn(0.0, 1.0),
            progressColor = Color(0xFF50E3C2),
            description = getStdDevDescription(voiceFeatures.pitchStdDev)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // MFCC 히트맵
        Text(
            text = "음색 분포 (MFCC)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MFCCHeatmap(
            mfccValues = voiceFeatures.mfccValues,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 범례
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF1E88E5))
            )
            Text(
                text = " 낮은 값",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFE53935))
            )
            Text(
                text = " 높은 값",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 음성 특성 요약
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE3F2FD))
                .padding(12.dp)
        ) {
            Text(
                text = generateVoiceDescription(voiceFeatures),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun VoiceFeatureItem(
    title: String,
    value: String,
    progress: Double,
    progressColor: Color,
    description: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun MFCCHeatmap(
    mfccValues: List<DoubleArray>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (mfccValues.isEmpty()) return@Canvas

        // MFCC 값 그리기
        val numFrames = min(mfccValues.size, 5) // 최대 5개 프레임만 표시
        val numCoeffs = min(mfccValues.firstOrNull()?.size ?: 0, 13) // 최대 13개 계수만 표시

        val cellWidth = width / numCoeffs
        val cellHeight = height / numFrames

        for (frameIdx in 0 until numFrames) {
            val frame = mfccValues[frameIdx]

            for (coeffIdx in 0 until numCoeffs) {
                if (coeffIdx >= frame.size) continue

                val value = frame[coeffIdx]
                // 값 정규화 (-20 ~ 20 범위 가정)
                val normalizedValue = ((value + 20.0) / 40.0).coerceIn(0.0, 1.0)

                // 색상 계산 (파랑 -> 빨강)
                val color = interpolateColor(
                    Color(0xFF1E88E5), // 파랑
                    Color(0xFFE53935), // 빨강
                    normalizedValue.toFloat()
                )

                // 사각형 그리기
                drawRect(
                    color = color,
                    topLeft = Offset(coeffIdx * cellWidth, frameIdx * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
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

// 피치 설명 생성
fun getPitchDescription(pitch: Double): String {
    return when {
        pitch < 85 -> "매우 낮은 음역대 (매우 낮은 남성 목소리)"
        pitch < 110 -> "낮은 음역대 (낮은 남성 목소리)"
        pitch < 140 -> "중간-낮은 음역대 (일반적인 남성 목소리)"
        pitch < 175 -> "중간 음역대 (중성적인 목소리)"
        pitch < 210 -> "중간-높은 음역대 (일반적인 여성 목소리)"
        pitch < 250 -> "높은 음역대 (높은 여성 목소리)"
        else -> "매우 높은 음역대 (아동 또는 매우 높은 여성 목소리)"
    }
}

// 표준편차 설명 생성
fun getStdDevDescription(stdDev: Double): String {
    return when {
        stdDev < 10 -> "낮은 변동성 (단조로운 말투, 평탄한 억양)"
        stdDev < 20 -> "중간 변동성 (자연스러운 억양)"
        stdDev < 30 -> "높은 변동성 (표현력 있는 말투, 감정적인 억양)"
        else -> "매우 높은 변동성 (극적인 말투, 매우 다양한 억양)"
    }
}

// 종합적인 음성 설명 생성
fun generateVoiceDescription(features: VoiceFeatures): String {
    val pitchCategory = when {
        features.averagePitch < 110 -> "낮은"
        features.averagePitch < 175 -> "중간"
        else -> "높은"
    }

    val variability = when {
        features.pitchStdDev < 10 -> "단조로운"
        features.pitchStdDev < 20 -> "자연스러운"
        else -> "표현력 있는"
    }

    return "이 음성은 ${pitchCategory} 음역대와 ${variability} 억양을 가지고 있습니다. " +
            "주파수 분포 패턴을 기반으로 동화 낭독에 적합한 음성을 생성할 수 있습니다. " +
            "이 음성 특징은 동화의 테마와 내용에 맞춰 최적의 낭독 효과를 제공합니다."
}
