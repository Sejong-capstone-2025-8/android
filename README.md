# 📚 랜덤포레스트 - 맞춤형으로 읽어주는 사진 속 동화 여행

<div align="center">
  <img src="https://github.com/user-attachments/assets/1d496fa9-1b9b-46f7-bf80-c0693f9e5a9b" alt="앱 아이콘" width="150" height="150"/>
  <br>
  <p><strong>사진 한 장으로 시작하는 인공지능 동화 생성 앱</strong></p>
  <p>
    <a href="#주요-기능">주요 기능</a> •
    <a href="#기술-스택">기술 스택</a> •
    <a href="#시스템-아키텍처">아키텍처</a> •
    <a href="#스크린샷">스크린샷</a> •
    <a href="#설치-방법">설치 방법</a> •
    <a href="#실행-방법">실행 방법</a> •
    <a href="#팀원">팀원</a> •
    <a href="#라이센스">라이센스</a>
  </p>
</div>

## 📝 프로젝트 개요

**랜덤포레스트**는 사용자가 찍거나 업로드한 사진을 기반으로 AI가 동화를 생성하고, 맞춤형 음성으로 읽어주는 안드로이드 앱입니다. 다양한 테마와 음성 옵션을 제공하여 각 사용자에게 특별한 동화 경험을 선사합니다.

사진 한 장으로 시작하여, 사용자는 판타지, 사랑, SF, 공포, 코미디 등 다양한 테마를 선택할 수 있으며, AI는 사진과 테마를 분석하여 맞춤형 동화를 생성합니다. 또한 사용자는 다양한 음성 중에서 선택하거나 직접 목소리를 녹음하여 동화를 들을 수 있으며, 배경음악도 설정할 수 있습니다.

## ✨ 주요 기능

### 🖼️ 사진 기반 동화 생성
- 카메라로 직접 촬영하거나 갤러리에서 사진 업로드
- GPT-4o API를 활용한 고품질 동화 생성
- 판타지, 사랑, SF, 공포, 코미디 등 다양한 테마 선택 가능

### 🔊 맞춤형 음성 기능
- 다양한 기본 AI 음성 모델 제공 (남성, 여성 등)
- 사용자 목소리 녹음 및 커스텀 음성 모델 생성 
- 동화 테마에 적합한 음성 자동 추천 알고리즘

### 🎵 배경음악 및 효과음
- 동화 분위기에 맞는 배경음악 선택 가능
- 장르별 다양한 배경음악 카테고리 제공

### 📱 직관적인 사용자 인터페이스
- Jetpack Compose로 구현된 현대적인 UI/UX
- 동화 생성부터 재생까지 간편한 워크플로우
- 동화 진행 상태를 시각적으로 표시하는 직관적인 플레이어

### 💾 데이터 관리
- 생성된 동화 저장 및 관리 기능
- 커스텀 음성 모델 관리 시스템

## 🛠️ 기술 스택

### 프론트엔드
- **언어**: Kotlin
- **UI 프레임워크**: Jetpack Compose
- **디자인 패턴**: MVVM (Model-View-ViewModel)
- **네비게이션**: Navigation Compose

### 백엔드 (앱 내부)
- **데이터베이스**: Room Database
- **비동기 처리**: Kotlin Coroutines, Flow
- **네트워크**: OkHttp, Retrofit
- **종속성 주입**: Hilt

### 외부 API 및 서비스
- **텍스트 생성**: OpenAI GPT-4o
- **음성 합성**: ElevenLabs TTS API
- **MFCC 음성 분석**: TarsosDSP

### 개발 도구 및 기타
- **빌드 도구**: Gradle (Kotlin DSL)
- **버전 관리**: Git, GitHub
- **CI/CD**: GitHub Actions
- **테스트 프레임워크**: JUnit, Espresso

## 📐 시스템 아키텍처

