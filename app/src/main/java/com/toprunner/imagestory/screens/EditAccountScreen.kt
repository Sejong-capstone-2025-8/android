package com.toprunner.imagestory.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.toprunner.imagestory.R

@Composable
fun EditAccountScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var username by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }

    // 기존 username 로드
    LaunchedEffect(user.uid) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc -> username = doc.getString("username") ?: "" }
    }

    // 이미지 선택기
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri -> photoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("계정 정보 수정", fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))

        // 프로필 사진 선택
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val currentPhoto = user.photoUrl
                if (currentPhoto != null) {
                    AsyncImage(
                        model = currentPhoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.example_image),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 유저네임 입력
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("유저네임") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = true,
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 저장 버튼
        Button(
            onClick = {
                loading = true
                val storageRef = storage.reference.child("profileImages/${user.uid}.jpg")
                // 디버그: 실제 업로드 경로와 버킷 출력
                Log.d(
                    "EditAccountScreen",
                    "Attempting upload to path='${storageRef.path}' in bucket='${storage.reference.bucket}'"
                )
                if (photoUri != null) {
                    // 사진 업로드
                    storageRef.putFile(photoUri!!)
                        .addOnSuccessListener {
                            // 업로드 성공 후 URL 가져오기
                            storageRef.downloadUrl
                                .addOnSuccessListener { uri ->

                                    // 프로필 및 Firestore 업데이트
                                    updateUserProfile(
                                        user = user,
                                        username = username,
                                        photoUri = uri,
                                        db = db,
                                        context = context,
                                        navController = navController
                                    ) { loading = false }
                                }
                                .addOnFailureListener { e ->
                                    loading = false
                                    Toast.makeText(context, "사진 URL 획득 실패: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            loading = false
                            Toast.makeText(context, "사진 업로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // 사진 변경 없을 때 기존 URL 사용
                    updateUserProfile(
                        user = user,
                        username = username,
                        photoUri = user.photoUrl,
                        db = db,
                        context = context,
                        navController = navController
                    ) { loading = false }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "저장 중…" else "저장")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.popBackStack() }) { Text("취소") }
    }
}

private fun updateUserProfile(
    user: FirebaseUser,
    username: String,
    photoUri: Uri?,
    db: FirebaseFirestore,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(username)
        .setPhotoUri(photoUri)
        .build()
    user.updateProfile(profileUpdates)
        .addOnCompleteListener { task ->
            onComplete()
            if (task.isSuccessful) {
                // Firestore username 업데이트
                db.collection("users").document(user.uid)
                    .update("username", username)
                Toast.makeText(context, "프로필 업데이트 완료", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(context, "프로필 업데이트 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}