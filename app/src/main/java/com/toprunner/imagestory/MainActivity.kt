package com.toprunner.imagestory

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.toprunner.imagestory.controller.StoryCreationController
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.screens.*
import com.toprunner.imagestory.ui.components.BottomNavBar
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.NavHostController


class MainActivity : ComponentActivity() {

    // 이미지 관련 상태
    private var capturedImageUri by mutableStateOf<Uri?>(null)
    private var capturedImageBitmap by mutableStateOf<Bitmap?>(null)
    private var selectedTheme by mutableStateOf<String?>(null)
    private var isLoading by mutableStateOf(false)

    // 컨트롤러
    private val storyCreationController by lazy { StoryCreationController(this) }

    // 권한 요청 런처들
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "카메라 사용 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 및 갤러리 런처들
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            try {
                // API 28 이상일 경우 ImageDecoder 사용
                capturedImageBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, capturedImageUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, capturedImageUri)
                }
                Log.d("MainActivity", "Image captured successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error processing captured image: ${e.message}", e)
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedImageUri = it
            try {
                val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val rotatedBitmap = fixImageRotation(uri, originalBitmap)
                capturedImageBitmap = rotatedBitmap
            } catch (e: Exception) {
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fixImageRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val exif = inputStream?.use {
                androidx.exifinterface.media.ExifInterface(it)
            }

            val orientation = exif?.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            ) ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL

            val matrix = android.graphics.Matrix()
            when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap // 회전 보정 실패 시 원본 그대로 사용
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeDefaultData()

        setContent {
            ImageStoryTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    // innerPadding: Scaffold에서 내려주는 기본 패딩
                    // navigationBarsPadding(): 시스템 내비게이션 바 공간만큼 추가 패딩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            //.navigationBarsPadding()
                    ){NavHost(
                        navController = navController,
                        startDestination = NavRoute.Home.route,
                        modifier = Modifier.fillMaxSize()

                    ) {
                        // 홈 화면
                        composable(NavRoute.Home.route) {
                            HomeScreen(
                                capturedImageBitmap = capturedImageBitmap,
                                selectedTheme = selectedTheme,
                                isLoading = isLoading,
                                onTakePhotoClicked = { checkCameraPermissionAndOpenCameraNonCompose() },
                                onPickImageClicked = { checkStoragePermissionAndOpenGallery() },
                                onThemeSelected = { theme -> selectedTheme = theme },
                                onGenerateStoryClicked = { startStoryCreation(navController) }
                            )
                        }

                        // 동화 리스트 화면
                        composable(NavRoute.FairyTaleList.route) {
                            FairyTaleListScreen(
                                navController = navController
                            )
                        }

                        // 목소리 리스트 화면
                        composable(NavRoute.VoiceList.route) {
                            VoiceListScreen(
                                navController = navController,
                                onRecordNewVoiceClicked = {
                                    navController.navigate(NavRoute.VoiceRecording.route)
                                }
                            )
                        }

                        // 음악 리스트 화면
                        composable(NavRoute.MusicList.route) {
                            MusicListScreen(
                                navController = navController
                            )
                        }

                        // 설정 화면
                        composable(NavRoute.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                onLogoutClicked = {
                                    // 로그아웃 로직
                                    Toast.makeText(this@MainActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                                    // 홈 화면으로 이동
                                    navController.navigate(NavRoute.Home.route) {
                                        popUpTo(NavRoute.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 생성된 동화 화면
                        composable(
                            route = NavRoute.GeneratedStory.route,
                            arguments = listOf(navArgument("storyId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val storyId = backStackEntry.arguments?.getLong("storyId") ?: -1
                            if (storyId != -1L) {
                                GeneratedStoryScreen(
                                    storyId = storyId,
                                    navController = navController
                                )
                            }
                        }

                        // 목소리 녹음 화면
                        composable(NavRoute.VoiceRecording.route) {
                            VoiceRecordingScreen(
                                navController = navController
                            )
                        }
                    }}

                }
            }
        }
    }

    private fun AnimatedContentScope.VoiceRecordingScreen(navController: NavHostController) {}

    // 앱 초기화 로직
    private fun initializeDefaultData() {
        lifecycleScope.launch {
            try {
                // 데이터베이스 초기화, 기본 음성/음악 데이터 로딩 등
                Log.d("MainActivity", "Initializing default data")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing default data: ${e.message}", e)
            }
        }
    }


    // 일반 함수로 작성 – Compose와 분리 (비-Composable)
    private fun checkCameraPermissionAndOpenCameraNonCompose() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // 권한 요청 전에 사용자가 이미 거부한 적이 있으면 rationale 표시
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 전통적인 AlertDialog.Builder 사용 (Compose 외부이므로 문제 없음)
                AlertDialog.Builder(this)
                    .setTitle("카메라 권한 필요")
                    .setMessage("사진 촬영을 위해 카메라 권한이 필요합니다. 권한을 허용하시겠습니까?")
                    .setPositiveButton("허용") { dialog, _ ->
                        requestCameraPermissionLauncher.launch(permission)
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        Toast.makeText(this, "카메라 사용 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            } else {
                // 권한 요청 (첫 요청 또는 rationale 표시가 필요 없는 경우)
                requestCameraPermissionLauncher.launch(permission)
            }
        }
    }




    // 권한 체크 및 갤러리 실행
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

    // 카메라 실행
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            if (photoFile != null) {
                capturedImageUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    photoFile
                )
                capturedImageUri?.let { uri ->
                    takePictureLauncher.launch(uri)
                } ?: run {
                    Toast.makeText(this, "이미지 URI가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "임시 파일을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "카메라를 열 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "Camera error: ${e.message}", e)
        }
    }

    // 갤러리 실행
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    // 임시 이미지 파일 생성
    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = cacheDir
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error creating image file: ${e.message}", e)
            null
        }
    }

    // 동화 생성 시작
    private fun startStoryCreation(navController: androidx.navigation.NavController) {
        if (capturedImageBitmap == null) {
            Toast.makeText(this, "사진을 찍거나 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTheme == null) {
            Toast.makeText(this, "테마를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        lifecycleScope.launch {
            try {
                val bitmap = capturedImageBitmap ?: throw IllegalStateException("이미지가 없습니다")
                val theme = selectedTheme ?: throw IllegalStateException("테마가 선택되지 않았습니다")

                Log.d("MainActivity", "Starting story creation with theme: $theme")
                val storyId = storyCreationController.createStory(bitmap, theme)
                Log.d("MainActivity", "Story created successfully with ID: $storyId")

                isLoading = false

                // 생성된 동화 화면으로 네비게이션
                navController.navigate(NavRoute.GeneratedStory.createRoute(storyId))
            } catch (e: Exception) {
                isLoading = false
                Log.e("MainActivity", "Error creating story: ${e.message}", e)
                Toast.makeText(this@MainActivity, "동화 생성에 실패했습니다: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}