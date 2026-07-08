# Momentwo Phase 1 — Core 모듈 분리 구현 계획

> 본 문서는 [멀티모듈 마이그레이션 계획](multi-module-migration-plan.md)의 **Phase 1(Core 분리)** 실행 계획이다. GitHub 상위 이슈 [#108](https://github.com/Momentwo/Momentwo/issues/108)에 대응하며, 각 `:core:*` 모듈은 서브이슈 #111~#121과 1:1로 매핑된다. GitHub Wiki 이관을 전제로 작성한다.

---

## 1. 배경 (Context)

Phase 0(PR [#105](https://github.com/Momentwo/Momentwo/pull/105))에서 convention plugin 9개, version catalog, `:app` 리팩터링, `settings.gradle.kts` `includeBuild` 기반이 완료됐다. Phase 1은 `:app`의 `data / domain / ui / di` 코드를 **11개 `:core:*` 모듈**로 분리한다. "PR 당 하나의 이슈" 원칙에 따라 모듈마다 별도 PR로 진행한다.

**검증 게이트**: 저장소에 CI 워크플로(`.github/workflows/`)가 없다. 따라서 게이트는 **로컬 Gradle**이며, 모든 커밋은 로컬에서 `./gradlew :app:assembleDebug` 통과를 유지해야 한다.

## 2. 사전 결정 및 조정

### 설계 결정

- **진입 순서**: 가장 단순한 leaf인 `:core:designsystem`을 워밍업으로 먼저 분리해 멀티모듈 셋업 워크플로우를 검증한 뒤, 의존성 순서대로 진행한다.
- **`:core:common` 적극 리팩터링**: Android library로 만들어 `BaseViewModel` + `@Dispatcher` 한정자 + `DispatchersModule`을 두고, 기존 `RepositoryImpl`의 하드코딩된 `Dispatchers.IO`를 주입 방식으로 전환한다.

### 계획서(§3.3) 대비 조정 2건

1. `ui/model/BottomNavigationItem.kt`가 `androidx.compose...ImageVector`에 의존한다. `:core:model`을 순수 JVM(`momentwo.jvm.library`)으로 유지하기 위해 `BottomNavigationItem`은 model이 아니라 **`:core:ui`로 이동**한다. 나머지 모델만 `:core:model`.
2. `AuthInterceptor`/`AuthAuthenticator`가 `PreferenceRepository`를 주입받는다 → **`:core:network → :core:datastore` 의존**이 발생한다. 따라서 **datastore를 network보다 먼저** 분리하고 이 의존을 허용한다. (대안: `:core:network`에 `TokenProvider` 인터페이스를 두고 datastore가 구현하여 DIP로 역전. 범위가 커지므로 기본은 직접 의존 허용, 리뷰 시 선택.)

## 3. 실행 순서 (11개 모듈)

| # | 모듈 | 이슈 | convention plugin | 의존 | 비고 |
|---|---|---|---|---|---|
| 1 | `:core:designsystem` | #117 | `library.compose` | — | 워밍업(theme만) |
| 2 | `:core:model` | #111 | `jvm.library` | — | 순수 JVM. BottomNavigationItem 제외 |
| 3 | `:core:common` | #112 | `library` + `hilt` | (lifecycle) | 적극: BaseViewModel + Dispatcher + impl 리팩터링 |
| 4 | `:core:ui` | #118 | `library.compose` | designsystem, model | composable + BottomNavigationItem |
| 5 | `:core:datastore` | #114 | `library` + `hilt` | model, common | Preference* + AuthModule datastore 파트 |
| 6 | `:core:network` | #113 | `library` + `network` + `hilt` | model, **datastore** | DTO(당분간 public), Api, Auth*, ApiModule |
| 7 | `:core:database` | #115 | `library` + `room` | model | DB/Dao/Entity/RemoteKey/DatabaseModule |
| 8 | `:core:data` | #116 | `library` + `hilt` | network, database, datastore, model, common | Impl/DataSource/RemoteMediator + 모든 Repo 인터페이스 + 도메인 Module |
| 9 | `:core:domain` | #119 | `library` + `hilt` | data, model, common | UseCase (Paging 사용 → android) |
| 10 | `:core:navigation` | #120 | `library` | model | Route 키/NavType/Builder 인터페이스 (NavHost는 app 잔존) |
| 11 | `:core:testing` | #121 | `library` | data, model, common | Fake/테스트 유틸 (없으면 최소·생략 가능) |

의존 규칙(계획서 §3.3) 유지 + 조정: `network → datastore` 허용. 금지: `core → feature`, `network ↔ database` 직접 의존.

## 4. 브랜치/PR 전략 — 스택형(체이닝) PR

각 모듈 브랜치를 **바로 아래 모듈 브랜치 위에서 분기**해 스택을 쌓는다. 병합을 기다리지 않고 위로 계속 작업할 수 있고(하위 모듈이 이미 브랜치에 포함되어 의존성이 자연 해결됨), 각 PR은 자기 모듈 diff만 노출한다.

```
develop
└─ refactor/core-designsystem   ← base develop         (PR → develop)
   └─ refactor/core-model       ← base core-designsystem (PR → core-designsystem)
      └─ refactor/core-common   ← base core-model
         └─ refactor/core-ui
            └─ refactor/core-datastore
               └─ refactor/core-network
                  └─ refactor/core-database
                     └─ refactor/core-data
                        └─ refactor/core-domain
                           └─ refactor/core-navigation
                              └─ refactor/core-testing
```

- 시작 전: `git fetch origin && git switch develop && git pull` 로 Phase 0(#105) 병합 상태를 확보한다.
- 브랜치 명명(기존 `refactor/build-logic-convention` 관례): `refactor/core-<module>`.
- **PR base = 바로 아래 브랜치** → 이슈 #111~#121과 1:1.
- **병합은 아래부터 순서대로**: 최하단(designsystem) PR 승인·병합 → 그 위(model) PR의 base를 `develop`으로 재지정(GitHub가 하위 병합 시 자동 재지정하기도 하나 수동 확인) → 병합 → 반복해 스택을 걷어올린다.
- **리뷰 수정 전파**: 하위 브랜치를 수정하면 그 위 스택을 재정렬한다 — `git rebase --onto <갱신된 하위> <옛 tip> <상위 브랜치>`. 하위 변경을 최소화하고 승인된 하위 PR을 즉시 병합해 스택을 짧게 유지하면 재정렬 비용이 준다.
- **충돌 안전**: `settings.gradle.kts`의 `include(...)`와 `app/build.gradle.kts`의 `implementation(project(...))` 추가는 스택상 순차 편집이라 스택 내부 충돌이 없다.

## 5. 커밋 컨벤션 & 원자적 단위 규칙

### 커밋 컨벤션 (deep-medi, develop 최근 스타일)

- 형식: `issue #<이슈번호> <type>: <한글 Subject>` (예: `issue #117 refactor: Theme/Color/Type 를 :core:designsystem 으로 이동`).
- type: `chore`(모듈 스캐폴딩·gradle·settings), `refactor`(코드 이동), `feat`(신규 추가: 한정자/모듈), `fix`.

### 원자적·빌드 가능 단위 규칙

- **모든 커밋이 초록**: 각 커밋 후 `./gradlew :app:assembleDebug`(+ 해당 모듈 `assembleDebug`) 성공.
- 표준 2커밋 템플릿:
  1. `chore: :core:X 모듈 생성 및 settings 등록` — 디렉토리 + `build.gradle.kts`(convention plugin, namespace) + `settings.gradle.kts include(":core:X")`. **빈 모듈이 컴파일됨.**
  2. `refactor: <내용> 를 :core:X 로 이동` — `git mv`로 파일 이동 + 패키지 `cord.eoeo.momentwo.core.X`로 리네임 + `:app`에 `implementation(project(":core:X"))` 추가 + `:app` 전역 import 정리. **파일 이동·import 갱신·의존성 추가는 한 커밋**이어야 빌드가 유지된다(부분 이동은 컴파일이 깨짐).
- 파일이 서로 독립적이면 이동을 하위 그룹으로 쪼개되(각 그룹이 초록), `MomentwoDatabase`처럼 상호 참조가 강하면 한 커밋으로 이동한다.

### 패키지/네임스페이스

이동 시 패키지를 `cord.eoeo.momentwo.core.<module>`로 리네임하고 모듈 namespace도 동일하게 한다. import 갱신은 이동 커밋에 포함한다(IDE Move refactor 또는 sed).

### Hilt 규칙 (Phase 1 동안)

- `@HiltAndroidApp`은 `:app`에만 유지. `@HiltViewModel`(화면 VM)은 **이동하지 않음**(Phase 2).
- `@Module @InstallIn(SingletonComponent::class)`는 각 모듈로 분산. `AuthModule`은 network/datastore로 **분할**.

## 6. 모듈별 상세 계획

### 1) :core:designsystem (#117) — 워밍업

- **브랜치** `refactor/core-designsystem`
- **이동**: `ui/theme/{Color,Theme,Type}.kt` → `core.designsystem.theme`. `Type.kt`가 참조하는 `res/font/*`가 있으면 모듈로 동반 이동.
- **커밋**
  1. `issue #117 chore: :core:designsystem 모듈 생성 및 settings 등록`
  2. `issue #117 refactor: Theme/Color/Type 를 :core:designsystem 으로 이동` (+ res/font, `:app` 의존성, `MomentwoTheme`·색상 import를 MainActivity/MomentwoApp/모든 Screen에서 갱신)
- **검증** `./gradlew :core:designsystem:assembleDebug :app:assembleDebug :app:lintDebug`; 앱 실행 → 테마 동일.
- **함정**: 폰트 리소스 누락. 다수 Screen의 theme import 갱신.

### 2) :core:model (#111)

- **브랜치** `refactor/core-model`
- **이동**: `ui/model/*.kt`(14개, **`BottomNavigationItem` 제외**) + `domain/model/{LoginData,Profile}.kt` → `core.model`. (domain/model은 외부 import 없음 확인 완료.)
- **제외**: `BottomNavigationItem.kt`는 `:core:ui` PR에서 처리(compose 의존).
- **커밋**
  1. `issue #111 chore: :core:model 모듈 생성 및 settings 등록` (`momentwo.jvm.library`)
  2. `issue #111 refactor: UI/도메인 모델을 :core:model 로 이동` (+ `:app` 의존성, import 정리)
- **검증** `./gradlew :core:model:assembleDebug :app:assembleDebug`.
- **함정**: `data/model`의 DTO 확장함수 `mapToXItem()`가 `core.model` 참조로 바뀜(import 갱신). 다른 android 타입 의존 모델이 더 있으면 해당 파일도 model에서 제외.

### 3) :core:common (#112) — 적극 리팩터링

- **브랜치** `refactor/core-common` (`momentwo.android.library` + `momentwo.android.hilt`)
- **신설/이동**
  - 신설: `@IODispatcher/@DefaultDispatcher/@MainDispatcher` 한정자 + `MomentwoDispatchers`; `DispatchersModule`(`@InstallIn(SingletonComponent)`, `Dispatchers.IO/Default/Main` 제공). (선택) `Result`/네트워크 예외 헬퍼.
  - 이동: `ui/BaseViewModel.kt`(`UiState/UiEvent/UiEffect` + `BaseViewModel` + `*_EFFECTS_KEY` 상수) → `core.common`.
- **커밋**
  1. `issue #112 chore: :core:common 모듈 생성 및 settings 등록`
  2. `issue #112 feat: Coroutine Dispatcher 한정자 및 DispatchersModule 추가`
  3. `issue #112 refactor: BaseViewModel/UiState/Event/Effect 를 :core:common 으로 이동` (+ `:app` 의존성, 모든 ViewModel import 갱신)
  4. `issue #112 refactor: RepositoryImpl 의 Dispatcher 를 주입 방식으로 전환` (`:app`의 `*RepositoryImpl` — 아직 app에 있음 — `withContext(Dispatchers.IO)` → 주입된 `@IODispatcher CoroutineDispatcher`)
- **검증** 각 커밋 초록 + 런타임 골든 패스(디스패처 동작 불변). 커밋4로 data PR은 순수 이동만 남긴다.
- **함정**: `DispatchersModule` 중복 바인딩 주의. BaseViewModel은 compose 비의존(lifecycle만) → common(android) 적합.

### 4) :core:ui (#118)

- **브랜치** `refactor/core-ui` (`momentwo.android.library.compose`), 의존 `:core:designsystem`, `:core:model`
- **이동**: `ui/composable/*.kt`(AlbumItemCard, CircleAsyncImage, UserItemBox, *Dialog 등) + `ui/model/BottomNavigationItem.kt` → `core.ui`.
- **커밋**
  1. `issue #118 chore: :core:ui 모듈 생성 및 settings 등록`
  2. `issue #118 refactor: 공용 Composable 및 BottomNavigationItem 을 :core:ui 로 이동` (+ 의존성, coil 등 필요 라이브러리, import 갱신)
- **검증** `./gradlew :core:ui:assembleDebug :app:assembleDebug`.
- **함정**: composable가 `core.model`·`core.designsystem` 참조. `CircleAsyncImage`의 coil 의존을 모듈에 포함.

### 5) :core:datastore (#114)

- **브랜치** `refactor/core-datastore` (`library` + `hilt`), 의존 `:core:model`, `:core:common`
- **이동**: `data/authentication/{PreferenceRepository, PreferenceRepositoryImpl, PreferenceKeys}.kt` → `core.datastore`. `AuthModule`에서 `DataStore<Preferences>` 생성 + `PreferenceRepository` 바인딩 파트 → datastore용 모듈로.
- **커밋**
  1. `issue #114 chore: :core:datastore 모듈 생성 및 settings 등록`
  2. `issue #114 refactor: PreferenceRepository/PreferenceKeys 를 :core:datastore 로 이동` (+ AuthModule datastore 파트 분할, `:app` 의존성, import 갱신)
- **검증** `./gradlew :core:datastore:assembleDebug :app:assembleDebug`.
- **함정**: `AuthModule` 분할 — datastore 파트만 이동, interceptor/okhttp 파트는 network PR까지 app에 잔존.

### 6) :core:network (#113)

- **브랜치** `refactor/core-network` (`library` + `network` + `hilt`), 의존 `:core:model`, **`:core:datastore`**
- **이동**: `data/MomentwoApi.kt`, `data/authentication/{AuthInterceptor,AuthAuthenticator}.kt`, `data/model/*`(DTO), `di/ApiModule.kt` + `AuthModule`의 interceptor/authenticator/okhttp 파트, (판단) `data/{CursorPagingSource,PagedPagingSource}.kt`. DTO→모델 매퍼는 DTO와 함께.
- **커밋**
  1. `issue #113 chore: :core:network 모듈 생성 및 settings 등록`
  2. `issue #113 refactor: MomentwoApi/DTO/Auth 인터셉터를 :core:network 로 이동` (+ `:core:datastore` 의존, `:app` 의존성, import 갱신)
  3. `issue #113 refactor: ApiModule/AuthModule 네트워크 파트를 :core:network 로 이동`
- **검증** `./gradlew :core:network:assembleDebug :app:assembleDebug`; 실 API 호출 골든 패스.
- **함정**: **DTO는 당분간 `public` 유지**(아직 `:app`의 RepositoryImpl이 참조) → data PR에서 `internal` 전환. `network→datastore` 의존 허용 반영.

### 7) :core:database (#115)

- **브랜치** `refactor/core-database` (`library` + `room`), 의존 `:core:model`
- **이동**: `data/MomentwoDatabase.kt`, 각 도메인 `data/*/local/{*Dao, *RemoteKeyDao}` + `entity/*`, `di/DatabaseModule.kt`. `schemaLocation`을 기존 경로와 동일 유지(room convention plugin이 `$projectDir/schemas` 강제 → 기존 스키마 파일 함께 이동).
- **제외**: `*RemoteMediator`, `*LocalDataSource`는 remote 의존 → `:core:data`.
- **커밋**
  1. `issue #115 chore: :core:database 모듈 생성 및 settings 등록`
  2. `issue #115 refactor: Room DB/Dao/Entity 를 :core:database 로 이동` (MomentwoDatabase가 전 Dao/Entity 참조 → 한 커밋 이동. + `:app` 의존성, import 갱신, schemas 이동)
- **검증** `./gradlew :core:database:assembleDebug :app:assembleDebug`; 스키마 export/DB 마이그레이션 정상.
- **함정**: schema 디렉토리 경로. Entity의 `mapToXItem()`가 `core.model` 참조.

### 8) :core:data (#116) — 최대 규모

- **브랜치** `refactor/core-data` (`library` + `hilt`), 의존 `:core:{network,database,datastore,model,common}`
- **이동**: 모든 `*RepositoryImpl`, `*DataSource`(remote/local), `*RemoteMediator`, `*PagingSource`(도메인별), **모든 Repository 인터페이스**(`domain/{album,friend,login,photo,profile,subalbum}/*Repository.kt` + `data/{comment,description,like,member,signup}/*Repository.kt`) → `core.data`(예: `core.data.repository`), `di/<도메인>Module.kt`(레포 바인딩) → data. `domain/mapper/ProfileMapper.kt` → data. `data/presigned/*` → data.
- **커밋** (하위 그룹으로 분할, 각 초록)
  1. `issue #116 chore: :core:data 모듈 생성 및 settings 등록`
  2. `issue #116 refactor: Repository 인터페이스를 :core:data 로 통합` (domain/·data/ 양쪽 인터페이스 이동 + `:app` 의존성 + import 갱신; UseCase·VM은 이제 `:core:data` 인터페이스 참조)
  3. `issue #116 refactor: RepositoryImpl/DataSource/RemoteMediator 를 :core:data 로 이동` (도메인 묶음이 크면 2~3개 커밋으로 분할: 예 album·friend·photo / comment·like·description·member / login·signup·profile·presigned)
  4. `issue #116 refactor: 도메인 Hilt Module(레포 바인딩)을 :core:data 로 이동`
  5. `issue #116 refactor: DTO 가시성을 internal 로 축소` (network의 DTO를 이제 data만 사용 → `internal` 전환)
- **검증** `./gradlew :core:data:assembleDebug :app:assembleDebug`; 전체 골든 패스.
- **함정**: 인터페이스 이동(커밋2)과 impl 이동(커밋3) 사이에도 app 초록 유지(app이 `:core:data` 의존). `QualifierModule`(@Remote/@LocalDataSource)도 data로. UseCase 제공은 domain PR로 미룸(여기선 바인딩만).

### 9) :core:domain (#119)

- **브랜치** `refactor/core-domain` (`library` + `hilt`), 의존 `:core:{data,model,common}`
- **이동/전환**: `domain/**/*UseCase.kt` → `core.domain`. UseCase는 `@Inject constructor`로 전환(레포 인터페이스가 data에 바인딩됨 → Hilt 자동 제공), 이동된 data 모듈에서 UseCase 제공 코드 제거. (Paging `PagingData` 사용 → android library.)
- **커밋**
  1. `issue #119 chore: :core:domain 모듈 생성 및 settings 등록`
  2. `issue #119 refactor: UseCase 를 :core:domain 으로 이동 및 @Inject 생성자 전환` (+ `:app` 의존성, VM의 UseCase import 갱신)
- **검증** `./gradlew :core:domain:assembleDebug :app:assembleDebug`.
- **함정**: UseCase provider 이중 정의 제거. `ProfileMapper`는 data에 잔류(도메인은 인터페이스만 참조).

### 10) :core:navigation (#120)

- **브랜치** `refactor/core-navigation` (`library`, `kotlinx-serialization`), 의존 `:core:model`
- **이동**: `ui/MomentwoNavGraph.kt`/`MomentwoNavigation.kt`의 **Route 키(@Serializable data class/object)** + 공통 `NavType` + `FeatureNavGraphBuilder` 인터페이스 → `core.navigation`. **NavHost 본체·화면별 Route 등록은 `:app`에 잔존**(Phase 2에서 feature:api로).
- **커밋**
  1. `issue #120 chore: :core:navigation 모듈 생성 및 settings 등록`
  2. `issue #120 refactor: Route 키/NavType 을 :core:navigation 으로 분리` (+ `:app` 의존성, import 갱신; NavHost는 app에서 이 키들을 참조)
- **검증** `./gradlew :core:navigation:assembleDebug :app:assembleDebug`; 네비게이션 골든 패스.
- **함정**: Route 키만 최소 분리. `@Serializable` 유지, serialization plugin 적용.

### 11) :core:testing (#121)

- **브랜치** `refactor/core-testing` (`library`), 의존 `:core:{data,model,common}`
- **내용**: 현재 Fake/테스트 유틸이 거의 없으면 **최소 스캐폴딩만**(`FakeXRepository` 뼈대, `MainDispatcherRule`) 또는 생략하고 이슈 클로즈. 있으면 이동.
- **커밋**
  1. `issue #121 chore: :core:testing 모듈 생성 및 settings 등록`
  2. (있을 때) `issue #121 feat: 공용 테스트 더블/유틸을 :core:testing 으로 이동`
- **검증** `./gradlew :core:testing:assembleDebug`.

## 7. 전체 검증 / 마무리

- 11개 PR 병합 후 `settings.gradle.kts`에 `:core:*` 11개 include 확인.
- `./gradlew clean assembleDebug` — 전체 모듈 빌드 통과.
- `./gradlew :app:installDebug` 후 골든 패스 6개(회원가입·로그인 / 앨범·상세 / 서브앨범·사진 / 좋아요·댓글·설명 / 친구 / 멤버) 수동 확인.
- 각 PR은 `:core:<x>:assembleDebug :app:assembleDebug :app:lintDebug` 통과가 병합 조건(CI 없음 → 로컬 필수). 스택 특성상 상위 브랜치 빌드는 하위 모듈을 이미 포함하므로 의존성이 자연 충족된다.
- 롤백: develop 병합 후에는 모듈 단위 revert 가능(모듈 경계=커밋 단위). 단 하위 모듈을 revert하면 이를 의존하는 상위 모듈이 깨지므로 위→아래 순서로 되돌린다.
- Phase 1 완료 시 `:app`에는 화면(Screen/VM/Contract/Route 등록)·NavHost·Application·MainActivity만 남고, 나머지는 `:core:*`로 이동 → Phase 2(feature 분리) 진입 준비 완료.

## 8. 주의

- 로컬 `develop`이 stale일 수 있으므로 반드시 `git fetch` 후 Phase 0 병합분에서 분기한다.
- `network→datastore` 직접 의존은 계획서(§3.3) 대비 조정이다. DIP(토큰 provider 인터페이스)를 원하면 network PR에서 대체한다.
- 새 모듈에서 `momentwo.android.*` plugin이 "not found"로 뜨면 설정 결함이 아니라 kotlin-dsl 캐시 손상이다 — `~/.gradle` 캐시(kotlin-dsl) 삭제 후 재빌드로 해결한다.
