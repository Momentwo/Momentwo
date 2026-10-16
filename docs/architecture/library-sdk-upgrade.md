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

## AndroidX 단계 병합·전체 검증

#193을 4c3555260으로 병합했고 [병합 후 전체 CI](https://github.com/Momentwo/Momentwo/actions/runs/38858025908)가 통과했다.
PR CI의 검사 도구 31 + 제품 239 = 270건, 실패·오류·skip 0, 경계·사이클 위반 0을 확인했다.
병합 후 Debug/Release·전체 Debug Lint·Release lintVital·전체 AndroidTest APK 빌드를 완료했다.

## 네트워크·Coroutines

Retrofit와 converter-moshi를 3.0.0, OkHttp BOM을 5.5.0, Moshi를 안정판 1.15.2,
Coroutines core/test와 Serialization runtime을 각각 1.11.0으로 정렬한다.
OkHttp Android 5.5.0 AAR도 minCompileSdk 37을 요구하며 minSdk 21이므로 앞 단계의 compileSdk 37 / 앱 minSdk 29와 호환된다.
HTTP Service·DTO·payload·기본 주소·인증/계정 격리·자동 재전송 정책과 로깅 NONE은 유지한다.

OkHttp 5의 공식 테스트 artifact인 mockwebserver3으로 전환한다. 요청 target/headers/body와 불변 MockResponse,
수신 완료 후 응답 전 연결 종료 API를 사용해 기존 회원가입/계정 격리/불명확한 POST 재전송 금지 검증을 유지한다.
회원가입 직렬화 테스트는 실제 Moshi 설정의 KotlinJsonAdapterFactory를 사용하며 코드 생성 어댑터를 검증한다고 표현하지 않는다.
서버 사진 응답의 취소 정리는 Coroutines의 최신 resume(value, onCancellation) 오버로드가 전달하는 응답을 닫도록 한다.
조회 결과의 취소 보존과 resource 정리 의미는 바꾸지 않는다.

