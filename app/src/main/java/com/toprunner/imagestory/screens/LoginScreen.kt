package com.toprunner.imagestory.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.toprunner.imagestory.R
import androidx.compose.ui.text.TextStyle  // 올바른 TextStyle 임포트
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    // 구글 로그인 관련 설정
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))  // 클라이언트 ID (Firebase Console에서 제공)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)

    }
    // Firebase에 구글 인증 정보 전달
    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("LoginScreen", "Google sign-in success: ${account.id}")
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Log.w("LoginScreen", "Google sign-in failed. Status code: ${e.statusCode}")
            Toast.makeText(context, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    val customFontFamily = FontFamily(
        Font(R.font.font)  // OTF 파일을 참조
    )

    Column(
        modifier = Modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "로그인",
            style = TextStyle(  // TextStyle 객체로 설정
                fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                fontSize = 50.sp,               // 텍스트 크기 설정
                fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                color = Color.Black,            // 텍스트 색상
                letterSpacing = 1.5.sp,         // 글자 간격
                textAlign = TextAlign.Center   // 텍스트 정렬
            )
        )
        Spacer(modifier = Modifier.height(36.dp))


        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") },singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") },singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        //이메일 로그인
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                // 이메일 또는 비밀번호가 비어있을 경우
                Toast.makeText(context, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }, modifier = Modifier
            .width(85.dp)
            .height(56.dp),
        ){
            Text(
                text = "확인",
                style = TextStyle(  // TextStyle 객체로 설정
                    fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                    fontSize = 22.sp,               // 텍스트 크기 설정
                    fontWeight = FontWeight.Light, // 텍스트 굵기 설정
                    color = Color.Black,            // 텍스트 색상
                    letterSpacing = 1.5.sp,         // 글자 간격
                    textAlign = TextAlign.Center   // 텍스트 정렬
                )
            )

        }
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = "구글 로그인",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )

        }
        Spacer(modifier = Modifier.height(8.dp))

        //회원가입 버튼
        TextButton(onClick = onRegisterClick) {
            Text("회원가입",color = Color.Gray)

        }
    }
}