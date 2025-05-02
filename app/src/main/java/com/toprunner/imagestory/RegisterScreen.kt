package com.toprunner.imagestory

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("회원가입", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("사용자명") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            // 이메일 형식 확인
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "유효한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@Button
            }

            // 비밀번호 길이 체크 (6자 이상이어야 함)
            if (password.length < 6) {
                Toast.makeText(context, "비밀번호는 6글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
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
        }) {
            Text("회원가입")
        }
    }
}