- [Retrofit 변경 기록](https://github.com/square/retrofit/blob/trunk/CHANGELOG.md)
- [OkHttp 변경 기록](https://github.com/square/okhttp/blob/master/CHANGELOG.md)
- [Coroutines 1.11.0](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.11.0)
- [Serialization 1.11.0](https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.11.0)

### 네트워크 단계 로컬 영향 범위

경계·사이클 위반 0(24/188/695). 취소/요청 제어 14건 + 회원가입 직렬화·실제 HTTP/계정 격리/불명확한 쓰기 12건을
가상 시간과 MockWebServer로 새로 실행해 26건 모두 통과했다. 실패·오류·skip 0, 35초이며 Gradle 워커 1·힙 2GB, 테스트 worker 힙 256MB를 사용했다.

app Debug·전체 Feature/Hilt 컴파일은 1분 23초(실행 72 / 재사용 408 task)에 통과했다.
이전 AndroidTest APK를 그대로 사용해 API 31 별도 앱의 계정 연결 취소·생성 초안 보존·콘텐츠 API 요청 없음 1건을 3.845초에 확인했다.
테스트 APK·전체 로컬 테스트·Release/Lint는 재빌드/반복하지 않았고 원래 설치 앱을 보존했다. 전체 검증은 원격 CI에서 확인한다.

## 네트워크 단계 병합·전체 검증

#194를 094bd89fa로 병합했고 [병합 후 전체 CI](https://github.com/Momentwo/Momentwo/actions/runs/38864797852)가 통과했다.
검사31 + 제품239 =270건, 실패·오류·skip0, 경계·사이클 위반0, Debug/Release·전체 Debug Lint·Release lintVital·전체 AndroidTest APK 빌드를 확인했다.

## Coil 3·저장소·Paging

Coil 3.6.3의 io.coil-kt.coil3 좌표와 coil3 패키지로 일괄 전환하며 DataStore 1.2.1 / Paging 3.5.3을 사용한다.
화면 구성·경로·기록 저장 형식·계정 구분과 DB schema는 유지한다.

- app의 SingletonImageLoader.Factory에서 OkHttp network fetcher와 CacheControlCacheStrategy를 등록한다.
  기존 Coil2의 respectCacheHeaders=true를 유지해 no-store 응답을 디스크에 저장하지 않는다.
  기존처럼 독립 클라이언트를 사용하며 API/계정 인증 클라이언트를 이미지 전역 로더에 연결하지 않는다.
- 직접 가져온 library-media 영속 파일과 server-media 열람 캐시는 기존 저장소가 계속 관리한다. Coil cache로 이관하거나 원본을 삭제하지 않는다.
- 내보내기 요청은 Size.ORIGINAL과 maxBitmapSize(Size.ORIGINAL), software bitmap을 사용한다.
  Coil3의 기본 4096 비트맵 제한이 갤러리 내보내기의 원본 해상도를 줄이지 않도록 한다. 화면 표시 요청은 해당 화면 크기에 맞게 처리한다.
- 실제 app singleton의 no-store 재요청/인증 미첨부, 저장한 응답의 네트워크 없는 재사용,
  실제 PhotoRepository·MediaStore를 통한 5000×3 PNG 원본 해상도 내보내기만 작은 loopback fixture로 검증한다.
  원본 앱·다른 갤러리 파일을 보존하고 UUID로 생성한 테스트 행만 정리한다.
- PhotoRepository 접근용 Hilt getter는 기존 debug 전용 LibraryTestAccess에만 추가한다. Release 공개 계약에 테스트 접근점을 추가하지 않는다.

[Coil3 전환](https://coil-kt.github.io/coil/upgrading_to_coil3/) · [네트워크/Cache-Control 설정](https://coil-kt.github.io/coil/network/)

### 이미지·저장소 로컬 영향 범위

DataStore 세션·취소4 + Friend/Photo Mediator9 =13건 새 실행, 실패·오류·skip0 (1분8초). 경계·사이클24/188/695·위반0.
별도 app Debug·AndroidTest와 전체 Feature/Hilt 컴파일은 워커1·힙2GB·in-process로 2분1초(실행442/재사용73 task)에 통과했다. DB schema 파일은 변경되지 않았다.
Debug APK의 graphics.path/datastore_shared_counter JNI8개가 ELF LOAD16KB 정렬을 가지며 zipalign의 16KB 검사도 통과했다.
이는 정적 APK 검사이며 16KB 페이지 기기 실행을 확인했다고 기록하지 않는다.

실기기의 no-store/인증 헤더·오프라인 캐시 2건과 기존 로컬 사진 설명·메모·즐겨찾기·몰입/재시작 1건은 통과했다.
계정 사진 내보내기는 기존 AccountPhotoRepository가 계정 컨텍스트를 요구하므로 테스트에 만료된 fixture 계정을 설정하고 종료 시 정리한다.
계정 없이 직접 호출한 초기 내보내기 테스트는 이 기존 전제 때문에 실패했으며, 제품 계정 정책은 변경하지 않았다.
테스트 APK만 재빌드(14초, 실행6/재사용491 task)하고 내보내기1건만 다시 실행해 0.407초에 통과했다. 5000×3 해상도·인증 미첨부·API요청0·fixture 정리를 확인했다. 기존3건은 재사용했다.

## 이미지·저장소 병합·전체 검증

#195를 318b8c3e6으로 병합했고 [전체 CI](https://github.com/Momentwo/Momentwo/actions/runs/38869129725)가 통과했다.
검사31+제품239=270건, 실패·오류·skip0, 경계·사이클0, Debug/Release·전체 Debug Lint·Release lintVital·전체 AndroidTest APK 빌드를 확인했다.

## targetSdk 35 (Android 15)

앱 defaultConfig와 라이브러리 계측 testOptions의 targetSdk를35로 맞춘다. compileSdk37/minSdk29/JVM17은 유지한다.

- MainActivity의 enableEdgeToEdge/setDecorFitsSystemWindows(false), Scaffold·상하단 바의 insets와 사진의 의도적인 전체 화면을 유지한다. edge-to-edge 예외 설정을 추가하지 않는다.
- 직접 작성한 코드에서 onBackPressed override, SequencedCollection 추가 메서드 직접 호출, 자체 TLS 검증 완화는 확인되지 않았다. AndroidX 뒤로가기와 기존 HTTP 설정을 유지한다.
- GetContent 계열·영속 파일 복사·앱이 생성한 MediaStore 행을 사용한다. 추가 사진/외부 저장소 권한을 요구하지 않는다.
- 기존 JobScheduler는 영속 작업·NETWORK_TYPE_ANY·IO에서의 처리와 중단 시 취소를 사용한다. FGS는 사용하지 않으며 Android15의 FGS 제한을 피하기 위한 서비스·권한을 추가하지 않는다. 실제 계정 동기화는 #150이다.

### 원격 SDK 검증과 로컬 검증의 역할

target-sdk PR과 targetSdk35 이상 develop 반영 후, 기존 verify job의 Debug·AndroidTest APK를 리모트 Linux/KVM job으로 전달한다.
SDK35 단계는 API35, SDK36은 API36, SDK37은 API29/35/36/37에서 실행한다. 일반 기능 PR에는 에뮬레이터 job을 추가하지 않는다.
공식 SDK 도구·안정 Google APIs x86_64 이미지를 사용한다. CLI23.0은 API37.0 AVD 메타데이터를 처리한다.
headless/2core/2GB, matrix 동시2대까지이며 에뮬레이터 job에서는 Gradle을 실행하지 않는다. 스크립트는 Linux GitHub CI 밖에서 실행을 거부한다.

새 설치 후 네트워크를 끊고 앨범·서브앨범 CRUD, 사진 설명/메모/즐겨찾기·Sheet·몰입/복원,
로그인/회원가입 탐색과 시스템 바 lifecycle을 선택 실행한다. API35 이상은 실제 시스템 가장자리 gesture로 IME를 먼저 닫고 입력을 유지한 뒤 화면을 나가는지도 확인한다.
설정만으로 오프라인을 판정하지 않고 테스트 안에서 activeNetwork가 없음을 확인한다. 이후 연결을 복구하고 loopback 이미지의 MediaStore 원본 해상도 내보내기를 확인한다.
API37에서는 재부팅 후 영속 파일·기록과 대화면/가로·글자1.3배에서 사진·키보드/뒤로가기를 짧게 추가 검증한다.
am instrument의 종료 코드0만으로 성공 처리하지 않고 선택한 테스트의 OK 건수까지 일치해야 통과한다. 로그와 단계별 결과는 별도 artifact로 보존한다.

맥에서는 이미 통과한 전체 검증을 반복하지 않는다. 단일 Gradle/워커1/힙2GB로 필요한 APK만 빌드하고 API31 실기기의 별도 앱에만 설치한다.
업데이트 전 seed → 별도 앱 프로세스 종료 → 새 APK 설치 → verify로 캐시 정리 후에도 사진 영속 파일·설명·메모·즐겨찾기가 유지되는지 확인한다.
원본 앱·다른 갤러리 파일은 보존한다. 실서버·정식 서명 업데이트/OS 복원은 #150이며, APK 빌드·정적16KB 정렬과 실제 OS 실행 결과를 구분한다.

[Android15 변경](https://developer.android.com/about/versions/15/behavior-changes-15) ·
[Android16 변경](https://developer.android.com/about/versions/16/behavior-changes-16) ·
[Android17 변경](https://developer.android.com/about/versions/17/behavior-changes-17)

### targetSdk35 로컬 결과

경계·사이클24/188/695·위반0, 별도 Debug/AndroidTest와 전체 Feature/Hilt 컴파일 통과(1분55초, 실행512/재사용3 task, 워커1·힙2GB).
API31에서는 기존 테스트 APK를 재사용했다. 이전 target34의 seed1건0.216초 → 별도 앱만 force-stop/target35 APK 교체 → verify1건0.532초 통과.
캐시 정리 후 영속 사진·설명·메모·즐겨찾기를 확인하고 fixture를 정리했다. 원본 앱의 version/min/target 및 최초 설치·최종 업데이트 시간이 동일함을 확인했다.
새 API35 실행은 원격 CI 결과로 별도 기록한다.

API35 첫 원격 실행에서 앨범 CRUD·로그인 탐색·시스템 바·실제 IME/뒤로가기4건은 통과했고, 사진1건은 Sheet 이후 몰입 전환 대기에서 실패했다.
기존 helper는 저장 후 바뀌는 메모 입력 placeholder로 Sheet가 열렸는지 판단하고 Compose idleness만 기다렸다.
IME를 명시적으로 닫고 고정 Sheet 제목이 없어질 때까지 기다린 뒤 사진을 누르도록 테스트를 수정한다. 제품 동작과 대기 시간 한도는 변경하지 않는다. 수정 결과는 후속 CI에서 확인한다.

## targetSdk35 병합·전체 검증

수정 후 #197을521ed69fd로 병합했고 [전체 CI/API35 재실행](https://github.com/Momentwo/Momentwo/actions/runs/38874970308)이 통과했다.
일반270건 실패·오류·skip0, 경계·사이클0, Debug/Release·Debug Lint/Release lintVital·전체 AndroidTest APK 빌드와 실제 API35 smoke를 확인했다.
PR의 API35 offline UI5건51.799초·내보내기1건1.206초도 통과했다. 최초 실패는 #196에 남겼고 저장소의 모든 브랜치 직접 갱신 제한을 유지하며 #197로 대체했다.

## targetSdk 36 (Android 16)

앱과 라이브러리 계측 targetSdk를36으로 올린다. compileSdk37/minSdk29/JVM17과 라이브러리·DB·제품 코드는 유지한다.

- 기존 edge-to-edge와 AndroidX BackHandler/NavController/OnBackPressedDispatcher를 사용한다. 예외 설정·legacy onBackPressed override는 없다.
- Activity에 방향·비율·크기 제한을 선언하지 않으며 대화면 resizability 예외를 추가하지 않는다. 최종 API37에서 대화면/가로·글자 확대를 실제 확인한다.
- Android16의 JobScheduler quota는 전체 앱에 적용된다. 기존 서비스는 coroutine에서 JobParameters를 보유하고 IO에서 작업하며 onStopJob에서 취소한다. 영속 outbox 복구 계약을 유지하고 쿼터 우회용 FGS·setImportantWhileForeground를 추가하지 않는다.
- 정기 ScheduledExecutorService나 앱의 별도 시스템 폰트 렌더러는 사용하지 않는다. UI 구조·입력 정책은 변경하지 않는다.

초기 SDK 정수값/문서 변경은 값·XML·diff만 자체 확인한 뒤 로컬 Gradle·실기기 설치를 반복하지 않고 원격 CI로 검증했다.
실행에서 드러난 테스트 준비 조건을 보완한 경우에만 테스트 APK 컴파일을 추가한다. 전체 검사/빌드/Lint·실제 API36 smoke는 CI에서 확인한다. API31의 직전 자료 보존 결과를 재사용하고 최종37에서 새 APK로 다시 확인한다.
실제 서버 동기화·정식 업데이트/OS 복원은 #150에 남긴다.

[Android16 대상 변경](https://developer.android.com/about/versions/16/behavior-changes-16) ·
[전체 앱 변경/JobScheduler quota](https://developer.android.com/about/versions/16/behavior-changes-all#job-scheduler)

API36 첫 원격 실행은 사진·탐색·시스템 바·실제 gesture4건이 통과했고 앨범1건은 탭 제목 직후의 추가 버튼 탐색에서 실패했다.
실패 로그에는 버튼이 unmerged tree에만 있다고 표시되었다. 탭 제목은 필수 로컬 데이터와 목적지 전환 완료를 보장하지 않으므로
실제로 활성화된 merged 추가 버튼을 기다리도록 테스트를 보완한다. 숨겨진 unmerged 노드로 클릭을 우회하지 않고 기존10초 한도를 유지한다.
새 테스트 소스 컴파일에 필요한 테스트 APK만 로컬 워커1/힙2GB로 빌드하며 제품 코드는 변경하지 않는다. 실제 결과는 후속 API36 CI에서 확인한다.

버튼 준비 대기 보완 후 테스트 APK 컴파일은1분45초(실행494/재사용3 task, 워커1·힙2GB·in-process)에 통과했다. 실기기 설치와 전체 로컬 단위 테스트/Release/Lint는 반복하지 않았다.
