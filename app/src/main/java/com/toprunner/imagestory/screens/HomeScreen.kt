package com.toprunner.imagestory.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.toprunner.imagestory.R

@Composable
fun AutoResizingText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 14.sp,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    style: TextStyle = TextStyle.Default
) {
    var currentFontSize by remember(text) { mutableStateOf(maxFontSize) }

    Text(
        text = text,
        modifier = modifier,
        fontSize = currentFontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layoutResult ->
            if (layoutResult.hasVisualOverflow && currentFontSize > minFontSize) {
                currentFontSize *= 0.9f
            }
        },
        style = style.copy(fontSize = currentFontSize)
    )
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    capturedImageBitmap: Bitmap?,
    selectedTheme: String?,
    isLoading: Boolean = false,
    onTakePhotoClicked: () -> Unit,
    onPickImageClicked: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onGenerateStoryClicked: () -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val themeOptions = remember { listOf("판타지", "사랑", "SF", "공포", "코미디") }
    val themeButtonText = selectedTheme ?: "테마 선택"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth > 600.dp
        val imageHeight = if (isWideScreen) 550.dp else 450.dp
        val buttonSpacing = if (isWideScreen) 16.dp else 8.dp
        val sidePadding = if (isWideScreen) 32.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBF0))
        ) {
            // 상단 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = 22.dp)
            ) {
                Text(
                    text = "홈",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.5.dp,
                color = Color(0xFFE0E0E0)
            )

            // 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = 16.dp)
            ) {
                if (capturedImageBitmap != null) {
                    val imageRatio = capturedImageBitmap.width.toFloat() / capturedImageBitmap.height
                    Image(
                        bitmap = capturedImageBitmap.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageRatio)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bookmark),
                                contentDescription = "No Image",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "사진을 찍거나 선택해주세요",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // 버튼들
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(buttonSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(48.dp)

                    Button(
                        onClick = onTakePhotoClicked,
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566)),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        IconText(iconId = R.drawable.ic_home, label = "사진 찍기")
                    }

                    Button(
                        onClick = onPickImageClicked,
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566)),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        IconText(iconId = R.drawable.ic_bookmark, label = "갤러리")
                    }

                    Button(
                        onClick = { showThemeDialog = true },
                        modifier = buttonModifier,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE566)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        IconText(iconId = R.drawable.ic_settings, label = themeButtonText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onGenerateStoryClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD166),
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading && capturedImageBitmap != null && selectedTheme != null
                ) {
                    Text(
                        text = "동화 생성하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (!isLoading && capturedImageBitmap != null && selectedTheme != null)
                            Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // 로딩 중일 때
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "동화를 생성 중입니다. 잠시만 기다려주세요...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 테마 선택 Dialog
        if (showThemeDialog) {
            Dialog(onDismissRequest = { showThemeDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "테마 선택",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            items(themeOptions) { theme ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onThemeSelected(theme)
                                            showThemeDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                ) {
                                    Text(text = theme, fontSize = 16.sp, color = Color.Black)
                                }
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showThemeDialog = false }) {
                                Text("취소", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(iconId: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        AutoResizingText(
            text = label,
            maxFontSize = 14.sp,
            minFontSize = 10.sp,
            modifier = Modifier.weight(1f), // 공간을 충분히 사용하게
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
