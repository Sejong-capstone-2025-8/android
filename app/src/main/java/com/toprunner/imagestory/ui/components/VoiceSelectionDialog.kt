package com.toprunner.imagestory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.toprunner.imagestory.R
import com.toprunner.imagestory.data.entity.VoiceEntity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VoiceSelectionDialog(
    voices: List<VoiceEntity>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectVoice: (VoiceEntity) -> Unit
) {
    var selectedVoice by remember { mutableStateOf<VoiceEntity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "낭독용 음성 선택",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "닫기",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 안내 텍스트
                Text(
                    text = "동화를 낭독할 음성을 선택해주세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 구분선
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // 음성 목록
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFE9D364))
                    }
                } else if (voices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "사용 가능한 낭독용 음성이 없습니다.\n'음성 리스트' 화면에서 낭독용 음성을 생성해주세요.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(voices) { voice ->
                            VoiceListItem(
                                voice = voice,
                                isSelected = voice == selectedVoice,
                                onClick = { selectedVoice = voice }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        Text("취소")
                    }

                    Button(
                        onClick = {
                            selectedVoice?.let { onSelectVoice(it) }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE9D364),
                            contentColor = Color.Black,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedVoice != null
                    ) {
                        Text("동화 생성")
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceListItem(
    voice: VoiceEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(voice.created_at))

    val isClonedVoice = try {
        val attributeJson = JSONObject(voice.attribute)
        attributeJson.optBoolean("isClone", false)
    } catch (e: Exception) {
        false
    }

    // 배경색 설정
    val backgroundColor = if (isSelected) Color(0xFFFEF9E7) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color(0xFFE9B44C) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEED0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isClonedVoice) R.drawable.ic_volume_up else R.drawable.ic_mic),
                contentDescription = "Voice",
                tint = Color(0xFFE9B44C),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = voice.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isClonedVoice) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "낭독용 음성",
                    fontSize = 12.sp,
                    color = Color(0xFFE9B44C),
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFEED0),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // 선택 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9D364))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bookmark),
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}