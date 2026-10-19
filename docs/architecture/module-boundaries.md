# 모듈 경계·사이클 검사 (#158)

## 실행

Temurin JDK 25와 Android SDK 37(테스트 fixture는 34), 기존 local.properties 설정을 사용한다. 앱 컴파일이나 콘텐츠 API 호출 없이 구성된 프로젝트 의존을 검사한다. 첫 실행의 Gradle/플러그인 다운로드와 SDK 설정은 필요하다.

```bash
./gradlew checkArchitecture
./gradlew checkModuleBoundaries
./gradlew checkModuleCycles
ANDROID_HOME=/path/to/android-sdk ./gradlew -p build-logic :convention:check
```

루트와 모든 모듈의 check는 checkArchitecture를 실행한다. 세 task는 앱 컴파일에 의존하지 않는다. checkArchitecture는 경계·소스·사이클 결과를 모두 모아 실패하므로 첫 경계 오류 때문에 사이클 진단이 생략되지 않는다. 개별 task는 해당 검사만 수행한다.

결과는 build/reports/module-architecture의 architecture.json/txt, boundaries.json/txt, cycles.json/txt에 task별로 기록한다. 위반이 있으면 모듈·configuration·variant·소스 종류·용도·파일/행·순환 경로와 수정 방향을 표시한다. 목록과 순환 그룹의 순서는 일정하다. 실패한 실행에서도 보고서를 남긴다.

## 허용·금지 규칙

| 출발 | 규칙 |
|---|---|
| Core | Core 의존 허용, Feature와 app 의존 금지 |
| Feature impl | Core·자신/다른 Feature API 허용, app·다른 Feature impl 금지 |
| Feature API | app·Feature impl·core:data 금지. 운영 그래프에서 간접적으로 impl/data에 도달하는 경로도 금지 |
| app 운영 | Feature API/impl 및 core:common·model·designsystem으로 조립 |
| 테스트 | 기존 Core 테스트·app 통합 테스트 의존 허용. Core→Feature, Feature→app, 타 Feature impl 금지는 유지 |
| core:testing | 테스트 configuration·테스트 소스에서만 사용 |

Core 내부의 새로운 계층 정책이나 현재 모든 의존의 허용 목록을 추가하지 않는다. 자신의 API와 다른 Feature API 연결은 허용되며 실제 순환 여부는 별도로 검사한다. DTO 공개 범위와 MVI 규칙은 이 검사 대상이 아니다.

모듈 역할은 :app, :core:<name>, :feature:<name>:api/impl로 분류한다. settings에 등록된 실제 모듈을 수집하고 모듈 수를 고정하지 않는다. 디렉터리 그룹 프로젝트는 제외하며, 새로운 역할을 사용하는 모듈은 정책을 명시적으로 추가해야 한다. build-logic은 메인 모듈 그래프에서 제외되는 included build이며 자체 테스트로 검증한다.

## Gradle 모델과 사이클

momentwo.module-architecture는 설정 완료 시 실제 ProjectDependency와 configuration.hierarchy를 스냅샷으로 만든다. Convention Plugin, apply된 스크립트, afterEvaluate에서 추가된 의존과 상속된 선언을 포함하며 build.gradle 문자열을 파싱하지 않는다. task 실행 중 Project/Configuration 객체를 조회하거나 classpath를 resolve하지 않는다. 전체 모듈 구성이 필요한 검사이므로 --configure-on-demand는 명시적으로 거부해 불완전한 그래프가 통과하지 않게 한다.

노드는 프로젝트 + variant + MAIN/UNIT_TEST/ANDROID_TEST/TEST_FIXTURES + COMPILE/RUNTIME이다. compileOnly와 runtimeOnly를 무조건 합치지 않는다. 각 테스트 노드는 자신의 운영 출력과 해당 configuration에서 선택한 대상 출력에 연결한다.

- A 운영 → B 운영, B 테스트 → A 운영: 정상이다. B 운영이 A 운영에 의존하는 관계로 바꾸지 않는다.
- A Debug → B Debug, B Release → A Release: 서로 다른 variant이므로 순환이 아니다.
- A 운영 → B 운영 → A 운영: 해당 맥락의 사이클로 실패한다.
- 자기 자신을 참조한 운영 의존은 실패한다. 테스트 → 자신의 운영 출력은 정상이다.
- 테스트 fixture 출력은 별도 노드다. 명시적인 ApiElements/RuntimeElements와 test-fixtures capability를 처리하고 fixture 간 실제 순환도 검출한다.

