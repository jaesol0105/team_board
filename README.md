# TEAMBOARD(팀보드)

<img src="https://github.com/user-attachments/assets/930c8110-a416-4092-9a87-fdedbddd0515" width="10%"/>

팀 프로젝트 관리 / 일정 공유 <br/>
협업 서비스 안드로이드 애플리케이션
 
## Development

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-8.2.2-green.svg)](https://gradle.org/)
[![Android Studio](https://img.shields.io/badge/Android%20Studio-2024.2.1%20%28Ladybug%29-green)](https://developer.android.com/studio)
[![minSdkVersion](https://img.shields.io/badge/minSdkVersion-21-red)](https://developer.android.com/distribute/best-practices/develop/target-sdk)
[![targetSdkVersion](https://img.shields.io/badge/targetSdkVersion-34-orange)](https://developer.android.com/distribute/best-practices/develop/target-sdk)

### Language
* Kotlin 

### Architecture
* MVVM Based

### Libraries

* Android
  * Lifecycle & ViewModel, LiveData
  * DataBinding
  * Navigation
  * Room

* Kotlin Libraries
  * Kotlin Coroutines
  * StateFlow

* Firebase
  * Firebase Auth
  * Cloud Firestore
  * Firebase Cloud Messaging

* Image Loading
  * Glide

* Compose
  * Material Design

## Features
* 개요
  * 프로젝트 별로 일정을 관리/공유 할 수 있는 협업 애플리케이션
  * 사용자는 여러 프로젝트를 생성할 수 있고, 멤버를 초대 할 수 있음
  * 큰 작업 단위인 Task, 개별 작업 단위인 Card를 통해 체계적인 일정 관리

* 주요 기능
  * 이메일을 통한 프로젝트 멤버 초대
  * 프로젝트 초대 앱 푸시 알림
  * 북마크 추가 및 해제 기능
  * 작업 별 진행률 표시 라벨 설정
  * 작업 별 멤버 역할 배정
  * 작업 별 마감일 설정
  * 프로필 편집
    
## Developing
* MVVM 아키텍쳐
* Coroutine + Flow 비동기 처리 구조
* Kakao SDK + Firebase OIDC 카카오 SSO 로그인 구현
* 유저/보드/태스크/카드 데이터에 대한 CRUD (Firebase)
  * 이미지 압축 및 업로드
* Firebase Device Token 기반 앱 푸시 알림 구현
* UI/UX
  * Jetpack Navigation을 이용한 화면 이동 구현
  * BottomSheetDialog를 활용한 DateTimePicker 등의 UI 컴포넌트 적용
  * ItemTouchHelper 클래스를 활용한 RecyclerView 아이템 드래그 위치 변경 기능 구현
* UI 코드 품질 개선
  * Sealed Class 기반의 UIState를 정의해 로딩·성공·실패 등 UI 상태를 일관되게 관리
  * DataBinding + ViewbindingAdapter를 통한 UI 로직 간소화
  * Extension 함수를 통한 공통 로직 유틸화

## Foldering
```
├── app
│   ├── data
│   │   ├── model
│   │   ├── repository
│   │   └── source
│   │       ├── local
│   │       └── remote
│   ├── service
│   ├── utils
│   └── presentation
│       ├── base
│       ├── boardlist
│       ├── bookmark
│       ├── carddetail
│       ├── common
│       │   ├── bindingadapters
│       │   └── extensions
│       ├── createboard
│       ├── factory
│       ├── login
│       ├── main
│       ├── member
│       ├── myprofile
│       ├── notification
│       ├── shared
│       ├── state
│       └── tasklist
└── gradle
```

## Demo
<img src="https://github.com/user-attachments/assets/d995607c-5f67-4b38-abd6-189832c5c5b2" width="24%"/>
<img src="https://github.com/user-attachments/assets/b54935dc-3149-4d7c-ab2a-2862003337e1" width="24%"/>
<img src="https://github.com/user-attachments/assets/275f0e7d-8fd7-4d59-bfa2-f912722daea2" width="24%"/>
<img src="https://github.com/user-attachments/assets/420af884-7e6e-43c5-a1d6-d150340204cc" width="24%"/>
