package com.toprunner.imagestory.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.FairyTaleEntity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FairyTaleItemCard(
    fairyTale: FairyTaleEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(fairyTale.created_at))

    val attributes = try {
        JSONObject(fairyTale.attribute)
    } catch (e: Exception) {
        JSONObject()
    }

    // 추천된 음성 버전인지 확인
    val isRecommendedVoiceVersion = attributes.optBoolean("isRecommendedVoiceVersion", false)
    val isSelectedVoiceVersion = attributes.optBoolean("isSelectedVoiceVersion", false)
    val creationMethod = attributes.optString("creationMethod", "")





    // 인터랙션 상태를 추적하기 위한 소스 생성
    val interactionSource = remember { MutableInteractionSource() }

    // 버튼이 눌렸는지 확인
    val isPressed by interactionSource.collectIsPressedAsState()

    // 눌렸을 때 크기 변화 - 약간 축소되는 효과
    val scale = if (isPressed) 0.93f else 1f

    // 애니메이션 처리
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 배경색 결정
    val backgroundColor = when {
        isRecommendedVoiceVersion -> Color(0xFFFFEFBB) // 추천 음성용 배경색(노란빛)
        isSelectedVoiceVersion -> Color(0xFFD5BEFF)    // 선택 음성용 배경색(보라빛)
        else -> Color(0xFFFFFBF0)                      // 기본 배경색
    }
    // NeuomorphicBox 적용 - 크기 변화 애니메이션 추가
    NeuomorphicBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 기본 리플 효과 제거
                onClick = onClick
            ),
        backgroundColor = backgroundColor,
        elevation = 4.dp,
        cornerRadius = 12.dp
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
                    .clip(RoundedCornerShape(8.dp))
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fairyTale.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 음성 유형에 따라 다른 뱃지 표시
                when {
                    isRecommendedVoiceVersion -> {
                        Text(
                            text = "추천된 음성 버전",
                            fontSize = 12.sp,
                            color = Color(0xFFE9B44C),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFEED0))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    isSelectedVoiceVersion -> {
                        Text(
                            text = "선택한 음성 버전",  // 선택한 경우 다른 텍스트 표시
                            fontSize = 12.sp,
                            color = Color(0xFF8A54AE),  // 보라색 계열
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF3E5FF))  // 옅은 보라색 배경
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

            }

            // 삭제 아이콘
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 화살표 아이콘 (상세화면 이동)
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Open",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

