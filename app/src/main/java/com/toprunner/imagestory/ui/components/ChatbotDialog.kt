package com.toprunner.imagestory.ui.components


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.OutlinedTextField as M2OutlinedTextField
import androidx.compose.material.TextFieldDefaults as M2TextFieldDefaults
import androidx.compose.material.Text as M2Text

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ChatbotDialog(
    conversationHistory: List<String>,
    userMessage: String,
    isLoading: Boolean,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = {},
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 제목 바
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE9D364))
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Text(
                        "동화 챗봇",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                    }
                }

                // 메시지 영역
                val listState = rememberLazyListState()
                LaunchedEffect(conversationHistory.size, isLoading) {
                    // 가장 마지막으로 스크롤
                    listState.animateScrollToItem(conversationHistory.size + if (isLoading) 1 else 0)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    items(conversationHistory) { raw ->
                        // "나: ..." or "동화 챗봇: ..."
                        val isUser = raw.startsWith("나:")
                        val text = raw.substringAfter(": ").trim()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (isUser) Color(0xFFDCF8C6) else Color.White,
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 1.dp,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    if (isLoading) {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.widthIn(max = 240.dp)
                                ) {
                                    Text(
                                        text = "로딩중입니다...",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }


                // 입력창 + 전송 버튼
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    M2OutlinedTextField(
                        value = userMessage,
                        onValueChange = onMessageChange,
                        placeholder = { M2Text("메시지를 입력하세요…") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = M2TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor   = Color(0xFFE9D364),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor          = Color(0xFFE9D364)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            onSend()
                            onMessageChange("")      // 입력창 클리어
                            focusManager.clearFocus() // 키보드 내리기
                        })
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {onSend()
                            onMessageChange("")
                            focusManager.clearFocus()},
                        enabled = userMessage.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (userMessage.isNotBlank()) Color(0xFFE9D364) else Color.LightGray,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "전송",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}