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

## Refactoring
* BottomSheetCustomDialog
* ViewbindinAdapter 를 통한 코드 간소화
* 확장함수를 통한 반복 로직 모듈화
* MVVM 패턴, 계층 분리와 관심사 분리

* LiveData -> StateFlow
  * (NPE 방지 및 생명주기 종속성 제거)
    
* 콜백 중심의 비동기 처리 -> 코루틴과 StateFlow-collect 구조
  * (모듈간 결합도를 낮추고 코드의 가독성 향상)

## Demo

<img src="https://github.com/user-attachments/assets/19955ed5-cbf5-4d9d-a7b4-3c1f3705c5ef" width="25%"/>
<img src="https://github.com/user-attachments/assets/8b8cd0d9-563e-40f6-adfc-597f98b092b7" width="25%"/>
<img src="https://github.com/user-attachments/assets/d0fd407e-6fee-43dc-aeac-ee3677e71008" width="25%"/>
<img src="https://github.com/user-attachments/assets/a692104e-2c99-40f6-9764-83e40a7e43f4" width="25%"/>
<img src="https://github.com/user-attachments/assets/275f0e7d-8fd7-4d59-bfa2-f912722daea2" width="25%"/>
<img src="https://github.com/user-attachments/assets/b84a31ff-28c2-4eec-84b8-a6b36338592f" width="25%"/>

