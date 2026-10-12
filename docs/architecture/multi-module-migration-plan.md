# Momentwo 멀티모듈 마이그레이션 계획

> 본 문서는 Android 공식 권장 아키텍처([Guide to app architecture](https://developer.android.com/topic/architecture), [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations))와 [Now in Android](https://github.com/android/nowinandroid)(이하 NIA) 구현을 참고해, Momentwo 앱을 단일 모듈에서 멀티모듈로 전환하기 위한 실행 계획이다. GitHub Wiki 이관을 전제로 작성한다.

> **2026-10-12 진행 상태:** Phase 0·1·2 병합 완료. 준비 #143 및 Feature #144~#149를 각 새 브랜치에서 develop으로 순차 병합했다. Phase 3는 별도 `refactor/app-slimming`에서 app 의존성·플러그인·legacy 코드와 문서 정리 및 최종 검증을 완료했다. 병합 상태는 [#110](https://github.com/Momentwo/Momentwo/issues/110)을 따른다. 총 24개 모듈, Feature ViewModel 9개, app main 소스 6개 파일이다.
> 백엔드 배포 중단으로 실제 API 골든 패스는 미확인이다. 사용자가 승인한 빌드·Lint·자동 테스트·가능한 기기 확인 기준으로 진행했으며, 남은 통합 회귀는 [#150](https://github.com/Momentwo/Momentwo/issues/150)에서 추적한다. 단계별 실제 검증 결과는 [Phase 2 실행 기록](phase2-feature-migration-plan.md)을 따른다.

---

## 1. 배경 (Context)

Momentwo는 마이그레이션 시작 시 `:app` 단일 모듈로 구성된 Android 앱이었다. 현재는 Core 11개와 Feature api/impl 12개로 분리됐고, `:app`에는 앱 시작과 화면 조립 코드만 남아 있다. 아래 배경은 최초 계획 수립 당시의 문제를 설명한다.

그러나 단일 모듈에서 비롯되는 한계가 누적되고 있다.

- **빌드 시간**: 작은 UI 변경에도 전체 모듈이 재컴파일된다.
- **결합도**: `data` 구현체와 `ui`가 같은 모듈에 있어 잘못된 의존(예: UI에서 `*RepositoryImpl` 직접 참조)을 컴파일러가 막지 못한다.
- **경계 모호함**: feature 간 import가 자유로워, 향후 한 화면 단위로 떼어 내거나 라이브러리화하기 어렵다.
- **재사용 단위 부재**: theme/composable/공용 모델 등이 명확한 "core"로 묶여 있지 않다.

이 계획은 **공식 권장사항과 NIA 구조를 Momentwo 규모에 맞게 축약**해 점진적으로 적용한다.

## 2. 설계 결정 요약

| 항목 | 선택 | 근거 |
|---|---|---|
| Feature 분할 단위 | 혼합 6개 (`auth`, `album`, `album-detail`, `photo`, `friend`, `subalbum`) | 화면 단위(11개)는 모듈이 과다, 도메인 4개는 album 모듈이 비대해짐. 중간 절충 |
| api/impl 분리 | **적용** (NIA 방식) | cross-feature 이동 시 Route 키만 노출하여 결합도 최소화 |
| Domain 위치 | `:core:domain`(UseCase) + `:core:data`(Repository 인터페이스) | NIA와 동일. UseCase 재사용성 보장 |
| 마이그레이션 | 단계적 전체 마이그레이션 (Phase 0 → 3) | 각 PR이 독립적으로 빌드/런타임 가능 |

## 3. 구현 아키텍처

### 3.1 모듈 트리

```
:app                              ← MomentwoApplication, MainActivity, NavHost, Theme 연결

build-logic/                      ← includeBuild
  └─ convention/                  ← 모든 convention plugin

:core:model                       ← 순수 Kotlin. 도메인 모델(AlbumItem, PhotoItem 등)
:core:common                      ← Android library. BaseViewModel, Dispatcher, 공통 확장
:core:network                     ← Retrofit, OkHttp, AuthInterceptor, MomentwoApi
:core:database                    ← Room(MomentwoDatabase, Dao, Entity, RemoteKey)
:core:datastore                   ← PreferenceRepository(토큰/세션)
:core:data                        ← Repository 인터페이스 + Impl, Service/DTO/DataSource/RemoteMediator
:core:domain                      ← UseCase
:core:designsystem                ← Theme, Color, Type, 기본 컴포넌트
:core:ui                          ← 도메인성 공유 Composable(AlbumItemCard, UserItemBox 등)
:core:navigation                  ← 현재 공용 소스 없음. 통합 키는 feature:api로 이관 완료
:core:testing                     ← MainDispatcherRule. Auth Fake는 해당 impl 테스트에만 위치

:feature:auth:api                 ← Login/SignUp/Profile destination 키만
:feature:auth:impl                ← Login/SignUp/Profile Screen+VM+Contract
:feature:album:api
:feature:album:impl               ← Album, CreateAlbum
:feature:album-detail:api
:feature:album-detail:impl        ← AlbumDetail, AlbumSetting, ChangeImage, Member
:feature:subalbum:api
:feature:subalbum:impl            ← SubAlbumList
:feature:photo:api
:feature:photo:impl               ← PhotoList, PhotoDetail, Comment, Like, Description
:feature:friend:api
:feature:friend:impl              ← Friend, FriendRequest, FriendList
```

총 **24개 include 모듈** (`:app` 1 + `:core:*` 11 + `:feature:*` 12). `build-logic`은 `includeBuild`로 별도.

### 3.2 화면 → Feature 매핑 (sub-feature 흡수)

| 기존 UI 폴더 | 흡수 대상 |
|---|---|
| `login`, `signup`, `profile` | `:feature:auth` (계정 화면) |
| `album`, `createalbum` | `:feature:album` |
| `albumdetail`, `albumsetting`, `changeimage`, `member` | `:feature:album-detail` |
| `albumdetail/subalbumlist` | `:feature:subalbum` (stateless UI, 상태 소유는 album-detail) |
| `photolist`, `photodetail`, comment/like/description 부속 | `:feature:photo` |
| `friend`, `friendrequest`, `friendlist` | `:feature:friend` |

### 3.3 의존성 규칙

```text
:app ─────────────► Feature impl 6개, 실제 키를 쓰는 Feature api 5개,
                    core:designsystem, core:model
:feature:X:impl ──► 자신의 api, 실제 사용하는 core 모듈
:feature:album-detail:impl ──► :feature:subalbum:api
:feature:X:api ───► serialization 런타임만 (현재 Core 의존 없음)
:core:domain ─────► :core:data, :core:model, :core:datastore
:core:data ───────► :core:common, :core:model, :core:network,
                    :core:database, :core:datastore
:core:network ────► :core:datastore
:core:database ───► :core:model
:core:ui ─────────► :core:designsystem, :core:model
```

모든 impl의 convention 기본 의존은 core:ui/designsystem/domain/navigation이다. common/model/data는 실제 사용에 따라 직접 추가한다. 화면 이동은 app callback, SubAlbum과 상세의 중첩 화면 연결은 app이 주입하는 Composable slot으로 조립한다. 전체 실제 프로젝트 의존성과 테스트 의존은 [module-graph.md](module-graph.md)에 정리했다.

**금지**:
- `:core:* → :feature:*` (절대)
- `:feature:X:impl → :feature:Y:impl` (api만 허용)
- `:core:network ↔ :core:database` 직접 의존 (둘 다 `:core:data`로만 흐름)

### 3.4 Mermaid 그래프

```mermaid
flowchart TD
  app[app: 루트 그래프와 슬롯]
  impl[Feature impl 6개: UI와 MVI]
  api[Feature api: 경로 키와 인자]
  detail[album-detail impl]
  subapi[subalbum api]
  ui[core:ui]
  ds[core:designsystem]
  domain[core:domain]
  data[core:data]
  network[core:network]
  database[core:database]
  datastore[core:datastore]
  model[core:model]
  app --> impl
  app --> api
  app --> ds
  app --> model
  impl --> api
  impl --> ui
  impl --> domain
  impl --> data
  detail --> subapi
  ui --> ds
  ui --> model
  domain --> data
  domain --> model
  domain --> datastore
  data --> network
  data --> database
  data --> datastore
  data --> model
  network --> datastore
  database --> model
```

위 그래프는 주요 실행 의존을 묶어 표현한다. SubAlbum은 data를 직접 사용하지 않으며, 전체 convention/모듈별 의존은 별도 표를 따른다.

## 4. Convention Plugin (`build-logic/convention`)

NIA의 16개를 다음 **9개**로 축약한다.

| Plugin id | 적용 모듈 | 책임 |
|---|---|---|
| `momentwo.android.application` | `:app` | AGP application, compileSdk/minSdk, Java 17, Kotlin |
| `momentwo.android.application.compose` | `:app` | Compose BOM, compose compiler |
| `momentwo.android.library` | 모든 `:core:*`, `:feature:*` | AGP library + Kotlin 공통 |
| `momentwo.android.library.compose` | `:core:designsystem`, `:core:ui`, `:feature:*:impl` | Compose 옵션 |
| `momentwo.android.feature` | `:feature:*:impl` | library.compose + Hilt + 기본 의존성(core:ui, core:designsystem, core:domain, core:navigation, lifecycle, navigation-compose) |
| `momentwo.android.hilt` | Hilt 필요 모듈 | hilt plugin + dagger-hilt + ksp |
| `momentwo.android.room` | `:core:database` | room plugin + schema 디렉토리 |
| `momentwo.android.network` | `:core:network` | retrofit + okhttp + moshi 묶음 |
| `momentwo.jvm.library` | `:core:model` | 순수 Kotlin JVM 모듈 (`:core:common`은 Android library) |

`gradle/libs.versions.toml`에 plugin 카탈로그 추가: `momentwo-android-application`, `momentwo-android-library`, … (NIA `libs.plugins.nowinandroid.*` 패턴 모방.)

## 5. 마이그레이션 단계

각 단계는 **별도 PR**, 빌드와 런타임이 깨지지 않음을 검증한 후 머지한다.

### Phase 0 — 기반 정비 (PR 1)

- `build-logic/convention` 디렉토리 생성, `settings.gradle.kts`에 `includeBuild("build-logic")` 추가.
- 위 9개 convention plugin 작성 (NIA `build-logic/convention/src/main/kotlin/` 참고).
- `:app/build.gradle.kts`를 convention plugin 적용 형태로 리팩터링 (모듈 분할 전이라도 적용 가능).
- `gradle/libs.versions.toml`에 plugin 카탈로그 보강, version catalog 표준화.
- **검증**: `./gradlew :app:assembleDebug`, `:app:lintDebug` 통과, 앱 실행 동일.

### Phase 1 — Core 분리 (PR 2~12, 모듈당 1 PR)

**구현 완료.** 실제 순서와 변경 결정은 [Phase 1 문서의 완료 기록](phase1-core-migration-plan.md)을 따른다. 아래 목록은 모듈별 책임을 설명한다.

의존이 가장 적은 것부터:

1. `:core:model` — `ui/model/*` + `data/model/*` 중 도메인 모델 이동. JVM library.
2. `:core:common` — Dispatcher, Result, 공통 확장(필요 시 신설).
3. `:core:network` — `data/MomentwoApi.kt`, `data/authentication/AuthInterceptor`, `AuthAuthenticator`, ApiModule 이동. Service/DTO는 응집도를 위해 `:core:data`에 둔다(PR #136).
4. `:core:datastore` — `data/authentication/PreferenceRepository*`, `PreferenceKeys` 이동.
5. `:core:database` — `data/MomentwoDatabase`, 각 도메인의 `local/*Dao`, `*Entity`, `RemoteKey*` 이동.
6. `:core:data` — 모든 `*RepositoryImpl`, `*DataSource`, `*RemoteMediator` 이동. Repository 인터페이스도 함께. di 모듈(`AlbumModule` 등)도 이관.
7. `:core:designsystem` — `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` + 기본 컴포넌트.
8. `:core:ui` — `ui/composable/*` (AlbumItemCard, CircleAsyncImage, UserItemBox 등).
9. `:core:domain` — 모든 UseCase 이동(`GetAlbumListUseCase`, `DownloadPhotoUseCase`, `ChangeSubAlbumTitleUseCase` 등).
10. `:core:navigation` — 통합 Route 키 분리. 커스텀 NavType/Builder 인터페이스는 현재 필요하지 않아 만들지 않았다(PR #138). NavHost 본체는 `:app`에 잔존.
11. `:core:testing` — MainDispatcherRule을 제공하는 최소 테스트 기반. Fake Repository와 실제 MVI 테스트는 후속 작업.

각 PR 검증: Android Core는 `:core:<x>:assembleDebug`, JVM model은 `:core:model:assemble`과 `:app:assembleDebug`를 사용한다. 수동 골든 패스의 이번 점검 결과는 미확인이며 Phase 2 사전 검증으로 남긴다.

### Phase 2 — Feature 분리 (PR 13~18, feature당 1 PR)

**구현·병합 완료.** 준비 #143, Auth #144, Friend #145, SubAlbum #146, AlbumDetail #147, Album #148, Photo #149 순으로 진행했다. [Phase 2 실행 계획과 기록](phase2-feature-migration-plan.md)을 따른다. Profile은 auth에 포함하며 기존 6개 Feature/24개 모듈 목표를 유지한다. PR 13~18은 계획상 순번이며 준비 PR은 별도다.

의존이 단순한 것부터:

1. `:feature:auth` — Login/SignUp/Profile. core:domain의 로그인 UseCase 및 core:data의 기존 Repository를 사용. cross-feature 이동은 app callback으로 연결.
2. `:feature:friend`
3. `:feature:subalbum` — stateless UI 분리. 상태·이벤트는 AlbumDetail에 유지하고 app이 slot으로 연결.
4. `:feature:album-detail` — subalbum:api의 내부 경로 키 사용. SubAlbum 구현은 app slot으로 연결, Photo 이동도 callback으로 유지.
5. `:feature:album` — 상세/친구/Profile 이동은 app callback으로 연결.
6. `:feature:photo`

각 feature는 동시에 `:api`와 `:impl` 두 모듈로 떼어낸다.
- `:feature:X:api` — 독립 `@Serializable` destination 키 + Route 인자 모델만. 현재 core:navigation에 있는 해당 키를 이관.
- `:feature:X:impl` — 기존 `*Route.kt` Composable 진입점/Screen/VM/Contract를 이관하고 그래프 등록 함수 제공. SubAlbum은 독립 VM 없이 화면 렌더링만 분리.

`:app`은 모든 impl을 조립한다. 루트 NavHost에서 authScreens/friendScreen/albumDetailScreen/albumScreens/photoScreens를 호출하며, SubAlbum은 상세 내부 destination의 슬롯으로 연결한다.

각 PR 검증: `./gradlew :feature:<x>:impl:assembleDebug :app:assembleDebug` 통과 + 해당 feature 진입/이탈 동작 확인.

### Phase 3 — `:app` 슬림화 + 마무리 (계획상 PR 19, #110)

별도 브랜치 `refactor/app-slimming`, base=develop/b913d8d. clean 전체 빌드·Lint·단위 테스트 18개(39s), 기기 테스트 7개(2m 37s)를 통과했다.

- `:app/src/main/java`에는 Application, MainActivity, MomentwoApp Composable(NavHost 연결), 앱 레벨 Theme 적용만 잔존.
- Room/serialization 플러그인, 불필요한 Core·네트워크·세션·Paging·Lifecycle·Coil Compose 직접 의존과 app BuildConfig 생성 설정 제거.
- 빈 MomentwoDestination 및 core:navigation의 불필요한 serialization 설정, 미사용 색상 리소스·기본 예제 테스트 제거. core:navigation은 기존 24개 모듈 계약을 유지하는 빈 공용 모듈로 남는다.
- 앱 시작 코드와 루트 Navigation/슬롯 6개 파일만 유지하고 내부 조립 함수의 접근 범위 정리.
- README와 본 문서 최신화.

## 6. 주요 파일 매핑 (이동 표)

| 마이그레이션 전 경로 | 현재 소유 모듈 |
|---|---|
| `data/MomentwoApi.kt` | `:core:network` |
| `data/MomentwoDatabase.kt` | `:core:database` |
| `data/authentication/AuthInterceptor`, `AuthAuthenticator` | `:core:network` |
| `data/authentication/Preference*` | `:core:datastore` |
| `data/*/`(repo impl, datasource) | `:core:data` |
| `data/model/`(DTO) | `:core:data` (Service/DataSource와 함께, PR #136 결정) |
| `data/*/local/`(Dao, Entity) | `:core:database` |
| `data/*/local/RemoteKey*` | `:core:database` |
| `data/*/RemoteMediator*` | `:core:data` |
| `domain/**/*UseCase.kt` | `:core:domain` |
| `domain/**/*Repository.kt`(interface) | `:core:data/repository/` |
| `ui/theme/` | `:core:designsystem` |
| `ui/composable/` | `:core:ui` |
| `ui/model/` | `:core:model` |
| 통합 Route 키(Phase 1의 core:navigation 포함) | `:feature:<feature>:api` |
| `ui/MomentwoNavGraph.kt` (NavHost) | `:app` |
| `core/navigation/MomentwoDestination`의 해당 키 | `:feature:<feature>:api` |
| `ui/<feature>/*Route.kt` (Composable) | `:feature:<feature>:impl` |
| `ui/<feature>/*Screen.kt`, `*ViewModel.kt`, `*Contract.kt` | `:feature:<feature>:impl` |
| `di/ApiModule`, `AuthModule` | `:core:network` |
| `di/DatabaseModule` | `:core:database` |
| `di/<domain>Module.kt` | `:core:data` (Repository 바인딩) |

## 7. 명명 규칙 (공식 권장 적용)

- 메서드: 동사구 (`makePayment()`)
- 속성: 명사구 (`inProgressTopicSelection`)
- Flow 스트림: `getXxxStream()` / 리스트면 `getXxxsStream()`
- Repository: 인터페이스 `AlbumRepository`, 기본 구현 `DefaultAlbumRepository`(오프라인 우선이면 `OfflineFirstAlbumRepository` 패턴). 현재의 `*RepositoryImpl` 명명은 점진 통일 권장.
- Fake: `FakeAlbumRepository`.
- 현재 MVI: BaseViewModel이 StateFlow 상태, SharedFlow 이벤트, Channel 효과를 노출한다. 화면별 State/Event/Effect Contract와 lifecycle 상태 수집을 유지한다. Orbit 도입·Reducer/동시 갱신 개선은 이번 이관에 포함하지 않았다.
- UI 수집: `collectAsStateWithLifecycle()` (raw `collectAsState()` 지양).

## 8. 검증 절차

**빌드**:
- `./gradlew clean :core:model:assemble assembleDebug` — JVM model과 Android 모듈 빌드 통과
- `./gradlew :app:installDebug` — 앱 설치
- `./gradlew :app:lintDebug testDebugUnitTest` — 전체 Android 단위 테스트. app의 실제 그래프 회귀는 connectedDebugAndroidTest로 확인한다. ktlint/detekt는 미구성.

**런타임 (수동 골든 패스)**:
1. 신규 회원가입 → 로그인 (`:feature:auth`)
2. 앨범 리스트 → 앨범 생성 → 앨범 상세 (`:feature:album` → `:feature:album-detail`)
3. 서브앨범 진입 → 사진 리스트 → 사진 상세 (`:feature:subalbum` → `:feature:photo`)
4. 좋아요/댓글/설명 (`:feature:photo`)
5. 친구 추가/목록 (`:feature:friend`)
6. 앨범 멤버 관리 (`:feature:album-detail`)

실제 API 골든 패스는 배포 복구 후 #150에서 확인한다. 2026-10-12 사용자는 빌드·Lint·자동 테스트·가능한 기기 확인 후 병합/계속 진행을 승인했다. 기기 테스트 7개는 화면 연결·인자 복원·백스택·몰입 모드·슬롯 콜백을 확인하며, 실제 API 또는 정식 앱 업데이트/프로세스 복원을 통과로 간주하지 않는다.

## 9. 리스크 및 대응

| 리스크 | 대응 |
|---|---|
| Hilt 다중 모듈 그래프 깨짐 | `@HiltAndroidApp`은 `:app`에만. `@InstallIn(SingletonComponent::class) @Module`은 각 모듈에 분산. `@HiltViewModel`은 `:feature:*:impl`에만. `momentwo.android.hilt` plugin이 일관 적용 보장 |
| Room schema 위치 변경 | `:core:database`로 이동 시 `schemaLocation` 경로를 기존과 동일하게 유지. plugin에서 옵션 강제 |
| 데이터 경계 | UI 모델은 core:model, DTO/Service는 core:data 소유. 기존 공개 DTO 경계는 유지하며 전체 비공개화는 별도 후속 작업 |
| Navigation 저장 경로 | Feature API와 상세 내부 키에 기존 @SerialName 유지. JSON 왕복·기기 인자 복원 통과, 정식 업데이트/프로세스 복원은 #150에서 별도 확인 |
| 빌드 시간 일시적 증가 | `gradle.properties`에 `org.gradle.configuration-cache=true`, `org.gradle.caching=true`, `org.gradle.parallel=true` 확인 |
| 순환 의존 발생 | 이동은 app callback, 중첩 화면은 app 주입 slot으로 조립. 키 직접 참조가 필요한 경우에만 다른 feature:api 사용 |

## 10. 후속 문서 (선택)

- `docs/architecture/module-graph.md` — 모듈 그래프 단독 페이지 (Wiki 사이드바 인용용)
- `docs/architecture/naming-conventions.md` — 공식 권장 명명 규칙 단독 페이지
- `docs/architecture/adr/0001-multi-module.md` — 결정 기록(ADR)

## 11. 참고 자료

- [Android Guide to app architecture](https://developer.android.com/topic/architecture)
- [Android Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Now in Android — ModularizationLearningJourney](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)
- [Now in Android — ArchitectureLearningJourney](https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md)
- [Now in Android — build-logic/convention](https://github.com/android/nowinandroid/tree/main/build-logic/convention)
