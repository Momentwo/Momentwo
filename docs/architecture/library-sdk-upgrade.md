# 일반 라이브러리·targetSdk 업데이트

추적: [#190](https://github.com/Momentwo/Momentwo/issues/190). 선행 핵심 도구는 #189 / PR #192에서 갱신했다.
각 PR은 최신 develop에서 시작하며 선행 병합의 전체 CI 통과 후 다음 단계로 진행한다.

## 안정판 고정 (2026-10-16)

Google Maven·Maven Central의 버전 목록에서 preview/alpha/beta/RC를 제외하고 안정판을 확인했다.
버전 숫자만 최신으로 바꾸지 않고 AAR metadata·minSdk·기존 API와 실제 빌드 결과를 확인한다.

| 묶음 | 기존 직접 선언 | 목표 |
|---|---|---|
| AndroidX Core | 1.13.1 | 1.19.1 |
| Activity Compose | 1.9.3 | 1.13.0 |
| Compose BOM | 2024.10.01 | 2026.09.00 |
| Material 3 / UI | 이전 BOM 관리 | 1.4.0 / 1.12.1 (새 BOM 관리) |
| Material icons | 별도 1.7.5 | BOM 관리 1.7.8 |
| Lifecycle | 2.8.7 | 2.11.0 |
| Navigation Compose | 2.8.3 | 2.10.2 |
| AndroidX Hilt Compose | 1.2.0 | 1.4.0 |
| AndroidX Test JUnit / Runner / Espresso | 1.2.1 / 1.6.1 / 3.6.1 | 1.3.0 / 1.7.0 / 3.7.0 |
| Coroutines | 1.7.3 | 1.11.0 |
| Serialization runtime | 1.7.3 | 1.11.0 |
| Retrofit / Moshi converter | 2.11.0 | 3.0.0 |
| OkHttp BOM | 4.12.0 | 5.5.0 |
| Moshi | 1.15.1 | 1.15.2 |
| Coil | 2.7.0 | 3.6.3 |
| DataStore | 1.1.1 | 1.2.1 |
| Paging | 3.3.2 | 3.5.3 |

JUnit 4.13.2와 선행 작업의 Hilt 2.60.1 / Room 2.8.5는 그대로 사용한다.
Kotlin 플러그인 2.4.21과 Serialization runtime의 버전은 별개다.

## AndroidX·Compose

Core 1.19.1, Navigation 2.10.2, Hilt Compose 1.4.0, Compose UI 1.12.1의 배포 AAR metadata가
minCompileSdk 37 및 최소 AGP 9.1.0을 요구한다. 이들 Android manifest의 minSdk는 23~24로 앱 minSdk 29와 호환된다.
따라서 compileSdk를 37로 먼저 올리되 targetSdk는 34로 유지한다. targetSdk 동작 변경은 뒤의 별도 PR에서 수행한다.
CI는 제품 SDK 37.0과 architecture TestKit fixture용 SDK 34를 함께 설치한다.

hiltViewModel은 새 hilt-lifecycle-viewmodel-compose artifact와 androidx.hilt.lifecycle.viewmodel.compose 패키지를 사용한다.
BOM이 제공하는 Material icons 1.7.8을 사용하고 중복된 별도 버전은 제거한다.
경로 직렬화, DB schema, 디자인, 입력/권한/저장 정책은 유지한다.

## 검증 자원 기준

맥에서는 에뮬레이터를 실행하지 않는다. 단일 Gradle 실행 / 워커 1 / 최대 힙 2GB / Kotlin in-process로
영향 범위 검사와 필요한 Debug·테스트 APK만 빌드하고, API 31 연결 실기기에서 짧은 smoke를 실행한다.
원래 설치 앱은 보존하고 임시 applicationId cord.eoeo.momentwo.migrationcheck를 사용한다.
전체 단위 테스트·Lint·Release·전체 계측 APK는 GitHub CI에서 검증한다. APK 빌드는 기기 실행 통과로 기록하지 않는다.
실서버 검증은 #150에 남긴다. 실행 환경이 없는 OS 버전은 미확인으로 기록한다.

## 근거

- [Compose BOM 매핑](https://developer.android.com/develop/ui/compose/bom/bom-mapping)
- [AndroidX Hilt 릴리스](https://developer.android.com/jetpack/androidx/releases/hilt)
- [Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk)
- [Google Maven](https://dl.google.com/dl/android/maven2/index.html) / [Maven Central](https://repo.maven.apache.org/maven2/)

## AndroidX 단계 로컬 결과 (2026-10-16)

- 경계·사이클: 24모듈 / 188맥락 / 695간선 / 위반 0.
- app Debug·AndroidTest APK 및 전체 Feature/Hilt 컴파일 통과. 워커 1 / 힙 2GB / Kotlin in-process, 5분 20초. 전체 로컬 테스트·Release/Lint는 반복하지 않았다.
- API 31 별도 앱의 사진 설명·메모·즐겨찾기·Sheet·몰입 모드·Activity 재시작 복원과 시스템 바 pause/dispose/resume 복원이 통과했다. 콘텐츠 API 요청이 발생하지 않았다.
- 로그인 smoke의 계정 화면은 저장 공간·세션 조회 후 본문을 표시하므로 Compose idleness만으로 준비를 판단하지 않는다. 비동기 본문 전환을 기다리지 않은 1회 실패를 확인했고, 명시적인 버튼 준비 대기를 보완했다. 제품 업무 로직은 변경하지 않았다.
- 대기 보완 후 테스트 APK만 재빌드(31초, 실행 6/재사용 491 task)하고 로그인·시스템 바 2건만 다시 실행해 4.368초에 통과했다. 사진 통과 결과는 재사용했다.
- PR 및 병합 후 전체 CI 결과는 별도로 확인한다.
