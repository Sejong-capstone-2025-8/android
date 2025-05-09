package com.toprunner.imagestory

import com.toprunner.imagestory.LoginScreen
import com.toprunner.imagestory.RegisterScreen
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.toprunner.imagestory.controller.StoryCreationController
import com.toprunner.imagestory.model.VoiceFeatures
import com.toprunner.imagestory.navigation.NavRoute
import com.toprunner.imagestory.repository.FairyTaleRepository
import com.toprunner.imagestory.screens.*
import com.toprunner.imagestory.ui.components.BottomNavBar
import com.toprunner.imagestory.ui.theme.ImageStoryTheme
import com.toprunner.imagestory.util.AudioAnalyzer
import com.toprunner.imagestory.util.ImageUtil
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // 구글 로그인 요청 코드

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
                Toast.makeText(this, "이미지 캡처 처리 중 오류 발생.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedImageUri = it
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                }

                capturedImageBitmap = bitmap

            } catch (e: Exception) {
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "갤러리 이미지 처리 오류: ${e.message}", e)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 안전한 라이프사이클 내 초기화
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                initializeDefaultData()
            }
        }

        setContent {
            ImageStoryTheme {
                val navController = rememberNavController()
                val generatedStoryViewModel: GeneratedStoryViewModel = viewModel()


                Scaffold(
                    bottomBar = {
                        // 로그인 화면과 회원가입 화면에서는 BottomNavBar를 숨기기
                        if (navController.currentBackStackEntryAsState().value?.destination?.route !in listOf(NavRoute.Login.route, NavRoute.Register.route)) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    // innerPadding: Scaffold에서 내려주는 기본 패딩
                    // navigationBarsPadding(): 시스템 내비게이션 바 공간만큼 추가 패딩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                        //.navigationBarsPadding()
                    ){
                        NavHost(
                        navController = navController,
                        startDestination = NavRoute.Login.route,
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
                        // 로그인 화면
                        composable(NavRoute.Login.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(NavRoute.Home.route) {
                                        popUpTo(NavRoute.Login.route) { inclusive = true }
                                    }
                                },
                                onRegisterClick = {
                                    navController.navigate(NavRoute.Register.route)
                                },
                                onGoogleLoginClick = {
                                    navController.navigate(NavRoute.Home.route) {
                                        popUpTo(NavRoute.Login.route) { inclusive = true }
                                    }} // 구글 로그인 버튼 클릭 시 호출
                            )
                        }

                        // 회원가입 화면
                        composable(NavRoute.Register.route) {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.popBackStack() // 로그인 화면으로 돌아감
                                }
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
                                    // 녹음하기 버튼 누르면 VoiceRecording으로 이동
                                    navController.navigate(NavRoute.VoiceRecording.route)
                                }
                            )
                        }

                            composable(
                                route = "generated_story_screen/{storyId}",                  // 🔥 경로 정의
                                arguments = listOf(navArgument("storyId") {
                                    type = NavType.LongType
                                })
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getLong("storyId") ?: 0L
                                val context = LocalContext.current  // LocalContext.current를 사용하여 context를 얻어옵니다.
                                val fairyTaleRepository = FairyTaleRepository(context)
                                GeneratedStoryScreen(
                                    storyId = storyId,
                                    navController = navController,
                                    generatedStoryViewModel = generatedStoryViewModel,// 🔥 ViewModel 공유
                                    fairyTaleRepository = fairyTaleRepository  // repository 전달

                                )
                            }
                            // 음악 리스트 화면