variant-aware 의존은 동일 variant, JVM main 출력, 동일 BuildType의 유일한 대상 순으로 판정한다. 모호한 flavor/fallback을 추측하지 않는다. 명시적 BuildType 요청과 표준 출력 configuration을 처리한다. 지원하지 않는 targetConfiguration·capability·요청 attribute·classifier, 지원되는 compile/runtime 맥락에 연결되지 않은 프로젝트 의존은 UNSUPPORTED_CONTEXT로 실패한다. 필요하면 Collector의 선택 규칙과 TestKit fixture를 함께 추가하며 예외를 조용히 무시하지 않는다.

강결합요소마다 전체 멤버와 대표 순환 경로 하나를 보고한다. 가능한 모든 단순 순환을 열거하지 않는다. 외부 라이브러리 간 사이클, Gradle task 자체의 임의 dependsOn 순환, Convention Plugin의 apply 호출 순환은 모듈 의존 사이클과 별도 범위다.

## 소스 검사와 한계

Android의 public DSL sourceSets(java/kotlin)와 JVM SourceSetContainer에 등록된 직접 작성 소스를 검사한다. Android namespace와 실제 package 선언으로 소유권을 구분하므로 album-detail/albumdetail처럼 디렉터리와 패키지가 달라도 처리한다. AGP의 public getter는 플러그인/TestKit classloader가 다른 경우에도 읽도록 분리한다.

별칭·와일드카드·Java static import를 검사하고 주석·문자열·중첩 주석·Kotlin raw string은 제외한다. 줄 번호를 보존하며 @SerialName의 기존 경로 문자열은 바꾸지 않는다. build/generated/.gradle 디렉터리와 소스 외 파일은 제외한다. 서로 다른 모듈이 같은 import package를 소유해 판정할 수 없으면 명시적으로 실패한다.

모든 완전 수식 참조·공개 시그니처를 해석하는 컴파일러 플러그인은 도입하지 않는다. import 검사는 소스의 금지 참조를 보완하는 수단이며, 프로젝트 의존 그래프 검사는 실제 Gradle 선언을 별도로 검사한다.

## CI

Android verification 워크플로는 PR 대상 develop, develop push, 수동 실행을 지원한다.

| 실행 | 검사 |
|---|---|
| PR | 검사 도구 JUnit/TestKit → checkArchitecture → Model JVM/전체 Debug 단위 테스트 → app Debug → 전체 Debug Lint |
| develop push·수동 | PR 검사 + app Release/lintVitalRelease + 전체 Debug AndroidTest APK 빌드 |

Ubuntu 24.04·Temurin 25.0.4.1+1·SDK 37.0(제품)·34(TestKit fixture)·build-tools 36.0.0·command-line tools 16.0과 Gradle 9.7.0을 사용한다. Action은 확인한 release의 커밋 SHA로 고정한다. CI는 local.properties에 SDK 경로와 BASE_URL="https://example.invalid/"만 생성하며 운영 인증 정보·릴리스 서명을 요구하지 않는다. 이 주소로 만든 산출물은 배포용이 아니다.

일반 pull_request 이벤트와 contents:read, checkout persist-credentials:false를 사용한다. 동일 PR의 이전 실행은 취소한다. Gradle User Home 의존 캐시만 재사용하고 단위 테스트와 검사 도구 테스트는 --rerun-tasks --no-build-cache로 실행한다. 실패해도 검사·단위 테스트·Lint 보고서를 14일 보관한다. SDK/외부 라이브러리를 다운로드할 네트워크는 필요하지만 실제 백엔드는 필요하지 않다.

일반 PR에서는 기기·에뮬레이터 테스트를 실행하지 않는다. target-sdk PR과 targetSdk35 이상 develop 반영 후에는 별도 Linux/KVM job에서 짧은 SDK 실행 검증을 수행한다. 이 job은 기존 빌드 job의 APK만 사용하며 Gradle과 에뮬레이터를 함께 실행하지 않는다. 자세한 대상·검증 범위는 [SDK 업데이트 가이드](library-sdk-upgrade.md)를 따른다. AndroidTest APK 빌드만으로 기기 회귀 통과로 해석하지 않는다. 실제 서버·정식 업데이트/복원은 #150에 남긴다. 빈 navigation 모듈은 #160에서 제거했다. 브랜치 보호·자동 롤백 정책은 변경하지 않는다.

