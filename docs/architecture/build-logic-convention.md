# build-logic Convention Plugin 가이드

> Momentwo는 멀티모듈 전환의 기반 작업(Phase 0)으로 `build-logic` 모듈과 Convention Plugin 체계를 도입했다. 본 문서는 그 **구조 · 동작 · 사용법 · 이점**을 정리한 단독 가이드이며, 상위 마이그레이션 흐름은 [multi-module-migration-plan.md](./multi-module-migration-plan.md)를 참고한다.

---

## 1. 도입 배경

단일 모듈에서 멀티모듈로 전환하면 모듈 수가 1개에서 24개(`:app` + `:core:* 11` + `:feature:* 12`)로 늘어난다. 이때 각 모듈이 자신만의 `build.gradle.kts`를 갖게 되는데, 같은 설정(compileSdk, Kotlin/Java 버전, Compose 활성화, Hilt 적용, Room 옵션 등)을 모든 모듈에 그대로 복붙하면 다음 문제가 즉시 발생한다.

- Kotlin/AGP 버전을 하나 올릴 때 24개 파일을 일일이 수정해야 한다.
- 신규 모듈을 만들 때마다 같은 50~100줄을 복사한다.
- 모듈마다 미세하게 다른 옵션이 누적되어 빌드 동작이 비결정적으로 변한다.
- 잘못된 plugin 조합(예: Compose 모듈인데 `compose-compiler`를 빠뜨림)을 컴파일러가 막지 못한다.

