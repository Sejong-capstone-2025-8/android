package com.toprunner.imagestory

import android.R.attr.fontFamily
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.toprunner.imagestory.R
import androidx.compose.ui.text.TextStyle  // 올바른 TextStyle 임포트

@Composable
fun firstScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val customFontFamily = FontFamily(
        Font(R.font.font)  // OTF 파일을 참조
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.firstscreen_background), // 이미지 리소스 ID
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 가운데 텍스트 + 버튼
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "맞춤형으로 읽어주는\n사진 속 동화 여행",
                style = TextStyle(  // TextStyle 객체로 설정
                    fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                    fontSize = 40.sp,               // 텍스트 크기 설정
                    fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                    color = Color.White,            // 텍스트 색상
                    letterSpacing = 1.5.sp,         // 글자 간격
                    textAlign = TextAlign.Center   // 텍스트 정렬
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 220.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(onClick = onLoginClick) {
                Text(
                    text = "로그인",
                    style = TextStyle(  // TextStyle 객체로 설정
                        fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                        fontSize = 24.sp,               // 텍스트 크기 설정
                        fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                        color = Color.White,            // 텍스트 색상
                        letterSpacing = 1.5.sp,         // 글자 간격
                        textAlign = TextAlign.Center   // 텍스트 정렬
                    )
                )
            }

            //회원가입 버튼
            TextButton(onClick = onRegisterClick) {
                Text(
                    text = "회원가입",
                    style = TextStyle(  // TextStyle 객체로 설정
                        fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                        fontSize = 24.sp,               // 텍스트 크기 설정
                        fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                        color = Color.White,            // 텍스트 색상
                        letterSpacing = 1.5.sp,         // 글자 간격
                        textAlign = TextAlign.Center   // 텍스트 정렬
                    )
                )
            }
        }
    }
}
