package com.toprunner.imagestory

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.toprunner.imagestory.data.database.AppDatabase
import com.toprunner.imagestory.navigation.NavRoute
import android.content.Context
import android.content.Intent
import android.os.Process
import android.preference.PreferenceManager

@Composable
fun ManageAccountScreen(
    navController: NavController
) {

    val firebaseAuth = FirebaseAuth.getInstance()
    val user = firebaseAuth.currentUser
    // ❶ 새 프로필 URL을 담을 상태
    var photoUrlState by remember { mutableStateOf(user?.photoUrl?.toString()) }

    // ❷ Lifecycle 이벤트로 화면 재진입 시마다 reload & 상태 갱신
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                firebaseAuth.currentUser
                    ?.reload()
                    ?.addOnSuccessListener {
                        photoUrlState = firebaseAuth.currentUser?.photoUrl?.toString()
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // 네비게이션 플래그
    var shouldNavigateToLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 로그아웃 후 상태 세팅 함수
    fun signOutAndNavigate() {
        firebaseAuth.signOut()
        shouldNavigateToLogin = true
    }
    // user가 없거나, 플래그가 세팅된 경우 한 번만 로그인 화면으로 이동
    if (user == null || shouldNavigateToLogin) {
        LaunchedEffect(shouldNavigateToLogin) {
            navController.navigate(NavRoute.Login.route) {
                popUpTo(NavRoute.ManageAccount.route) { inclusive = true }
            }
        }
        return
    }

    // --- 비밀번호 변경 다이얼로그용 상태 ---
    var showChangePwdDialog by remember { mutableStateOf(false) }
    // --- 계정 삭제 다이얼로그용 상태 ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    //  Google 로그인 여부
    val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

    var showInitializationDialog by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    var username by remember { mutableStateOf("불러오는 중...") } // username 상태 관리

    // Firestore에서 username 값을 가져오는 비동기 작업
    LaunchedEffect(userId) {
        if (isGoogleUser) {
            // Google 로그인은 displayName 사용
            username = user.displayName ?: "유저네임 없음"
        }
        else {
            userId?.let {
                db.collection("users").document(it).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            username = document.getString("username") ?: "유저네임 없음"
                        } else {
                            username = "유저네임 없음"
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Firestore", "Error getting documents: ", exception)
                        username = "유저네임 불러오기 실패"
                    }
            }
        }
    }

    val customFontFamily = FontFamily(
        Font(R.font.font)  // OTF 파일을 참조
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF0))
            .padding(16.dp)
    ) {
        // Header section
        Text(
            text = "계정 정보 관리",
            style = TextStyle(  // TextStyle 객체로 설정
                fontFamily = customFontFamily,  // 추가한 OTF 폰트 사용
                fontSize = 32.sp,               // 텍스트 크기 설정
                fontWeight = FontWeight.Medium, // 텍스트 굵기 설정
                color = Color.Black,            // 텍스트 색상
                letterSpacing = 1.5.sp,         // 글자 간격
                textAlign = TextAlign.Center   // 텍스트 정렬
            )
        )
// ---------------- Profile Picture Section ----------------
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top=10.dp)
        ) {
            if (photoUrlState != null) {
                AsyncImage(
                    model = photoUrlState,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 리소스 이미지는 painterResource 로 렌더링
                Image(
                    painter = painterResource(R.drawable.example_image),
                    contentDescription = "Default Profile Image",
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display account info
        user?.let {
            // 이메일
            AccountInfoRow("이메일 ", it.email ?: "이메일 없음")
            Spacer(modifier = Modifier.height(16.dp))

            // 유저네임 (Firestore에서 가져온 값)
            AccountInfoRow("유저네임 ", username)
            Spacer(modifier = Modifier.height(16.dp))

            // 계정 생성 시간 (밀리초를 Date로 변환)
            val creationTime = it.metadata?.creationTimestamp?.let { timestamp ->
                // Date 객체 생성
                val date = java.util.Date(timestamp)
                // 포맷터 생성 및 타임존 설정 (KST)
                val sdf = java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault()
                ).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
                }
                // 포맷 적용
                sdf.format(date)
            } ?: "생성 시간 없음"

            AccountInfoRow("계정 등록 시간 ", creationTime)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isGoogleUser) {
            Button(
                onClick = {
                    navController.navigate(NavRoute.EditAccount.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("계정 정보 수정", fontSize = 16.sp)
            }

        }

        if (!isGoogleUser) {
            Button(
            onClick = { showChangePwdDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp)
        ) {
            Text("비밀번호 변경", fontSize = 16.sp)
        }
        }

        Button(
            onClick = { showInitializationDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),

            ) {
            Text("계정 초기화", fontSize = 16.sp, color = Color.White)
        }

        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),


            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),

            ) {
            Text("계정 삭제", fontSize = 16.sp, color = Color.White)
        }

        // 변경 다이얼로그
        if (showChangePwdDialog) {
            // 상태 변수
            var currentPwd by remember { mutableStateOf("") }
            var newPwd by remember { mutableStateOf("") }
            var newPwdConfirm by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showChangePwdDialog = false },
                title = { Text("비밀번호 변경") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentPwd,
                            onValueChange = { currentPwd = it },
                            label = { Text("현재 비밀번호") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrect = false,
                                keyboardType = KeyboardType.Password
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPwd,
                            onValueChange = { newPwd = it },
                            label = { Text("새 비밀번호 (6자 이상)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrect = false,
                                keyboardType = KeyboardType.Password
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPwdConfirm,
                            onValueChange = { newPwdConfirm = it },
                            label = { Text("새 비밀번호 확인") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrect = false,
                                keyboardType = KeyboardType.Password
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // 입력 검증
                        if (newPwd.length < 6) {
                            Toast.makeText(context, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (newPwd != newPwdConfirm) {
                            Toast.makeText(context, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        // 재인증 및 업데이트
                        val email = user.email
                        if (email.isNullOrBlank()) {
                            Toast.makeText(context, "이메일 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val credential = EmailAuthProvider.getCredential(email, currentPwd)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                user.updatePassword(newPwd)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                        showChangePwdDialog = false
                                        // 로그아웃 + 네비게이션 플래그 세팅
                                        signOutAndNavigate()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "비밀번호 변경 실패: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                            }
                    },colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )) {
                        Text("변경")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePwdDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Black
                        )) {
                        Text("취소")
                    }
                }
            )
        }
        // 계정 삭제 확인 다이얼로그
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("계정 삭제") },
                text = { Text("정말 계정을 삭제하시겠습니까?\n삭제하면 복구할 수 없습니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        user.delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                signOutAndNavigate()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "계정 삭제 실패: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        showDeleteDialog = false
                    }) { Text("삭제", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("취소", color = Color.Red) }
                }
            )
        }

        val context = LocalContext.current

        fun clearAppData(context: Context) {
            context.filesDir?.deleteRecursively()
            context.cacheDir?.deleteRecursively()

            // ✅ Room 데이터베이스 삭제
            context.deleteDatabase("fairy_tale_database")
        }

        fun restartApp(context: Context) {
            val activity = context as? Activity ?: return
            val intent = Intent(context, activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.finish()
            context.startActivity(intent)

            // 완전한 프로세스 종료 후 재시작 효과
            Process.killProcess(Process.myPid())
        }

        if (showInitializationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("계정 초기화") },
                text = { Text("정말 초기화 하시겠습니까?\n이후 삭제된 동화는 복구할 수 없습니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        clearAppData(context) // 내부 저장소 삭제
                        showInitializationDialog = false
                        restartApp(context)
                    }) { Text("초기화", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showInitializationDialog = false }) { Text("취소", color = Color.Red) }
                }
            )
        }


//        // Button to go back
//        Spacer(modifier = Modifier.height(8.dp))
//        Button(
//            onClick = { navController.popBackStack() },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text("뒤로 가기", fontSize = 16.sp)
//        }
    }
}

@Composable
fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

