package com.toprunner.imagestory.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.toprunner.imagestory.R
import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.google.firebase.auth.FirebaseAuth
//import com.toprunner.imagestory.navigation.NavRoute

//@Composable
//fun LoginScreen(navController: NavController) {
//    val context = LocalContext.current
//    val auth = remember { FirebaseAuth.getInstance() }
//
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("로그인", style = MaterialTheme.typography.headlineMedium)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("이메일") },
//            singleLine = true,
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("비밀번호") },
//            singleLine = true,
//            visualTransformation = PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                isLoading = true
//                auth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        isLoading = false
//                        if (task.isSuccessful) {
//                            Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
//                            navController.navigate(NavRoute.Home.route) {
//                                popUpTo(NavRoute.Login.route) { inclusive = true }
//                            }
//                        } else {
//                            Toast.makeText(context, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                            Log.e("LoginScreen", "Login failed", task.exception)
//                        }
//                    }
//            },
//            enabled = !isLoading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("로그인")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextButton(
//            onClick = {
//                // 간단한 회원가입 예시
//                if (email.isNotBlank() && password.isNotBlank()) {
//                    auth.createUserWithEmailAndPassword(email, password)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Toast.makeText(context, "회원가입 성공! 로그인하세요.", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(context, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                } else {
//                    Toast.makeText(context, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("회원가입")
//        }
//    }
//}


@Composable
fun LoginScreen(
    onLoginClicked: (email: String, password: String) -> Unit,
    onSignUpClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("로그인", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.example_image), // 여기에 리소스 이미지 id 사용
            contentDescription = "앱 로고",
            modifier = Modifier
                .fillMaxWidth(1f) // 화면 너비의 60%만큼만 사용
                .aspectRatio(1f)    // 가로:세로 비율 1:1
                .padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginClicked(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { onSignUpClicked() }) {
            Text("회원가입")
        }
    }
}

@Composable
fun MyApp() {
    LoginScreen(
        onLoginClicked = { email, password ->
            Log.d("Login", "이메일: $email, 비밀번호: $password")
            // 예: 서버 요청 or Firebase 로그인 처리
        },
        onSignUpClicked = {
            Log.d("Login", "회원가입 클릭됨")
            // 예: 회원가입 화면 이동
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // 임시로 클릭 이벤트를 비워둠
    LoginScreen(
        onLoginClicked = { _, _ -> },
        onSignUpClicked = { }
    )
}