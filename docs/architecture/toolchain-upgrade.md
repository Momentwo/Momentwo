# 빌드 도구 업데이트

추적: [#189 핵심 도구](https://github.com/Momentwo/Momentwo/issues/189) →
[#190 일반 라이브러리·targetSdk](https://github.com/Momentwo/Momentwo/issues/190).
두 Issue의 단계는 별도 PR로 진행한다. 선행 PR의 병합 후 전체 CI가 통과하면 최신 develop에서 다음 브랜치를 만든다.

## 준비 단계

현재 AGP 8.5.2 / Kotlin 2.0.20 / Gradle 8.7 / JDK 17에서 동작을 보존한다.

- `core:network`의 `local.properties`는 Gradle `ProviderFactory.fileContents`로 읽는다.
  Java Properties 형식과 `BASE_URL="…"`의 따옴표를 그대로 유지하며 주소를 로그에 출력하지 않는다.
  값이 없으면 구성 단계에서 실패한다. CI에서는 `https://example.invalid/`을 사용한다.
- Android TestKit fixture는 테스트 task가 카탈로그의 AGP 버전을 system property로 전달한다.
  실제 저장소와 다른 AGP 버전에서 검사가 통과하는 일을 방지한다.

## 핵심 전환 목표

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