//                        composable(NavRoute.MusicList.route) {
//                            MusicListScreen(
//                                navController = navController,
//                                viewModel = generatedStoryViewModel,                     // 🔥 ViewModel 주입
//                                onNavigateToStory = { storyId ->                          // 🔥 이동 콜백
//                                    navController.navigate(NavRoute.GeneratedStory.createRoute(storyId))
//                                }
//                            )
//                        }

                            composable(NavRoute.MusicManager.route) {
                                MusicManagerScreen() // ✅ 음악 관리 화면
                            }
                            composable(
                                route = "music_list/{storyId}",
                                arguments = listOf(navArgument("storyId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getLong("storyId") ?: 0L

                                MusicListScreen(
                                    navController = navController,
                                    viewModel = generatedStoryViewModel,
                                    storyId = storyId,  // 🔥 전달
                                    onNavigateToStory = { id ->
                                        navController.navigate(NavRoute.GeneratedStory.createRoute(id))
                                    }
                                )
                            }



                            // 설정 화면
                        composable(NavRoute.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                onLogoutClicked = {
                                    // Firebase 로그아웃 처리
                                    val firebaseAuth = FirebaseAuth.getInstance()
                                    firebaseAuth.signOut()

                                    // 구글 로그아웃 처리
                                    val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        Toast.makeText(this@MainActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                                        // Firebase 로그아웃도 함께 처리
                                        firebaseAuth.signOut()

                                        // 로그인 화면으로 이동
                                        navController.navigate(NavRoute.Login.route) {
                                            popUpTo(NavRoute.Home.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                            //계정 관리 화면
                            composable(NavRoute.ManageAccount.route) {
                                ManageAccountScreen(navController = navController)
                            }
                            //계정 정보 수정 화면
                            composable(NavRoute.EditAccount.route) {
                                EditAccountScreen(navController = navController)
                            }



                        // 생성된 동화 화면
                            composable(
                                route = NavRoute.GeneratedStory.route,
                                arguments = listOf(
                                    navArgument("storyId") { type = NavType.LongType },
                                    navArgument("bgmPath") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getLong("storyId") ?: -1
                                val bgmPath = backStackEntry.arguments?.getString("bgmPath")
                                val context = LocalContext.current  // LocalContext.current를 사용하여 context를 얻어옵니다.
                                val fairyTaleRepository = FairyTaleRepository(context)
                                if (storyId != -1L) {
                                    GeneratedStoryScreen(
                                        storyId = storyId,
                                        bgmPath = bgmPath,
                                        navController = navController,
                                        generatedStoryViewModel = generatedStoryViewModel,
                                        fairyTaleRepository = fairyTaleRepository  // repository 전달
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
    // MainActivity.kt의 startStoryCreation 메서드 수정
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

        // 이미지 최적화를 먼저 수행
        val imageUtil = ImageUtil()
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                // 이미지 최적화 먼저 수행
                val bitmap = capturedImageBitmap ?: throw IllegalStateException("이미지가 없습니다")
                val optimizedBitmap = imageUtil.compressImage(bitmap)
                val theme = selectedTheme ?: throw IllegalStateException("테마가 선택되지 않았습니다")

                // 본격적인 동화 생성 작업
                withContext(Dispatchers.IO) {
                    Log.d("MainActivity", "Starting story creation with theme: $theme")
                    val storyId = storyCreationController.createStory(optimizedBitmap, theme)
                    Log.d("MainActivity", "Story created successfully with ID: $storyId")

                    // UI 작업은 Main 스레드에서 수행
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        // 생성된 동화 화면으로 네비게이션
                        navController.navigate(NavRoute.GeneratedStory.createRoute(storyId))
                    }
                }
            } catch (e: Exception) {
                // UI 작업은 Main 스레드에서 수행
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Log.e("MainActivity", "Error creating story: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "동화 생성에 실패했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun testVoiceAnalyzer() {
        lifecycleScope.launch {
            try {
                // 1. 테스트용 샘플 오디오 파일 찾기 (앱 애셋에서 로드 또는 녹음된 파일 사용)
                val audioDir = File(filesDir, "audio_files")
                val audioFiles = audioDir.listFiles { file -> file.name.endsWith(".wav") || file.name.endsWith(".mp3") || file.name.endsWith(".3gp") }

                if (audioFiles.isNullOrEmpty()) {
                    Log.e("TEST", "테스트할 오디오 파일이 없습니다.")
                    Toast.makeText(this@MainActivity, "테스트할 오디오 파일이 없습니다. 먼저 음성을 녹음하세요.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 가장 최근 파일 사용
                val testFile = audioFiles.maxByOrNull { it.lastModified() }
                Log.d("TEST", "테스트 파일: ${testFile?.absolutePath}")
                Toast.makeText(this@MainActivity, "테스트 파일: ${testFile?.name}", Toast.LENGTH_SHORT).show()

                // 2. AudioAnalyzer 실행
                val analyzer = AudioAnalyzer(this@MainActivity)
                withContext(Dispatchers.IO) {
                    Log.d("TEST", "음성 분석 시작...")
                    val result = analyzer.analyzeAudioFile(testFile!!.absolutePath)

                    // 3. 결과 로그 출력
                    Log.d("TEST", "분석 결과: pitchAvg=${result.averagePitch}, stdDev=${result.pitchStdDev}")
                    Log.d("TEST", "MFCC 값: ${result.mfccValues.size} 프레임, 첫 프레임: ${result.mfccValues.firstOrNull()?.contentToString()}")

                    // 4. UI에 토스트 메시지 표시
                    withContext(Dispatchers.Main) {
                        val resultText = "분석 결과: 평균 피치=${result.averagePitch.toInt()}Hz, " +
                                "변동성=${result.pitchStdDev.toInt()}Hz, " +
                                "MFCC 프레임 수=${result.mfccValues.size}"
                        Toast.makeText(this@MainActivity, resultText, Toast.LENGTH_LONG).show()

                        // 5. 선택적으로 분석 결과를 담은 액티비티나 다이얼로그 표시 가능
                        // 예: showVoiceAnalysisDialog(result)
                    }
                }
            } catch (e: Exception) {
                Log.e("TEST", "음성 분석 테스트 실패: ${e.message}", e)
                Toast.makeText(this@MainActivity, "음성 분석 테스트 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 분석 결과를 보여주는 다이얼로그
    private fun showVoiceAnalysisDialog(voiceFeatures: VoiceFeatures) {
        // 컴포즈 다이얼로그로 구현 가능
        // 여기서는 코드 예시만 제공:
        /*
        val dialogController = DialogController()
        dialogController.showDialog {
            Box(modifier = Modifier.padding(16.dp)) {
                ImprovedVoiceFeatureVisualization(voiceFeatures)
            }
        }
        */
    }

}