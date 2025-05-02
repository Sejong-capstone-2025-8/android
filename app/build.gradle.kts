import java.util.Properties
import java.io.FileInputStream

plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
FileInputStream(localPropertiesFile).use { fis ->
    localProperties.load(fis)
}

android {
    namespace = "com.toprunner.imagestory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.toprunner.imagestory"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ELEVENLABS_API_KEY", "\"${localProperties.getProperty("elevenlabs_api_key")}\"")
        buildConfigField("String", "GPT_API_KEY", "\"${localProperties.getProperty("gpt_api_key")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    //tarsosDSP
//    implementation("be.tarsos.dsp:core:2.5")
//    implementation("be.tarsos.dsp:jvm:2.5")
    implementation ("com.github.st-h:TarsosDSP:2.4.1")

    implementation("androidx.appcompat:appcompat:1.7.0") // Example
    implementation("com.google.android.material:material:1.12.0")

    implementation(libs.jetbrains.kotlin.stdlib.jdk8)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0") // 버전 다운그레이드

    implementation(libs.gson) // 최신 버전의 Gson
    // 구글 로그인
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    //firebase 라이브러리
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Room 라이브러리
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.media3.common.ktx)
    kapt("androidx.room:room-compiler:2.7.0")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")



    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation ("androidx.navigation:navigation-compose:2.8.9")

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.activity:activity-ktx:1.10.1")

    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.material:material:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")


    // 테스트 의존성
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation ("androidx.room:room-runtime:2.7.0")
    kapt ("androidx.room:room-compiler:2.7.0")
    implementation ("androidx.room:room-ktx:2.7.0")

    implementation ("androidx.exifinterface:exifinterface:1.3.7")
}