**Convention Plugin**은 Gradle이 공식적으로 제안하는 “빌드 로직을 plugin으로 추출해서 재사용한다”는 패턴이다. 우리는 이를 [Now in Android(NIA)](https://github.com/android/nowinandroid)의 구현 형태를 Momentwo 규모로 축약해 채택했다.

## 2. 전체 구조

```
Momentwo/
├─ build-logic/                          ← includeBuild 로 분리된 별도 Gradle build
│  ├─ settings.gradle.kts                ← rootProject 명 "build-logic", :convention 포함
│  └─ convention/
│     ├─ build.gradle.kts                ← kotlin-dsl, 9개 plugin 등록
│     └─ src/main/kotlin/cord/eoeo/momentwo/convention/
│        ├─ ProjectExtensions.kt         ← libs (VersionCatalog) 접근 helper
│        ├─ KotlinAndroid.kt             ← configureKotlinAndroid / configureKotlinJvm
│        ├─ AndroidCompose.kt            ← configureAndroidCompose
│        ├─ AndroidApplicationConventionPlugin.kt
│        ├─ AndroidApplicationComposeConventionPlugin.kt
│        ├─ AndroidLibraryConventionPlugin.kt
│        ├─ AndroidLibraryComposeConventionPlugin.kt
│        ├─ AndroidFeatureConventionPlugin.kt
│        ├─ AndroidHiltConventionPlugin.kt
│        ├─ AndroidRoomConventionPlugin.kt
│        ├─ AndroidNetworkConventionPlugin.kt
│        └─ JvmLibraryConventionPlugin.kt
│
├─ settings.gradle.kts                   ← pluginManagement { includeBuild("build-logic") }
├─ gradle/libs.versions.toml             ← 외부 의존성·플러그인 버전
└─ app/build.gradle.kts                  ← id("momentwo.android.application") ...
```

### 2.1 includeBuild 메커니즘

루트 `settings.gradle.kts`에서 다음 한 줄로 `build-logic`을 메인 빌드에 합류시킨다.

```kotlin
pluginManagement {
    includeBuild("build-logic")
    // ...
}
```

이렇게 하면 Gradle은 `build-logic` 안의 plugin을 “루트 빌드의 plugin marker”로 인식한다. 각 모듈은 `plugins { id("momentwo.android.application") }` 한 줄로 사용할 수 있다.

별도 모듈(`include(":build-logic:convention")`)이 아닌 **includeBuild**를 쓰는 이유는 빌드 격리다. `build-logic`은 자신의 settings로 자체 의존(`com.android.tools.build:gradle`, `kotlin-gradle-plugin` 등)을 가져오며, 메인 빌드의 분류(classpath)와 섞이지 않는다.

### 2.2 Version Catalog 공유

`build-logic/settings.gradle.kts`에서 메인 빌드의 카탈로그를 그대로 재사용한다.

```kotlin
versionCatalogs {
    create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}
```

따라서 plugin 코드 내부에서도 `libs.findLibrary("hilt-android")` 같은 호출이 그대로 동작한다. 버전 단일 출처(single source of truth)는 `gradle/libs.versions.toml`이다.

## 3. 공용 유틸

Plugin 본체에 들어가기 전, 모든 Android plugin이 공유하는 공통 헬퍼를 둔다.

### 3.1 `ProjectExtensions.kt`

```kotlin
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
```

`Project` 확장 프로퍼티 한 줄로, plugin 코드에서 `libs.findLibrary(...)`를 짧게 호출할 수 있게 한다.

### 3.2 `KotlinAndroid.kt`

```kotlin
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = 34
        defaultConfig.minSdk = 29
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    configure<KotlinAndroidExtension> {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
}
```

- compileSdk / minSdk / Temurin 25 toolchain / Java·Kotlin 출력 대상 17을 한 곳에서 결정한다.
- `CommonExtension` 타입을 받아 application이든 library든 동일하게 적용된다.
- JVM 전용 모듈을 위한 `configureKotlinJvm()`도 함께 정의한다.

### 3.3 `AndroidCompose.kt`

```kotlin
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.apply {
        buildFeatures.apply { compose = true }
    }
    dependencies {
        val composeBom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(composeBom))
        add("androidTestImplementation", platform(composeBom))
        add("implementation", libs.findLibrary("ui").get())
        add("implementation", libs.findLibrary("ui-graphics").get())
        add("implementation", libs.findLibrary("ui-tooling-preview").get())
        add("implementation", libs.findLibrary("material3").get())
        add("debugImplementation", libs.findLibrary("ui-tooling").get())
    }
}
```

Compose BOM, Compose UI/Graphics/Material3, Tooling preview까지의 “Compose를 켜면 무조건 따라오는 것”을 묶었다. Compose Compiler plugin 적용 자체는 각 plugin이 책임진다(`org.jetbrains.kotlin.plugin.compose`).

## 4. 9개 Convention Plugin 상세

| Plugin id | 적용 대상 | 책임 |
|---|---|---|
| `momentwo.android.application` | `:app` | AGP application·내장 Kotlin + Temurin 25 / 출력 대상 17 + targetSdk 34 |
| `momentwo.android.application.compose` | `:app` | application 위에 Compose Compiler + Compose 의존성 |
| `momentwo.android.library` | 모든 `:core:*`, `:feature:*:api/impl` | AGP library·내장 Kotlin + Temurin 25 / 출력 대상 17 + `testOptions.targetSdk` |
| `momentwo.android.library.compose` | `:core:designsystem`, `:core:ui`, `:feature:*:impl` | library 위에 Compose 옵션 |
| `momentwo.android.feature` | `:feature:*:impl` | `library + library.compose + hilt` + Lifecycle/Navigation/Hilt-compose/kotlinx-serialization |
| `momentwo.android.hilt` | Hilt 가 필요한 모든 모듈 | KSP + Hilt Gradle plugin + hilt-android + hilt-compiler(ksp) |
| `momentwo.android.room` | `:core:database` | KSP + Room Gradle plugin + `schemaDirectory("$projectDir/schemas")` + room/ktx/paging/compiler(ksp) |
| `momentwo.android.network` | `:core:network` | Retrofit + OkHttp BOM + okhttp/logging-interceptor + Moshi + Moshi converter |
| `momentwo.jvm.library` | `:core:model` | 순수 Kotlin JVM (org.jetbrains.kotlin.jvm), Temurin 25 / 출력 대상 17 |

### 4.1 `momentwo.android.application`

```kotlin
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.application")
                }
        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = 34
        }
    }
}
```

`:app`에서 가장 먼저 적용해야 하는 기반 plugin. AGP `application`·내장 Kotlin + Temurin 25 / 출력 대상 17 + targetSdk 34를 일괄 적용한다.

### 4.2 `momentwo.android.application.compose`

application 모듈에서 Compose를 켜는 plugin. `org.jetbrains.kotlin.plugin.compose`를 적용하고 `ApplicationExtension`에 `configureAndroidCompose`를 위임한다. application 한정 plugin이며, library는 `momentwo.android.library.compose`를 따로 둔다.

### 4.3 `momentwo.android.library`

```kotlin
extensions.configure<LibraryExtension> {
    configureKotlinAndroid(this)
    testOptions.targetSdk = 34
}
```

`com.android.library`와 AGP 내장 Kotlin을 사용한다. **`defaultConfig.targetSdk` 대신 `testOptions.targetSdk`를 쓰는 이유**: AGP 9에서 library DSL의 `defaultConfig.targetSdk`가 제거된다. NIA 가이드대로 안드로이드 테스트 대상 SDK만 명시한다.

### 4.4 `momentwo.android.library.compose`

`LibraryExtension`을 받아 `configureAndroidCompose`를 호출하는 미러 plugin. application의 그것과 동일한 의존성/buildFeatures를 라이브러리 모듈에도 보장한다.

### 4.5 `momentwo.android.feature`

```kotlin
with(pluginManager) {
    apply("momentwo.android.library")
    apply("momentwo.android.library.compose")
    apply("momentwo.android.hilt")
}
dependencies {
    add("implementation", project(":core:ui"))
    add("implementation", project(":core:designsystem"))
    add("implementation", project(":core:domain"))
    add("implementation", project(":core:navigation"))
    add("implementation", libs.findLibrary("lifecycle-runtime").get())
    add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
    add("implementation", libs.findLibrary("lifecycle-viewmodel").get())
    add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
    add("implementation", libs.findLibrary("navigation-compose").get())
    add("implementation", libs.findLibrary("hilt-compose").get())
    add("implementation", libs.findLibrary("kotlin-serialization").get())
}
```

`:feature:*:impl`이 반드시 갖춰야 하는 최소 의존을 한 곳에 모은 메타 plugin. 다른 convention plugin을 조합(`apply("momentwo.android.library")` …)해서 빌드 블록 한 줄로 “Feature 모듈”을 정의한다.

> 2026-10-12 Phase 2 준비 브랜치에서 `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` 기본 의존성을 추가했다. `core:common/model/data` 및 Coil/Paging 등은 각 Feature가 실제 사용하는 경우 직접 선언한다. serialization 런타임과 compiler plugin은 별개이며, 직렬화 키를 선언하는 모듈에는 `kotlinSerialization` plugin도 적용한다. 진행 상태는 [Phase 2 실행 계획](phase2-feature-migration-plan.md)을 따른다.

### 4.6 `momentwo.android.hilt`

```kotlin
with(pluginManager) {
    apply("com.google.devtools.ksp")
    apply("com.google.dagger.hilt.android")
}
dependencies {
    add("implementation", libs.findLibrary("hilt-android").get())
    add("ksp", libs.findLibrary("hilt-compiler").get())
}
```

Hilt를 사용하는 모든 모듈에 KSP까지 묶어 적용한다. `:feature:*:impl`은 `momentwo.android.feature`가 내부적으로 이 plugin을 끌어쓰며, `:core:data`처럼 Hilt module/Provides를 두는 모듈은 이 plugin을 직접 적용한다. `@HiltAndroidApp`은 여전히 `:app`에만 둔다.

### 4.7 `momentwo.android.room`

```kotlin
extensions.configure<RoomExtension> {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    add("implementation", libs.findLibrary("room").get())
    add("implementation", libs.findLibrary("room-ktx").get())
    add("implementation", libs.findLibrary("room-paging").get())
    add("ksp", libs.findLibrary("room-compiler").get())
}
```

Room의 schema export 위치를 모듈 디렉토리 기준 `schemas/`로 강제한다. Phase 1에서 `:core:database` 모듈이 생기면 이 plugin은 그 모듈에 붙고, schema는 `:core:database/schemas/`로 함께 이동한다.

### 4.8 `momentwo.android.network`

Retrofit + OkHttp BOM + Moshi 묶음을 한 번에 정의한다. Phase 1에서 `:core:network` 모듈만 이 plugin을 적용하게 되며, 네트워크 stack을 다른 모듈이 “모르게” 만들어 결합도를 차단한다.

### 4.9 `momentwo.jvm.library`

```kotlin
pluginManager.apply("org.jetbrains.kotlin.jvm")
configureKotlinJvm()
```

`:core:model`처럼 Android에 의존하지 않는 순수 Kotlin/JVM 모듈을 위한 plugin. Android SDK 의존이 없어 컴파일/테스트가 가장 빠르다.

## 5. 외부 버전과 포함 빌드 플러그인 참조

외부 라이브러리·플러그인 버전은 `gradle/libs.versions.toml`로 관리한다.
Convention Plugin은 `build-logic`의 `gradlePlugin` 등록과 `settings.gradle.kts`의 `includeBuild`로 연결한다.
모듈에서는 다음처럼 버전 없는 ID를 사용한다.

```kotlin
plugins {
    id("momentwo.android.feature")
}
```

내부 플러그인에 가짜 `unspecified` 버전을 지정하지 않으며, 루트의 형제 플러그인 `apply false` 등록도 필요하지 않다.
외부 플러그인에만 `alias(libs.plugins.…)`를 사용한다.

## 6. 사용 예시 (Cheat Sheet)

새 모듈을 만들 때 `build.gradle.kts`에 plugin ID 1~3줄만 적으면 된다.

### 6.1 `:app` (현재)

```kotlin
plugins {
    id("momentwo.android.application")
    id("momentwo.android.application.compose")
    id("momentwo.android.hilt")
}

android {
    namespace = "cord.eoeo.momentwo"
    defaultConfig {
        applicationId = "cord.eoeo.momentwo"
        versionCode = 1
        versionName = "1.0"
    }
}
```

app에는 Room/serialization 플러그인과 BASE_URL/BuildConfig 생성이 필요하지 않다. Room은 core:database, BASE_URL은 core:network, 경로 직렬화 compiler는 각 Feature API와 내부 직렬화 키가 있는 album-detail:impl이 소유한다.

### 6.2 `:feature:photo:impl` (현재, 주요 의존 발췌)

```kotlin
plugins {
    id("momentwo.android.feature")
}

android {
    namespace = "cord.eoeo.momentwo.feature.photo.impl"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:photo:api"))
}
```

`momentwo.android.feature` 한 줄로 library + compose + hilt + lifecycle + navigation-compose + serialization이 모두 들어온다.

### 6.3 `:core:network` (현재, 플러그인 발췌)

```kotlin
plugins {
    id("momentwo.android.library")
    id("momentwo.android.hilt")
    id("momentwo.android.network")
}

android { namespace = "cord.eoeo.momentwo.core.network" }
```

### 6.4 `:core:model` (현재)

```kotlin
plugins {
    id("momentwo.jvm.library")
}
```

## 7. 도입 이점

### 7.1 단일 출처(DRY)
compileSdk, minSdk, Java 버전, Kotlin JVM target, Compose 옵션, Hilt/Room 적용 방식이 **`build-logic` 한 곳**에 정의된다. SDK 버전을 올릴 때 `KotlinAndroid.kt` 한 파일만 수정하면 24개 모듈이 일괄 반영된다.

### 7.2 모듈 build.gradle.kts 슬림화
적용 전 `:app/build.gradle.kts`는 약 110줄 — Compose BOM, lifecycle, room 의존성이 모두 펼쳐져 있었다. Phase 3에서 app의 데이터/UI 구현 의존을 제거했으며, 새 feature 모듈에는 실제 사용하는 의존만 명시한다. 모듈 파일에 남는 것은 **그 모듈만의 특이사항**뿐이다.

### 7.3 일관성과 표준화
“Compose 모듈인데 Compose BOM을 빠뜨림”, “Hilt 모듈인데 KSP를 적용하지 않음” 같은 사람 실수를 plugin 단위로 봉쇄한다. 어떤 모듈이 어떤 능력을 갖는지가 plugin id만 보면 한눈에 드러난다.

### 7.4 plugin 조합을 통한 명시적 계약
`momentwo.android.feature` 한 줄이 곧 “이 모듈은 Compose UI + ViewModel + Hilt + Navigation을 쓴다”는 선언이다. 모듈의 역할을 plugin 조합으로 표현할 수 있다.

### 7.5 빌드 캐시 / 구성 캐시 친화적
Convention plugin은 한 번 컴파일된 후 캐시된다. `configuration-cache`와 `build-cache`(이미 활성화된 옵션) 하에서 동일 설정을 가진 모듈 빌드가 빠르게 재현된다. 모듈마다 다른 KTS 스크립트가 흩어져 있을 때보다 캐시 적중률이 높다.

### 7.6 새 모듈 진입 장벽 감소
Phase 1~2에서 23개 모듈을 새로 만들어야 한다. 새 `build.gradle.kts`를 작성할 때 “어떤 의존을 적어야 하지?”를 매번 고민할 필요 없이 plugin ID만 골라 적으면 된다. 멀티모듈 작업의 실제 비용은 모듈을 만드는 비용이 아니라 **각 모듈의 빌드 스크립트를 정합성 있게 유지하는 비용**인데, 그 비용을 사실상 0으로 만든다.

### 7.7 잘못된 의존 조합 차단
예를 들어 `:core:model`은 `momentwo.jvm.library`만 적용하므로 Android SDK / Compose를 의존성으로 끌어올 길이 컴파일러에서 막힌다. 아키텍처 규칙(예: `:core:model`은 순수 Kotlin)을 빌드 구성으로 강제할 수 있다.

### 7.8 멀티 빌드 격리
`build-logic`은 자체 settings를 가지는 별도 빌드다. 메인 빌드의 classpath 충돌(예: AGP 버전 두 개가 동시에 들어와 충돌)을 원천 차단한다.

## 8. 트레이드오프와 주의점

- **첫 빌드 cold-start 오버헤드**: `:build-logic:convention:compileKotlin`이 한 번 더 실행된다. 이후 incremental 빌드에는 영향이 없다.
- **Convention plugin 자체를 수정하면 적용 모듈 전체가 재구성된다.** 빈번한 수정은 피한다. 대신 catalog 값(`libs.versions.toml`) 조정으로 처리 가능한 변경은 그쪽을 우선한다.
- **IDE 인덱싱 부담 증가**: Android Studio가 build-logic을 별도 모듈처럼 인덱싱한다. 큰 문제는 아니나, 동기화가 더 길어진 인상을 받을 수 있다.
- **포함 빌드의 플러그인 참조**: `id("momentwo.…")`로 버전 없이 참조한다. Gradle 9에서는 같은 artifact의 플러그인에 가짜 `unspecified` 버전을 요청하면 unknown-version 충돌이 발생하므로 내부 플러그인의 catalog alias와 루트 형제 플러그인 `apply false` 등록을 제거했다. 외부 플러그인 버전은 카탈로그로 관리한다.
- **순환 의존**: convention plugin 안에서 다른 convention plugin을 `apply("momentwo.android.library")` 식으로 적용할 수 있지만, plugin 간 순환 의존이 생기지 않도록 조심한다. 본 구성에서는 `feature → library + library.compose + hilt`의 단방향 호출만 사용한다.

## 9. 마이그레이션 적용 결과

Phase 1~3에서 다음 설정을 적용했다.

- `momentwo.android.feature`에 `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` 기본 의존을 추가했다.
- `momentwo.android.room`은 `:core:database`에 적용하며 app에서는 제거했다.
- `momentwo.android.network`은 `:core:network`에 적용한다.
- `momentwo.jvm.library`는 순수 Kotlin `:core:model`에 적용한다. BaseViewModel을 사용하는 `:core:common`은 Android library다.

기존 모듈용 Convention 9개에 루트 검사용 `momentwo.module-architecture`를 추가했다. 모듈 공통 설정과 루트 전체 그래프 검사는 적용 책임을 구분한다.

### 루트 아키텍처 검사 (#158)

루트는 `id("momentwo.module-architecture")`를 버전 없이 적용한다. 형제 Convention도 각 모듈에서 버전 없는 ID로 참조한다. 수집기는 설정 완료 후 실제 프로젝트 configuration의 의존을 스냅샷으로 만들고, task는 이 값과 소스 입력만 검사한다. 앱 컴파일이나 dependency resolution 없이 경계·사이클을 진단한다. 루트/각 모듈 `check`에 연결되며 included build 자체는 별도 JUnit/TestKit으로 검증한다.

규칙·variant/테스트 맥락·지원하지 않는 의존의 처리·CI 범위는 [모듈 경계 가이드](module-boundaries.md)를 따른다. 모듈 수를 고정하지 않으므로 #160의 빈 navigation 삭제에도 적용된다.

## 10. 참고 자료

- [Now in Android — build-logic/convention](https://github.com/android/nowinandroid/tree/main/build-logic/convention)
- [Now in Android — ModularizationLearningJourney](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)
- [Gradle — Sharing build logic in a multi-repo setup](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
- [Gradle — Composite builds (includeBuild)](https://docs.gradle.org/current/userguide/composite_builds.html)
- [Gradle — Version catalogs](https://docs.gradle.org/current/userguide/platforms.html)

## 핵심 도구 전환 (#189)

Gradle 9.7.0·AGP 9.3.3·Kotlin 2.4.21을 사용한다. Android에서는 org.jetbrains.kotlin.android를 별도로 적용하지 않고 AGP 내장 Kotlin과 KotlinAndroidExtension 공개 인터페이스를 사용한다. 순수 JVM의 Kotlin 플러그인과 Compose Compiler·Serialization 플러그인은 2.4.21로 정렬한다.

실행·컴파일 toolchain은 Temurin 25.0.4.1+1이며 build-logic을 포함한 Java/Kotlin 출력은 17이다. build-logic의 kotlin-dsl 컴파일러는 Gradle 내장 Kotlin(9.7.0에서는 2.4.0)을 사용하고, 제품 KGP 버전과 구분한다. Hilt 2.60.1·KSP 2.3.12(KSP2)·Room 2.8.5를 함께 검증하며 minSdk 29 / compileSdk·targetSdk 34는 유지한다.

CommonExtension의 제네릭·블록 메서드 대신 공개 속성의 apply를 사용한다. core:network는 ProviderFactory.fileContents로 local.properties를 읽고 Gradle/AGP 내부 API에 의존하지 않는다. 새 DSL/내장 Kotlin 비활성화로 우회하지 않는다. 세부 검증은 [도구 업데이트 기록](toolchain-upgrade.md)을 따른다.
