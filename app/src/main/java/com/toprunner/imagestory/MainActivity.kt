package com.toprunner.imagestory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.toprunner.imagestory.controller.StoryCreationController
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var capturedImageUri by mutableStateOf<Uri?>(null)
    private var capturedImageBitmap by mutableStateOf<Bitmap?>(null)
    private var selectedTheme by mutableStateOf<String?>(null)
    private val storyCreationController by lazy { StoryCreationController(this) }
    private var isLoading by mutableStateOf(false)

    // 카메라 권한 요청
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "카메라 사용 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 저장소 권한 요청
    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 실행 결과 처리
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d("CameraDebug", "Take picture result: $success")
        if (success && capturedImageUri != null) {
            try {
                capturedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, capturedImageUri)
                Log.d("CameraDebug", "Image captured successfully")
            } catch (e: Exception) {
                Log.e("CameraDebug", "Error processing captured image: ${e.message}", e)
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("CameraDebug", "Image capture failed or cancelled")
        }
    }

    // 갤러리에서 이미지 선택 결과 처리
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedImageUri = it
            try {
                capturedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } catch (e: Exception) {
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageStoryTheme {
                StoryApp(
                    capturedImageBitmap = capturedImageBitmap,
                    selectedTheme = selectedTheme,
                    isLoading = isLoading,
                    onTakePhotoClicked = { checkCameraPermissionAndOpenCamera() },
                    onPickImageClicked = { checkStoragePermissionAndOpenGallery() },
                    onThemeSelected = { theme -> selectedTheme = theme },
                    onGenerateStoryClicked = { startStoryCreation() },
                    onNavigationItemClicked = { screen -> navigateToScreen(screen) }
                )
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        Log.d("CameraDebug", "Checking camera permission")
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                Log.d("CameraDebug", "Camera permission granted, opening camera")
                openCamera()
            }
            else -> {
                Log.d("CameraDebug", "Requesting camera permission")
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermissionAndOpenGallery() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                requestStoragePermissionLauncher.launch(permission)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoFile?.let {
                capturedImageUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    it
                )

                Log.d("CameraDebug", "Camera URI: $capturedImageUri")

                val currentUri = capturedImageUri
                if (currentUri != null) {
                    takePictureLauncher.launch(currentUri)
                } else {
                    Toast.makeText(this, "이미지 URI가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "임시 파일을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e("CameraDebug", "Failed to create temp file")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "카메라를 열 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("CameraDebug", "Camera error: ${e.message}", e)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = cacheDir

            Log.d("CameraDebug", "Creating file in: ${storageDir.absolutePath}")

            val file = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
            Log.d("CameraDebug", "File created: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("CameraDebug", "Error creating image file: ${e.message}", e)
            null
        }
    }

    private fun navigateToGeneratedStoryScreen(storyId: Long) {
        val intent = Intent(this, GeneratedStoryActivity::class.java).apply {
            putExtra("STORY_ID", storyId)
        }
        startActivity(intent)
    }

    private fun navigateToScreen(screen: String) {
        when (screen) {
            "home" -> { /* Already in home screen */ }
            "fairytale_list" -> {
                val intent = Intent(this, FairyTaleListActivity::class.java)
                startActivity(intent)
            }
            "voice_list" -> {
                val intent = Intent(this, VoiceListActivity::class.java)
                startActivity(intent)
            }
            "music_list" -> {
                val intent = Intent(this, MusicListActivity::class.java)
                startActivity(intent)
            }
            "settings" -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> Log.d("Navigation", "Unknown screen: $screen")
        }
    }

    private fun startStoryCreation() {
        capturedImageBitmap?.let { bitmap ->
            selectedTheme?.let { theme ->
                isLoading = true
                lifecycleScope.launch {
                    try {
                        val storyId = storyCreationController.createStory(bitmap, theme)
                        isLoading = false
                        navigateToGeneratedStoryScreen(storyId)
                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(this@MainActivity, "동화 생성에 실패했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("StoryCreation", "Error creating story", e)
                    }
                }
            } ?: run {
                Toast.makeText(this, "테마를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "사진을 찍거나 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun StoryApp(
    capturedImageBitmap: Bitmap?,
    selectedTheme: String?,
    isLoading: Boolean = false,
    onTakePhotoClicked: () -> Unit,
    onPickImageClicked: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onGenerateStoryClicked: () -> Unit,
    onNavigationItemClicked: (String) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val themeOptions = remember { listOf("판타지", "사랑", "SF", "공포", "코미디") }
    val themeButtonText = selectedTheme ?: "테마"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 상단 바 (헤더)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 22.dp)
            ) {
                Text(
                    text = "홈",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.5.dp,
                color = Color(0xFFE0E0E0)
            )

            // 메인 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (capturedImageBitmap != null) {
                    Image(
                        bitmap = capturedImageBitmap.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.example_image),
                        contentDescription = "Default Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 첫 번째 행: 사진 찍기, 갤러리에서 불러오기, 테마 버튼 나란히 배치
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 사진 찍기 버튼
                    Button(
                        onClick = { onTakePhotoClicked() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE566),
                            contentColor = Color(0xFF1C1C0D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "사진 찍기",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1C1C0D)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "사진 찍기",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // 갤러리에서 불러오기 버튼
                    Button(
                        onClick = { onPickImageClicked() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE566),
                            contentColor = Color(0xFF1C1C0D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bookmark),
                            contentDescription = "갤러리",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1C1C0D)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "갤러리",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // 테마 버튼
                    Button(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE566),
                            contentColor = Color(0xFF1C1C0D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        ),
                        enabled = !isLoading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "테마 선택",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1C1C0D)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = themeButtonText,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 동화 생성하기 버튼 (강조)
                Button(
                    onClick = { onGenerateStoryClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD166),
                        contentColor = Color(0xFF1C1C0D)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 12.dp
                    ),
                    enabled = !isLoading && capturedImageBitmap != null && selectedTheme != null
                ) {
                    Text(
                        text = "동화 생성하기",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.5.dp,
                color = Color(0xFFE0E0E0)
            )

            // 하단 네비게이션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconTint = Color(0xFFAA8866)

                    BottomNavItem(
                        iconResId = R.drawable.ic_home,
                        text = "홈화면",
                        tint = iconTint,
                        isSelected = true,
                        onClick = { onNavigationItemClicked("home") }
                    )
                    BottomNavItem(
                        iconResId = R.drawable.ic_bookmark,
                        text = "동화 리스트",
                        tint = iconTint,
                        onClick = { onNavigationItemClicked("fairytale_list") }
                    )
                    BottomNavItem(
                        iconResId = R.drawable.ic_bookmark,
                        text = "목소리 리스트",
                        tint = iconTint,
                        onClick = { onNavigationItemClicked("voice_list") }
                    )
                    BottomNavItem(
                        iconResId = R.drawable.ic_music,
                        text = "음악 리스트",
                        tint = iconTint,
                        onClick = { onNavigationItemClicked("music_list") }
                    )
                    BottomNavItem(
                        iconResId = R.drawable.ic_settings,
                        text = "설정",
                        tint = iconTint,
                        onClick = { onNavigationItemClicked("settings") }
                    )
                }
            }
        }

        // 로딩 표시
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
    }

    // 테마 선택 대화상자
    if (showThemeDialog) {
        ThemeSelectionDialog(
            themeOptions = themeOptions,
            onThemeSelected = { theme ->
                onThemeSelected(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    themeOptions: List<String>,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "테마 선택",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    items(themeOptions) { theme ->
                        ThemeItem(
                            theme = theme,
                            onThemeSelected = onThemeSelected
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "취소",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun ThemeItem(theme: String, onThemeSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onThemeSelected(theme) }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = theme,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

