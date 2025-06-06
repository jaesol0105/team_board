# TEAMBOARD(팀보드)

<img src="https://github.com/user-attachments/assets/930c8110-a416-4092-9a87-fdedbddd0515" width="10%"/>

팀 프로젝트 관리 / 일정 공유 <br/>
안드로이드 애플리케이션
 
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
* 주요 기능
  * 여러개의 Board를 통해 프로젝트 별로 일정을 관리 할 수 있습니다.
  * 큰 작업 단위인 Task, 개별 작업 단위인 Card를 통해 체계적인 일정관리를 할 수 있습니다.

* 기능
  * 이메일을 통한 멤버 초대
  * Board 초대 앱 푸시 알림
  * 북마크 추가 및 해제 기능
  * 작업 단위(Card) 별 진행 현황 표시 라벨
  * 작업 단위(Card) 별 멤버 역할 배정
  * 작업 단위(Card) 별 마감일 설정
  * 드래그를 통한 카드 item 순서 변경
    
## Refactoring
* MVVM 패턴 설계 / 관심사 분리
* 코루틴 + Flow 비동기 처리 구조
* 확장함수를 통한 반복 로직 모듈화
* ViewbindinAdapter를 통한 UI 코드 간소화
* BottomSheetCustomDialog, Custom DateTimePicker

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
