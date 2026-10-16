# 빌드 도구 업데이트

추적: [#189 핵심 도구](https://github.com/Momentwo/Momentwo/issues/189) →
[#190 일반 라이브러리·targetSdk](https://github.com/Momentwo/Momentwo/issues/190).
두 Issue의 단계는 별도 PR로 진행한다. 선행 PR의 병합 후 전체 CI가 통과하면 최신 develop에서 다음 브랜치를 만든다.

## 준비 단계

준비 PR #191에서는 AGP 8.5.2 / Kotlin 2.0.20 / Gradle 8.7 / JDK 17에서 동작을 보존했다.

- `core:network`의 `local.properties`는 Gradle `ProviderFactory.fileContents`로 읽는다.
  Java Properties 형식과 `BASE_URL="…"`의 따옴표를 그대로 유지하며 주소를 로그에 출력하지 않는다.
  값이 없으면 구성 단계에서 실패한다. CI에서는 `https://example.invalid/`을 사용한다.
- Android TestKit fixture는 테스트 task가 카탈로그의 AGP 버전을 system property로 전달한다.
  실제 저장소와 다른 AGP 버전에서 검사가 통과하는 일을 방지한다.

## 핵심 전환 설정

Kotlin·Compose Compiler·Serialization plugin 2.4.21, AGP 9.3.3, Gradle 9.7.0,
Temurin 25.0.4.1+1 LTS 실행·컴파일 toolchain, Java/Kotlin 출력 대상 17을 사용한다.
KSP 2.3.12(KSP2), Hilt 2.60.1, Room 2.8.5를 함께 검증한다.
Kotlin 공식 호환성 표에 명시된 AGP 상한은 9.3.1이므로 9.3.3 조합은 저장소의 전체 검증으로 확인한다.

AGP 내장 Kotlin과 새 공개 DSL로 전환하며 내부 API·레거시 DSL 비활성화 옵션에 의존하지 않는다.
minSdk 29 / compileSdk 34 / targetSdk 34와 DB·API·Navigation 계약을 유지한다.
일반 라이브러리와 targetSdk 상향은 #190에서 진행한다.

## 검증

기존 도구에서 준비 PR을 먼저 검증하고, 핵심 전환에서는 새 도구의 결과와 비교한다.
아래는 전체 검증 기준이며 GitHub CI에서 실행한다. 사용자 요청에 따라 로컬에서는 이미 통과한 검증을 재사용하고,
영향 범위 검사·필요한 APK 빌드·연결 실기기의 짧은 smoke만 수행한다.
로컬 Gradle은 `--no-daemon --max-workers=1`과 최대 힙 2GB로 단일 실행하며 에뮬레이터를 띄우지 않는다.
Kotlin 컴파일이 필요한 경우 `-Pkotlin.compiler.execution.strategy=in-process`로 별도 컴파일 daemon도 방지한다.

```shell
./gradlew -p build-logic :convention:test :convention:validatePlugins --rerun-tasks --no-build-cache
./gradlew checkArchitecture
./gradlew :core:model:test testDebugUnitTest --rerun-tasks --no-build-cache
./gradlew :app:assembleDebug :app:assembleRelease lintDebug :app:lintVitalRelease assembleDebugAndroidTest
```

검사 결과의 모듈·맥락·의존 간선을 확인하고, 단위 테스트 누락이나 생성 코드 누락을 성공으로 취급하지 않는다.
기기는 임시 applicationId `cord.eoeo.momentwo.migrationcheck`로 확인한다.
정식 앱은 삭제하거나 덮어쓰지 않으며 실서버 확인은 #150에서 추적한다.

### 준비 단계 로컬 결과 (2026-10-16)

- Convention JUnit/TestKit 31건과 `validatePlugins`, 제품 단위 테스트 239건(모델 1 + Android 238), 실패·오류·skip 0.
- 경계·사이클 검사: 모듈 24개 / 맥락 234개 / 간선 1116개 / 위반 0.
- Debug/Release 및 app·network Debug Lint 검증 결과를 재사용했다.
- 사용자 자원 제한 적용 후 app Debug·AndroidTest APK만 별도 패키지로 빌드: 14초, 실행 task 12개 / 재사용 499개.
- 연결 API 31 실기기의 최초 진입·계정 연결 취소·로그인/회원가입/뒤로가기 3건 통과(6.187초).
  기기 앨범·초안 보존과 콘텐츠 API 요청 없음, 원본 앱 보존을 확인했다.
- PR과 병합 커밋의 전체 검증은 GitHub CI에서 확인한다. APK 빌드를 기기 실행 결과로 대체하지 않는다.

### 핵심 전환 로컬 결과 (2026-10-16)

- Kotlin 2.4.21 / Temurin Java compiler 25 / JVM target 17을 core:model·core:data·app의 실제 task에서 확인했다. 모델과 Convention class major는 61이다.
- Gradle 배포 SHA-256은 `84fbba45c7f4c64abc77460e1c00f541e9f960e3c7ed2538f1ede19eacd873ae`, Wrapper JAR은 공식 값 `7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d`와 일치했다.
- Convention 코드·테스트 컴파일 및 validatePlugins 통과. 변경한 AGP 맥락 fixture 1건만 실행해 통과했다. 전체 검사 도구 테스트는 CI에서 실행한다.
- 모델 1건 + 회원가입 Repository/HTTP 계약 15건 통과, 실패·오류·skip 0. Hilt/Room 생성 코드가 컴파일됐고 DB 스키마 파일은 변경되지 않았다.
- 실제 그래프는 모듈 24개 / 맥락 188개 / 간선 695개 / 위반 0. AGP 9 기본값에 따른 Release 단위 테스트 구성 차이는 모듈 경계 가이드에 기록했다.
- 별도 앱·테스트 APK 빌드: 힙 2GB / 워커 1 / Kotlin in-process, 1분 52초. core:ui가 공개하는 WindowInsetsControllerCompat에 기존 AndroidX Core 버전의 api 의존을 명시했다.
- API 31 실기기에서 사진 기록·즐겨찾기·몰입/재시작 복원, 계정 연결 취소, 로그인/회원가입/뒤로가기 3건 통과.
- 앨범 smoke는 폼 입력과 카드의 제목을 혼동하지 않도록 전환 완료·클릭 대상·고유 fixture 이름을 지정했다. 테스트 APK만 다시 빌드(13초 / 실행 6·재사용 491 task)하고 앨범 생성·서브앨범·수정·재진입·삭제 1건을 재실행해 5.545초에 통과했다. 제품 UI 흐름은 변경하지 않았다.
- 원본 앱과 기기 원본 파일을 보존했다. 전체 테스트·Lint·Release·전체 계측 APK는 원격 CI에서 확인한다.

### 핵심 전환 전체 CI 완료

준비 #191과 핵심 #192를 순서대로 병합했다. #192 병합 커밋 87451ee81의
[전체 CI](https://github.com/Momentwo/Momentwo/actions/runs/38854661780)가 통과했다.
검사 도구 31 + 제품 단위 239 = 270건, 실패·오류·skip 0이며 Debug/Release·전체 Debug Lint·Release lintVital·전체 AndroidTest APK 빌드를 확인했다.
#189는 종료했으며 후속 버전·SDK 변경은 [라이브러리 업데이트 기록](library-sdk-upgrade.md)에서 이어간다.