## 검사 도구 회귀

JUnit은 정책·강결합요소·소스 lexer/행 번호를 검증하고 TestKit은 임시 프로젝트에서 정상/위반 의존과 플러그인 연결을 검증한다. 테스트 fixture에서만 순환과 잘못된 의존을 만들며 실제 제품 모듈은 오염시키지 않는다. AGP fixture는 테스트 task가 Version Catalog에서 전달하는 버전(현재 9.3.3)·SDK 34를 사용한다. 단위/기기 테스트의 컴파일·런타임 맥락이 자신의 운영 출력으로 연결되는지도 확인한다.

운영/테스트 역방향의 오탐 방지, Debug/Release 분리, 상속·늦은 선언·Convention 의존, compileOnly/runtimeOnly, fixture 출력·미지원 맥락, 모듈 추가/삭제, check 연결, 실패 보고서, 생성 코드 제외와 새 소스 입력 무효화를 포함한다. 설정 캐시 재사용 후 새 소스 파일의 위반도 다시 검출하는지 확인한다. 프로젝트 전체의 configuration-cache 옵션을 강제로 활성화하지 않는다. CI 제품 검증은 로컬 재검증과 동일하게 --no-daemon·Gradle heap 4GiB·worker 2개를 사용하며 기존 gradle.properties의 로컬 기본값은 유지한다.

### 로컬 실행 결과 (2026-10-15)

- 현재 24개 모듈·234개 빌드 맥락·1,116개 간선에서 경계·사이클 위반 0건.
- 검사 도구 JUnit/TestKit 31건과 Model JVM 1건 + Android 단위 238건 = **270건**, 실패·오류·skip 0. 검사 도구와 제품 단위 테스트는 --rerun-tasks --no-build-cache로 재실행했다.
- validatePlugins·actionlint·git diff --check, app Debug/Release/lintVitalRelease·전체 Debug Lint·전체 Debug AndroidTest APK 빌드 통과.
- 로컬 DEX 병합 지연 후 app 산출물을 정리하고 단일 실행 프로세스/4GiB로 재검증했다. 기기 설치·기기 테스트·실제 API 성공을 확인한 결과는 아니며 기존 설치 앱은 변경하지 않았다.
- PR CI와 병합 커밋의 전체 CI 결과는 #158에 실행 링크로 기록한 뒤 종료한다.

## Gradle 9 전환 (#189)

ProjectDependency.path와 현재 빌드의 프로젝트 조회로 대상 configuration을 찾는다. 제거된 dependencyProject API를 사용하지 않으며 알 수 없는 대상/맥락은 계속 실패한다. AGP 내장 Kotlin과 새 공개 DSL의 소스 집합에서도 생성 코드는 제외하고 직접 작성한 Kotlin/Java import를 검사한다.

AGP 9는 기본적으로 tested build type(Debug)의 단위 테스트만 구성한다. 도구 전환 직후 그래프는 24개 모듈·188개 맥락·695개 간선이었으며 Debug 단위/기기 테스트와 Debug/Release 운영 출력을 모두 포함한다. 이전 234개 맥락과의 차이는 Release 단위 테스트 46개 맥락이 기본 생성되지 않기 때문이다. TestKit에서는 Release 단위 테스트를 명시적으로 켜고 해당 맥락의 수집도 검증한다. 이 수치는 정책에 하드코딩하지 않는다.


## #160 빈 모듈 제거 후 검증

core:navigation 제거 후 23개 프로젝트를 같은 경계/사이클 정책으로 검사한다. 정책이나 검사기에 모듈 수를 고정하지 않으며 기존 TestKit의 모듈 추가·삭제 회귀를 재사용한다.
앞의 24개/234개 또는188개 맥락 수치는 각 실행 시점의 기록이다. 변경 후 실제 맥락·간선 수와 검증 범위는 [모듈 의존 표](module-graph.md)에 기록한다.
