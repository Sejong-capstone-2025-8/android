import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.toprunner.imagestory.R
import java.time.format.TextStyle

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


//firebase 로그인 인증 과정
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

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
        Spacer(modifier = Modifier.height(12.dp))
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
            .fillMaxWidth()
            .height(56.dp))
        {
            Text("로그인")
        }
        //회원가입 버튼
        TextButton(onClick = onRegisterClick) {
            Text("회원가입",color = Color.Gray)

        }
        // 구글 로그인 버튼 추가
        Spacer(modifier = Modifier.height(16.dp))
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
    }
}
