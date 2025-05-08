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
    
  <img src="https://github.com/user-attachments/assets/87bb410b-c94b-472d-87bf-b698e65349cd" alt="앱 스크린샷" width="150"/>

  <img src="https://github.com/user-attachments/assets/47641307-924b-4ca9-bc2b-81d46f714486" alt="앱 스크린샷" width="150"/>

  <img src="https://github.com/user-attachments/assets/21672d47-d60e-45c1-a792-4d003abea551" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/c1c5cbc9-9052-43e2-bc9f-6a2f19381173" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/3a913ae5-17e8-44e1-876f-9f12f0f5e992" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/f7da40da-3251-4067-9f66-f3baab66ce6d" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/f7d125af-5b9a-4a6c-a01d-a2fec3822cf1" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/74a068ed-aaff-4eb0-96db-c2eee40cb59c" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/b4f250f5-d5f0-46c2-81f3-2f6868549f1a" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/dfc3a378-8118-43c0-af49-e89983572ad7" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/40270ca2-d44f-47a7-844c-34ab438ea3a2" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/7dae30fe-780f-46d8-8cf0-16e206c43f3f" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/21d3abc0-db35-4f34-809b-ae92373ab577" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/32d9b810-541e-495c-8de7-d97e3b756a80" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/6fd2e9d9-e080-4e32-be97-8a61bf324657" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/2eab285f-91b7-4386-96d2-2e9d56468df1" alt="앱 스크린샷" width="150"/>
  <img src="https://github.com/user-attachments/assets/56157754-2132-420b-bbb9-8a887b893d58" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/0f33f3e5-25c4-4368-b791-b8202a2892bb" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/1381088e-7699-402a-bdb9-22262a1d2011" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/2853211d-3ccc-4c15-8935-dbec1ed9232a" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/09e962bb-33ac-4193-b23d-235f42f14285" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/17c87abf-d1e2-42fc-abc2-c8dbd547a653" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/fc5eb18c-83c1-44f3-b2e0-19b67ea3bc92" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/e743a60c-6244-486b-9d20-9dd0fcf7ef44" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/31700f0b-96db-4eb0-82ed-3e61a69b1f50" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/72f31a9c-9ae3-462c-b0dc-39c451150814" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/799c8cd7-01c1-4802-acde-ba656291087c" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/bc8c4a51-1985-4ffd-8ef4-e37f9687b2d6" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/cb690b93-57d3-4be3-81c9-16dc7a732428" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/9cf5756d-1688-4e43-b75b-fc148237833b" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/a620d3bb-972b-4efb-8283-465290338020" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/82fde689-ee1e-4f9c-a5d5-5f96653bf137" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/a682d613-08b5-4873-9f6c-18d68f63e95b" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/a0a8ec23-530f-42e4-8454-b767b67b5e31" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/5407c307-bed8-4904-a0d3-8e14477e502c" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/ba1af9fe-dc32-4640-97b4-545ca88cf4f4" alt="앱 스크린샷" width="150"/>
<img src="https://github.com/user-attachments/assets/aef55326-6b76-469d-a20f-98571d972cef" alt="앱 스크린샷" width="150"/>


  
  
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