```
                ┌───────────────────────────────────────────────────┐
                │                     UI Layer                       │
                │  ┌────────────┐    ┌────────────┐    ┌──────────┐ │
                │  │MainActivity│    │StoryActivity│    │VoiceActivity│
                │  └────────────┘    └────────────┘    └──────────┘ │
                └───────────────────────────────────────────────────┘
                                        ▲
                                        │
                                        ▼
                ┌───────────────────────────────────────────────────┐
                │                  Controller Layer                  │
                │             ┌─────────────────────────┐           │
                │             │StoryCreationController  │           │
                │             └─────────────────────────┘           │
                └───────────────────────────────────────────────────┘
                                        ▲
                                        │
                                        ▼
┌──────────────────────────┐   ┌───────────────────────────────────────────┐   ┌──────────────────────────┐
│      Service Layer        │   │             Repository Layer              │   │        Util Layer        │
│  ┌─────────┐ ┌─────────┐ │   │  ┌─────────────┐    ┌─────────────┐      │   │  ┌─────────────────────┐ │
│  │GPTService│ │TTSService│ │◄─►│  │FairyTaleRepo│    │ VoiceRepo   │      │◄─►│  │FileStorageManager   │ │
│  └─────────┘ └─────────┘ │   │  └─────────────┘    └─────────────┘      │   │  └─────────────────────┘ │
└──────────────────────────┘   │  ┌─────────────┐    ┌─────────────┐      │   │  ┌─────────────────────┐ │
                                │  │ ImageRepo   │    │ MusicRepo   │      │   │  │VoiceFeaturesUtil    │ │
                                │  └─────────────┘    └─────────────┘      │   │  └─────────────────────┘ │
                                └───────────────────────────────────────────┘   │  ┌─────────────────────┐ │
                                                      ▲                          │  │NetworkUtil          │ │
                                                      │                          │  └─────────────────────┘ │
                                                      ▼                          └──────────────────────────┘
                                ┌───────────────────────────────────────────┐
                                │                Data Layer                  │
                                │  ┌─────────────┐    ┌─────────────┐      │
                                │  │AppDatabase  │    │   Entities   │      │
                                │  └─────────────┘    └─────────────┘      │
                                └───────────────────────────────────────────┘
```

## 📱 스크린샷

<div align="center">
  <img src="https://github.com/user-attachments/assets/9d911862-76d7-4ab3-ab17-965f53a5a54e" alt="앱 스크린샷" width="200"/>
    
<img src="https://github.com/user-attachments/assets/71f0483c-01bb-4ba4-a637-9c815f09a5a9" width="200" alt="Screenshot_20250414_205808_Photos and videos">

<img src="https://github.com/user-attachments/assets/0362cf42-bc1c-4ed1-ae55-9531845884f3" width="200" alt="Screenshot_20250414_205839_ImageStory">

<img src="https://github.com/user-attachments/assets/0f9726e4-b826-4f89-af43-9c73f4274331" width="200" alt="Screenshot_20250414_205846_ImageStory">

<img src="https://github.com/user-attachments/assets/d1e14e06-7eab-4452-bc1b-6ec7a6e6252e" width="200" alt="Screenshot_20250414_205959_ImageStory">

<img src="https://github.com/user-attachments/assets/e74c9599-2ac7-4d49-9fff-8ec3e1ebe627" width="200" alt="Screenshot_20250414_210005_ImageStory">

<img src="https://github.com/user-attachments/assets/97054fc8-3741-4fc9-8b0b-17fea0a217ef" width="200" alt="Screenshot_20250414_210009_ImageStory">

<img src="https://github.com/user-attachments/assets/efafdfcc-704c-4b5f-9485-955b99dad491" width="200" alt="Screenshot_20250414_210013_Permission controller">

<img src="https://github.com/user-attachments/assets/46bb961a-097a-4f77-af82-ccda1cba3b54" width="200" alt="Screenshot_20250414_210021_ImageStory">

<img src="https://github.com/user-attachments/assets/900ebf17-b20c-4d44-a81a-9840140f2c32" width="200" alt="Screenshot_20250414_210043_ImageStory">

<img src="https://github.com/user-attachments/assets/854c0527-ed3c-4858-9636-17b66c5c997e" width="200" alt="Screenshot_20250414_210050_ImageStory">

<img src="https://github.com/user-attachments/assets/5ce1849f-2693-436a-b95d-f5649d4c4809" width="200" alt="Screenshot_20250414_210054_ImageStory">

<img src="https://github.com/user-attachments/assets/cc971cda-9da2-4cfb-aa4e-6e2f8ca6ce6b" width="200" alt="Screenshot_20250414_210059_ImageStory">

<img src="https://github.com/user-attachments/assets/9fad633b-8ab0-46fb-a3d5-c413db905800" width="200" alt="Screenshot_20250414_210102_ImageStory">
  
  
</div>

## 🚀 요구 사항

- Android Studio Iguana | 2023.2.1 이상
- Android 7.0 (API Level 24) 이상 디바이스 또는 에뮬레이터
- JDK 11 이상


## 🤝 팀원

본 프로젝트는 다음 팀원들에 의해 개발되었습니다:

- **정제호**
- **박찬우** 
- **이석준**
- **김선웅** 

## 📝 라이센스

이 프로젝트는 GPL 라이센스 하에 배포될 예정입니다. 


---

<div align="center">
  <p>© 2025 랜덤포레스트 팀 - 캡스톤 디자인 프로젝트</p>
</div>
