package com.toprunner.imagestory.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.toprunner.imagestory.R

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    val customFontFamily = FontFamily(
        Font(R.font.font)  // OTF 파일을 참조
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "회원가입",
            style = TextStyle(  // TextStyle 객체로 설정
                fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                fontSize = 50.sp,               // 텍스트 크기 설정
                fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                color = Color.Black,            // 텍스트 색상
                letterSpacing = 1.5.sp,         // 글자 간격
                textAlign = TextAlign.Center   // 텍스트 정렬
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        // 사용자명
        Text("사용자명", color = Color.Black,style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("사용자명 입력") },singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // 이메일
        Text("이메일", color = Color.Black, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일 입력") },singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // 비밀번호
        Text("비밀번호", color = Color.Black, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호 입력 (6글자 이상)") },singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // 비밀번호 확인
        Text("비밀번호 확인", color = Color.Black, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("비밀번호 재입력") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "이용약관 및 개인정보취급방침에 동의합니다. (필수)",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (!termsAccepted) {
                Toast.makeText(context, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            // 이메일 형식 확인
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "유효한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            // 비밀번호 길이 체크 (6자 이상이어야 함)
            if (password.length < 6) {
                Toast.makeText(context, "비밀번호는 6글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (password != confirmPassword) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser  // 현재 로그인된 사용자
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email
                        )
                        // Firestore에 userId를 문서 ID로 사용하여 데이터 저장
                        user?.let {
                            firestore.collection("users").document(it.uid)  // uid를 문서 ID로 사용
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "회원가입 성공", Toast.LENGTH_SHORT).show()
                                    onRegisterSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "회원가입 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        },
            modifier = Modifier
                .width(85.dp)
                .height(56.dp)
        ) {
            Text(
                text = "완료",
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
    }
}