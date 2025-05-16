package com.toprunner.imagestory.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.toprunner.imagestory.R
import com.toprunner.imagestory.navigation.NavRoute
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign


@Composable
fun SettingsScreen(
    navController: NavController,
    onLogoutClicked: () -> Unit // onLogoutClicked를 외부에서 전달받음
) {
    val backgroundColor = Color(0xFFFFFBF0)

    val customFontFamily = FontFamily(
        Font(R.font.font)  // OTF 파일을 참조
    )

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
                        text = "설정",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F2E20)
                    )
                }

            }
        }
        SettingsItemCard(
            icon = R.drawable.ic_account,
            title = "계정 정보 관리",
            onClick = { navController.navigate(NavRoute.ManageAccount.route)}
        )

        SettingsItemCard(
            icon = R.drawable.ic_logout,
            title = "로그아웃",
            onClick = { onLogoutClicked() }
        )

        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }

        val developerEmails = listOf(
            "dev1@example.com",
            "dev2@example.com",
            "dev3@example.com",
            "dev4@example.com"
        )

        SettingsItemCard(
            icon = R.drawable.ic_notice,
            title = "문의하기",
            onClick = {
                showDialog = true
            }
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(
                    text = "확인하는대로 답변드리겠습니다",
                    style = TextStyle(  // TextStyle 객체로 설정
                        fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                        fontSize = 24.sp,               // 텍스트 크기 설정
                        fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                        color = Color.Black,            // 텍스트 색상
                        letterSpacing = 1.5.sp,         // 글자 간격
                        textAlign = TextAlign.Center   // 텍스트 정렬
                    )
                ) },
                text = {
                    Column {
                        developerEmails.forEach { email ->
                            TextButton(onClick = {
                            }) {
                                Text(
                                    text = email,
                                    style = TextStyle(  // TextStyle 객체로 설정
                                        fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                                        fontSize = 24.sp,               // 텍스트 크기 설정
                                        fontWeight = FontWeight.Light, // 텍스트 굵기 설정
                                        color = Color.Black,            // 텍스트 색상
                                        letterSpacing = 1.5.sp,         // 글자 간격
                                        textAlign = TextAlign.Center   // 텍스트 정렬
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(
                            text = "닫기",
                            style = TextStyle(  // TextStyle 객체로 설정
                                fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                                fontSize = 16.sp,               // 텍스트 크기 설정
                                fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                                color = Color.Black,            // 텍스트 색상
                                letterSpacing = 1.5.sp,         // 글자 간격
                                textAlign = TextAlign.Center   // 텍스트 정렬
                            )
                        )
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "앱 버전: 1.0.0",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SettingsItemCard(icon: Int, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFF9C8A54),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "More",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 외부에서 호출할 수 있도록 로그아웃 함수 Composable로 만들어 전달
@Composable
fun Logout(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // 기본 로그아웃 처리 로직
    firebaseAuth.signOut()

    // 구글 로그아웃 처리 추가
    val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
    googleSignInClient.signOut().addOnCompleteListener {
        Toast.makeText(context, "구글 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
        // Firebase 로그아웃도 함께 처리
        firebaseAuth.signOut()

        // 로그인 화면으로 이동
        navController.navigate(NavRoute.Login.route) {
            popUpTo(NavRoute.Home.route) { inclusive = true }
        }
    }
}