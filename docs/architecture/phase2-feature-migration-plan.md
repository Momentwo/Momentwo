# Momentwo Phase 2 — Feature 분리 재개 계획

> 2026-10-02 작성. 기준: `develop` / `c67f368` (원격 develop과 동일).
> 2026-10-12 갱신: 작업별 새 브랜치·PR, develop 기준 순차 병합 흐름 확정. 아래 초기 현황은 재개 전 기준이며, 실제 완료 상태와 검증 결과는 실행 기록에 누적한다.
> 상위 이슈: [#109](https://github.com/Momentwo/Momentwo/issues/109). 전체 계획: [multi-module-migration-plan.md](multi-module-migration-plan.md).
> 이 문서는 현재 코드에 맞춘 Phase 2 실행 기준이다. 기존 하위 이슈의 Route 이동 설명과 다른 부분은 아래 결정에 따른다.

## 현재 완료 상태 (2026-10-12)

| 단계 | 병합 PR | 상태 |
|---|---|---|
| P0 준비 | [#143](https://github.com/Momentwo/Momentwo/pull/143) | 완료 |
| P1 Auth | [#144](https://github.com/Momentwo/Momentwo/pull/144) | 완료, #122 종료 |
| P2 Friend | [#145](https://github.com/Momentwo/Momentwo/pull/145) | 완료, #123 종료 |
| P3 SubAlbum | [#146](https://github.com/Momentwo/Momentwo/pull/146) | 완료, #124 종료 |
| P4 AlbumDetail | [#147](https://github.com/Momentwo/Momentwo/pull/147) | 완료, #125 종료 |
| P5 Album | [#148](https://github.com/Momentwo/Momentwo/pull/148) | 완료, #126 종료 |
| P6 Photo | [#149](https://github.com/Momentwo/Momentwo/pull/149) | 완료, #127 종료 |

Phase 2 종료 검증: develop/b913d8d에서 clean 전체 빌드·app Lint·전체 Android 단위 테스트 통과(48s). Feature 테스트 18개와 당시 app 예제 1개가 통과했다. 기기 Navigation 테스트 7개 통과, 모듈 경계 감사에서 24개 include·Feature VM 9개·app main 파일 6개와 금지 의존 없음 확인. #109는 종료하고 빈 legacy 타입·app 의존/플러그인/문서 정리를 #110의 별도 refactor/app-slimming에 인계했다.

아래 계획의 초기 상태/미래형 설명은 재개 전 기준 기록이다. 실제 API 골든 패스와 정식 업데이트·프로세스 복원은 미확인으로 [후속 회귀 #150](https://github.com/Momentwo/Momentwo/issues/150)에 남긴다. 전체 마이그레이션 결과는 [현재 아키텍처](multi-module-migration-plan.md)와 [실제 의존 표](module-graph.md)를 따른다.

## 1. 재개 기준과 범위

- Phase 0: convention plugin 9개 도입 완료(PR #105).
- Phase 1: Core 11개 분리 및 Dispatcher 후속 작업 완료(PR #129, #131~#141; #130은 #141로 대체되어 미병합 종료).
- 재개 전 기준은 `:app` + `:core:*` 11개 = 12개 모듈이다. 이후 Feature 모듈 추가 현황은 실행 기록을 따른다.
- 2026-10-02 `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest` 성공. 증분 빌드이며 clean 빌드 결과는 아니다.
- app 의존에 포함되지 않는 `:core:testing:assembleDebug`도 별도 성공(8s, 증분 빌드).
- 기기 골든 패스는 이번 점검에서 실행하지 않았다. Phase 2 구현을 시작하기 전 기준 동작을 기록하고, 각 PR 병합 전에 해당 경로를 재검증한다.
- MVI는 화면 ViewModel 9개에 이미 적용돼 있다. `BaseViewModel`, State/Event/Effect, `collectAsStateWithLifecycle()`을 유지한다.

목표는 **6개 Feature × api/impl = 12개 모듈 추가**, 최종 include 24개다. 화면 동작 보존과 모듈 경계 확보에 집중한다. Orbit 도입, 공통 Reducer 재설계, 신규 Profile UI, 전체 DTO 비공개화, Repository 명명 일괄 변경은 별도 후속 작업이다.

## 2. 구현 결정

### 2.1 Feature 소유권

| Feature | api의 경로 키 | impl로 이동할 현재 UI 패키지 |
|---|---|---|
| auth | Login, SignUp, Profile | `ui/login`, `ui/signup`(transformation 포함), `ui/profile` |
| friend | Friend | `ui/friend` 전체(friendlist, friendrequest, 내부 Navigation 포함) |
| subalbum | SubAlbumList(상세 내부 경로) | `ui/albumdetail/subalbumlist` |
| album-detail | AlbumDetail | `ui/albumdetail`의 나머지(albumsetting, member, 내부 Navigation 포함) |
| album | Album, CreateAlbum | `ui/album`, `ui/createalbum` |
| photo | PhotoList, PhotoDetail | `ui/photolist`, `ui/photodetail`(comment/description BottomSheet 포함) |

Profile은 계정 화면으로 `auth`에 포함한다. 현재 빈 본문과 불완전한 프로필 표시 기능은 그대로 유지하며, 기능 완성은 이동 PR에 포함하지 않는다.

### 2.2 경로 키와 Composable Route 구분

- `api`: 독립적인 `@Serializable` object/data class와 이동 인자만. 예: `LoginDestination`, `ProfileDestination(nickname)`.
- `impl`: 기존 `LoginRoute()`, `PhotoListRoute()` 같은 **Composable 진입점**, Screen, ViewModel, Contract, 내부 UI, 그래프 등록 함수.
- 현재 `core:navigation/MomentwoDestination`의 해당 키를 Feature 이동 PR에서 함께 옮긴다. `NavHost`, `navigate`, `SavedStateHandle.toRoute`의 타입 참조를 같은 커밋에서 갱신한다.
- 새 키는 기존 `sealed MomentwoDestination`을 상속하지 않는다. 필요한 마커가 없다면 독립 키만 사용하고, 남은 기존 키는 미이관 화면의 작업이 끝날 때까지 유지한다.
- 타입명/패키지 변경에 따른 저장된 백스택 호환성은 배포 전 확인한다. 기존 버전 위 업데이트·프로세스 재생성에서 실패하면 경로 직렬화 이름 유지 등 호환 방안을 이동 PR에서 적용한다.
- `Member`, `AlbumSetting`, `ChangeImage`와 Friend 내부 경로는 해당 impl 내부에 유지한다. 다른 Feature가 이동해야 하는 키만 api로 공개한다.

### 2.3 의존성과 플러그인

```text
app -> feature:*:impl, feature:*:api
feature:X:impl -> 자신의 api, 필요한 core 모듈
feature:X:impl -> 다른 feature:Y:api (키 참조가 필요한 경우만)
feature:X:api -> core:navigation, core:model (실제로 사용하는 경우만)
```

`core -> feature`, `feature:X:impl -> feature:Y:impl`, `api -> impl`, `api -> core:data`는 금지한다. api는 ViewModel/Contract, Compose 함수, DTO, Repository를 노출하지 않는다.

- `momentwo.android.feature`에 기존 계획대로 `core:ui`, `core:designsystem`, `core:domain`, `core:navigation` 기본 의존성을 추가한다.
- 모든 impl이 `BaseViewModel`/UI 모델을 직접 사용하는 것은 아니므로 `core:common`, `core:model`, `core:data`는 실제 import에 따라 명시한다. 기존 ViewModel의 Repository 직접 사용은 허용하며, 모두 UseCase로 감싸는 작업은 추가하지 않는다.
- 직렬화 키를 선언하는 api에 `kotlinSerialization` plugin과 serialization 런타임을 명시한다. impl도 내부 직렬화 키가 있으면 compiler plugin을 명시한다. Feature plugin의 serialization 런타임 추가만으로 compiler plugin 적용을 대신할 수 없다.
- Coil, Paging, icons, SavedStateHandle 관련 의존성을 이동 파일의 import 기준으로 각 impl에 추가한다. app의 의존성이나 다른 모듈의 `implementation`에 기대지 않는다.
- `core:testing`은 `testImplementation`으로 연결한다. 테스트 프레임워크 의존성은 runtime 의존성으로 넣지 않는다.

화면 간 이동은 기존 callback 방식을 우선 유지한다. `app`이 경로 키와 구현을 조립하므로, 이동 callback만 필요한 Feature에는 다른 Feature api 의존성도 강제로 추가하지 않는다. 미래 api의 빈 스텁을 먼저 만들 필요가 없다.

### 2.4 SubAlbum과 AlbumDetail의 연결

현재 SubAlbum은 독립 ViewModel/Contract 없이 AlbumDetail의 상태·이벤트를 사용한다. Phase 2에서는 이 동작을 보존해 **화면 렌더링 모듈로 먼저 분리**한다.

| 책임 | 소유자 |
|---|---|
| 목록 조회·생성·이름 변경·삭제, 선택 목록·편집 모드, 공통 툴바/다이얼로그 | AlbumDetailViewModel/Contract 및 AlbumDetail 화면 |
| 목록·카드 렌더링, 클릭/선택/뒤로가기 callback 호출 | subalbum:impl |
| SubAlbumList 직렬화 키 | subalbum:api |
| 두 impl을 연결하는 Composable slot 어댑터 | app |

1. 현재 app의 `AlbumDetailRoute/Screen`에 `subAlbumContent` Composable slot을 도입한다.
2. slot에는 `List<SubAlbumItem>`, 편집 모드, 선택 ID와 기존 동작 callback만 전달한다. AlbumDetail의 ViewModel이나 전체 MVI Contract를 넘기지 않는다.
3. app의 그래프 조립 코드가 slot에서 `subalbum:impl`의 `SubAlbumListScreen()`을 호출한다. AlbumDetail은 subalbum:api의 키만 사용하고 impl을 import하지 않는다.
4. AlbumDetail 내부 NavHost는 유지한다. 해당 destination의 콘텐츠가 주입된 slot을 실행하며, app이 바깥 NavHost에서 SubAlbum을 중복 등록하지 않는다.
5. #125에서 AlbumDetail을 모듈로 옮길 때 같은 slot을 유지한다. app은 두 impl에 의존하므로 연결이 가능하고, Feature 간 impl 의존은 생기지 않는다.

SubAlbum에 독립 MVI가 필요해지면 상태 소유권과 툴바 연동을 별도 설계한다. 이번 분리에서는 두 ViewModel이 같은 편집 상태를 복제하지 않는다.

### 2.5 데이터와 MVI의 유지 범위

- Phase 1 결정대로 DTO와 Service/DataSource는 `core:data`에 유지한다(PR #136).
- Auth의 `SignUpContract.mapToUser()`와 `SignUpRepository`가 DTO `User`를 공개하는 현재 경계는 유지한다. DTO를 숨길 때는 입력 모델과 매핑을 함께 바꾸는 별도 PR을 만든다.
- 상태 갱신 경쟁, Effect 수집 lifecycle, 오류 표현 일관성의 개선은 별도 PR로 분리한다. 이동 도중 확인된 회귀는 해당 PR에서 수정한다.
- MVI 테스트는 실제 상태·효과 동작을 검증한다. 파일 위치나 import를 그대로 따라가는 테스트를 추가하지 않는다.

## 3. PR 실행 순서와 완료 조건

기존의 'PR 13~18'은 계획상의 순번이다. **Phase 2는 준비 1개 + Feature 6개 = 7개 PR**로 진행한다. 각 작업은 자신의 새 브랜치와 PR을 사용하고, **모든 PR의 base는 develop**이다. 앞 PR이 병합된 뒤 최신 origin/develop에서 다음 브랜치를 만든다.

| 순서 | 이슈 / 새 브랜치 | 변경 | 완료 조건 |
|---|---|---|---|
| P0 준비 | #109 / `refactor/feature-migration-foundation` | 미커밋 계획 문서 4개 포함, 플러그인 Core 의존 보완, 기준 골든 패스 기록 | 기존 app 빌드·Lint 성공, Feature 없이도 기존 앱 동작 유지 |
| P1 Auth | #122 / `refactor/feature-auth` | api/impl 생성, Login/SignUp/Profile 키와 UI 이관, auth 그래프 함수 등록 | 로그인·자동 로그인·회원가입·Profile 진입/이탈 및 백스택 유지 |
| P2 Friend | #123 / `refactor/feature-friend` | friend 전체와 내부 그래프 이관 | 목록·검색·요청 송수신/응답 및 탭 전환 유지 |
| P3 SubAlbum | #124 / `refactor/feature-subalbum` | 키·stateless UI 이동, app slot 어댑터 도입 | 조회·선택·편집 취소·생성/삭제/이름 변경·PhotoList 이동 유지 |
| P4 AlbumDetail | #125 / `refactor/feature-album-detail` | 남은 상세 UI/VM/Contract·내부 그래프 이동, slot 유지 | 멤버·설정·대표 이미지·하단 탭·SubAlbum 연동 유지 |
| P5 Album | #126 / `refactor/feature-album` | Album/CreateAlbum과 키 이관 | 생성 후 목록 갱신, 상세·친구·Profile 이동 유지 |
| P6 Photo | #127 / `refactor/feature-photo` | PhotoList/Detail·BottomSheet 및 키 이동 | Paging·업로드/삭제·좋아요·댓글·설명·다운로드·몰입 모드 유지 |

### 3.1 PR 제목과 이슈 연결

| 순서 | PR 제목 | 본문 이슈 연결 |
|---|---|---|
| P0 | `refactor: Feature 분리 기반 정비` | `Refs #109` (상위 이슈 유지) |
| P1 | `refactor: :feature:auth 모듈 분리 (Login/SignUp/Profile)` | `Closes #122`, `Refs #109` |
| P2 | `refactor: :feature:friend 모듈 분리` | `Closes #123`, `Refs #109` |
| P3 | `refactor: :feature:subalbum 모듈 분리 및 화면 연결` | `Closes #124`, `Refs #109` |
| P4 | `refactor: :feature:album-detail 모듈 분리` | `Closes #125`, `Refs #109` |
| P5 | `refactor: :feature:album 모듈 분리` | `Closes #126`, `Refs #109` |
| P6 | `refactor: :feature:photo 모듈 분리` | `Closes #127`, `Refs #109` |

2026-10-12 확인한 저장소 기본 브랜치는 develop이다. 각 Feature PR의 `Closes` 문구로 병합 시 해당 하위 이슈가 종료되도록 연결한다. 실제 병합·검증 완료 및 이슈 상태를 확인하고 자동 종료되지 않으면 수동 정리한다. #109는 P6 병합 이후 Phase 2 종료 게이트까지 완료한 뒤 종료한다.

### 3.2 작업마다 반복할 흐름

1. **시작:** 앞 PR의 develop 병합을 확인한다. 로컬 변경을 보존한 상태에서 fetch하고 develop을 fast-forward 갱신한 뒤, 표의 이름으로 새 브랜치를 만든다.
2. **구현:** 해당 작업 범위만 변경한다. api와 impl은 같은 Feature PR에 포함하며, 다른 Feature 이동은 다음 PR로 둔다.
3. **검증:** 해당 모듈/app 빌드·Lint·단위 테스트 및 해당 화면 수동 회귀를 수행한다. 실패는 현재 브랜치에서 해결하고, 미확인 항목은 PR에 남긴다.
4. **PR 생성:** 브랜치를 push하고 `head=<작업 브랜치>`, `base=develop`으로 Draft PR을 만든다. 이슈·변경 범위·검증 결과를 본문에 기록한다. 기본 검증이 끝난 구현을 PR로 올리고, 남은 수동 확인은 Draft 상태에서 추적할 수 있다.
5. **리뷰:** 검증 게이트 충족 후 Ready for review로 전환한다. 리뷰 수정은 같은 브랜치/PR에서 이어서 진행하며, 수정 범위에 필요한 검증을 다시 실행한다.
6. **다음 작업:** PR 병합을 확인하고 링크/검증 결과/하위 이슈 상태를 #109에 기록한다. 다음 브랜치는 갱신된 develop에서 새로 만든다. 병합 전에는 다음 Feature 구현을 시작하지 않는다.

일반적인 다음 작업의 시작 명령은 다음과 같다. 작업 트리의 현재 작업을 먼저 커밋하거나 보존한 뒤 실행한다.

```bash
git fetch origin
git switch develop
git pull --ff-only origin develop
git switch -c refactor/feature-auth
```

예시의 브랜치 이름은 실제 단계에 맞춰 바꾼다. 기존 브랜치가 발견되면 소유 작업과 내용을 확인하고 충돌 없는 새 이름을 정한다. 진행 중인 PR에 develop이 변경되면 같은 브랜치에서 갱신하고 재검증한다.

### 3.3 최초 준비 PR의 문서 처리

2026-10-12 현재 develop에는 지난 계획 작업의 문서 4개가 미커밋 상태로 남아 있다.

- `docs/architecture/multi-module-migration-plan.md`
- `docs/architecture/phase1-core-migration-plan.md`
- `docs/architecture/build-logic-convention.md`
- `docs/architecture/phase2-feature-migration-plan.md` (신규)

P0 시작 때 이 변경들을 보존한 채 `refactor/feature-migration-foundation`으로 옮긴다. 현재 로컬/원격 develop의 HEAD가 같으므로 새 브랜치 생성 시 변경을 그대로 가져갈 수 있다. 작업 시작 당시 원격 상태를 다시 확인한다. 준비 PR에 문서 정리와 플러그인 보완을 별도 커밋으로 포함하고, 관계없는 파일은 커밋에 포함하지 않는다.

P0 검증은 app 빌드·Lint·기존 단위 테스트와 기준 동작 확인이다. 아직 Feature 모듈이 없으므로 Feature 빌드 명령을 사용하지 않는다.

### 3.4 PR 본문과 검증 기록

저장소 PR 형식인 `## Summary`, `## Changes`, `## Test plan`을 사용한다. 변경 내용은 한국어로 작성하고, 필요할 때만 `## 참고`를 추가한다. Test plan은 빌드/자동 테스트, Navigation·MVI, 기기 수동 회귀로 나누고 실제 확인한 항목에만 체크한다.

각 PR에 아래 내용을 포함한다.

- 파일/모듈 이동 범위와 이동 전후 경로 키·화면 소유권
- 추가·제거한 Gradle 의존성과 Hilt/serialization 변경
- 실행한 명령, 성공/실패 결과, 수동 검증 환경과 미확인 항목
- 해당 `Closes`/`Refs` 문구와 선행 병합 PR 링크

PR 생성 후 #109의 해당 단계에 PR 링크를 기록한다. 검증이 끝나지 않은 Draft나 미병합 PR은 단계 완료로 체크하지 않는다.

### 3.5 Phase 3 인계

P6 병합 후 Phase 2 종료 검증까지 완료하면 #110을 **별도 새 브랜치 `refactor/app-slimming`**, **별도 PR `refactor: app 모듈 슬림화 및 마이그레이션 마무리`**로 진행한다. base는 최신 develop이며, app의 불필요한 의존성/플러그인/리소스와 README·문서를 정리한다. Phase 3 작업은 Photo PR에 합치지 않는다.

각 Feature의 기본 커밋 단위:

1. `chore`: api/impl 빈 모듈 생성, settings/include 및 app 의존 연결. 키가 없는 빈 모듈도 빌드 가능하게 만든다.
2. `refactor`: 경로 키·UI 파일 이동, 패키지/import, 그래프 등록, SavedStateHandle 타입 갱신을 함께 수행한다. SubAlbum은 slot 연결도 이 커밋에 포함한다.
3. 필요한 실제 회귀 테스트와 동작 수정. 기존 API 동작은 바꾸지 않는다.

최초 실행 대상은 **P0 → #122**다. PR마다 빌드 가능한 단위를 유지하고, 이동만 완료한 채 Hilt 그래프/Navigation이 깨지는 중간 커밋을 남기지 않는다.

## 4. 검증 게이트

### 사전 기준 동작

- [ ] 기기/에뮬레이터, 앱 버전, 백엔드 환경과 테스트 계정 조건 기록
- [ ] 기존 골든 패스 6개와 Profile 진입 확인
- [ ] 기존 실패·미구현 기능은 별도 기록하여 이동으로 생긴 회귀와 구별

2026-10-12 사용자가 백엔드 배포 중단 상태를 확인하고 **빌드·Lint·자동 테스트와 가능한 기기 확인 후 병합하며 계속 진행**하도록 승인했다. 이 작업에서는 해당 기준을 병합 게이트로 사용한다. API 골든 패스는 미확인으로 명시해 배포 복구 후 검증 항목으로 남기며, 실행하지 않은 API 경로를 통과로 표시하지 않는다.

### 각 Feature PR

```bash
./gradlew :feature:<name>:api:assembleDebug :feature:<name>:impl:assembleDebug :app:assembleDebug :app:lintDebug
./gradlew :feature:<name>:impl:testDebugUnitTest :app:testDebugUnitTest
```

- [ ] 기존 runtime용 Gradle 모듈/라이브러리 의존성이 실제 import를 충족
- [ ] core→feature / impl→타 feature impl 의존 없음
- [ ] Hilt 생성, `SavedStateHandle.toRoute`, nullable Profile 인자와 백스택 정상
- [ ] 해당 Feature 진입/이탈·뒤로가기·재진입·회전·백그라운드 복귀 확인
- [ ] 기존 설치 버전에서 업데이트 후 저장 백스택·프로세스 재생성 확인
- [ ] 로그인 성공/실패·자동 로그인·회원가입 검증·Effect 등의 실제 동작 테스트 추가 (Auth부터)
- [ ] 변경으로 생긴 오류와 기존 실패를 PR의 검증 결과에 구분해서 기록

재개 전 자동 테스트는 산술/기본 계측 예제뿐이었다. Auth부터 실제 상태·효과와 Navigation 회귀 테스트를 추가한다. 테스트 task 성공만으로 MVI 회귀가 검증됐다고 판단하지 않는다. `ktlintCheck`/`detekt`는 현재 구성돼 있지 않으므로 필수 명령으로 쓰지 않는다.

### Phase 2 종료 / Phase 3 인계

```bash
./gradlew clean :core:model:assemble assembleDebug :app:lintDebug :app:testDebugUnitTest
```

- [ ] app + Core 11 + Feature 12 = 24개 include 확인
- [ ] `app`의 모든 Feature Screen/ViewModel/Contract 및 내부 화면 그래프가 이관됨(Profile 포함)
- [ ] 경로 키 이동 완료, 남은 `MomentwoDestination` 및 오래된 UI import 제거
- [ ] 골든 패스 6개 + Profile + 중첩 NavHost 연동 확인
- [ ] #122~#127 종료 후 #109 종료
- [ ] #110에 app의 남은 의존성/플러그인/리소스·문서 정리 목록 인계

app에는 Application/Activity, 루트 NavHost와 navigation actions, Theme 적용, Feature slot 조립 코드만 남길 수 있다. Room/Retrofit/DataStore 등 app의 불필요한 직접 의존성과 플러그인 제거는 Phase 3에서 한다.

## 5. 롤백

회귀는 우선 현재 Feature PR에서 수정한다. 이미 병합한 작업을 되돌려야 하면 마지막 Feature부터 역순으로 revert한다. 키·화면·import·app 의존·slot 연결을 한 단위로 되돌리며, 런타임 자료 없이 새 Route 타입과 기존 백스택이 호환된다고 가정하지 않는다.

## 6. 실행 기록

### 2026-10-12 — P0 준비 작업 착수

- 브랜치: `refactor/feature-migration-foundation`, base: `origin/develop` / `c67f368`.
- 기존 미커밋 문서 4개를 보존해 준비 브랜치로 이동했다.
- Feature 플러그인에 core:ui/designsystem/domain/navigation 기본 의존성을 추가했다. 현재 app에는 Feature 플러그인을 적용하지 않으므로 기존 화면 실행 경로는 바뀌지 않는다.
- Android CLI 환경과 연결 기기 확인: Samsung SM_N976N, Android API 31. 에뮬레이터 목록은 비어 있다.
- 생성된 BuildConfig의 실제 BASE_URL 연결 확인: `ConnectionRefusedError` (errno 61). 주소나 인증 정보를 문서에 기록하지 않는다.
- 빌드·Lint·단위 테스트 및 가능한 기기 화면 확인 결과는 준비 PR에 기록한다. 로그인 이후 API 골든 패스는 백엔드 연결이 복구되기 전까지 미확인이다.
- 사용자가 백엔드 배포 중단을 확인하고 빌드·Lint·자동 테스트 및 가능한 기기 확인을 기준으로 병합/계속 진행하도록 승인했다. 이 기준을 이후 모든 PR에 적용한다.
- 준비 app 빌드·Lint·단위 테스트 성공(3m 4s). 저장소에 포함하지 않는 임시 Feature 모듈에 플러그인을 실제 적용해 Core API 참조와 AAR 빌드도 확인했다(5s).
- 기존 앱의 debug 서명 불일치를 피하기 위해 임시 Gradle init script로 `cord.eoeo.momentwo.migrationcheck` 패키지를 별도 빌드·설치했다(11s). 원본 앱 데이터는 변경하지 않았다.
- 별도 설치 앱의 로그인 화면 및 회원가입 진입을 Android CLI layout으로 확인했다. API 연결을 사용하는 경로는 사용자 승인에 따라 미확인으로 유지한다.
- 준비 PR: [#143](https://github.com/Momentwo/Momentwo/pull/143). 이후 진행/병합 상태와 검증 결과는 #109에 기록한다.

### 2026-10-12 — P1 Auth 구현 및 검증

- 준비 PR #143 병합 후 최신 develop에서 `refactor/feature-auth`를 생성했다. 준비 PR 생성 후 추가 push를 거부하는 저장소 규칙에 따라 마지막 검증 기록 커밋은 Auth 브랜치에 보존했다. 이후 PR은 모든 구현·검증·기록을 마친 뒤 최초 push한다.
- auth:api/impl 두 모듈을 추가했다. Login/SignUp/Profile 키는 api로, UI 14개 파일은 impl로 이동했다. app은 공개 `authScreens()`만 호출하며 내부 화면·VM·Contract는 internal로 제한했다.
- `@SerialName`으로 기존 경로의 직렬화 이름을 유지했다. nullable Profile 인자와 `SavedStateHandle.toRoute`도 새 api 타입으로 갱신했다.
- Auth API 테스트 2개 및 ViewModel 동작 테스트 8개 통과: 로그인 성공·실패/세션 저장, 자동 로그인, 회원가입 이동·입력 검증·정규화·요청 매핑.
- SM_N976N/API 31 별도 테스트 패키지에서 실제 app 그래프/Hilt를 사용하는 계측 테스트 2개 통과: 로그인→회원가입→뒤로가기, nullable Profile 진입→뒤로가기(최종 2m 12s).
- 실제 API 응답·로그인 이후 데이터 동작은 백엔드 배포 중단으로 미확인이다. 사용자가 승인한 자동/기기 확인 기준으로 진행한다.

### 2026-10-12 — P2 Friend 구현 및 검증

- Auth PR #144 병합 후 develop/cbea6e3에서 `refactor/feature-friend`를 생성했다.
- friend:api/impl을 추가하고 UI 11개 파일, MVI와 내부 목록/요청 그래프를 이관했다. FriendDestination은 기존 직렬화 이름을 유지하며 app은 friendScreen()으로 연결한다.
- 내부 UI·VM·Contract·탭 경로/Actions는 internal로 제한했다. Paging/Coil/icons와 실제 Core 의존성을 명시했다.
- friend api/impl 및 app 빌드·Lint, API 직렬화 단위 테스트 1개, app 단위 테스트 통과(최종 22s).
- SM_N976N/API 31 별도 앱 패키지에서 기기 테스트 3개 통과(2m 41s): 로그인/회원가입/뒤로가기, nullable Profile, Friend 진입/요청 탭/뒤로가기.
- 자동 잠금으로 처음 UI 계층을 찾지 못했던 테스트는 테스트 Activity 표시·화면 유지와 창 포커스 대기로 보완했다. 제품 코드/기기 보안 설정은 변경하지 않았다.
- 실제 친구 API와 기존 배포 앱 업데이트/저장 백스택 복원은 미확인으로 유지한다.

### 2026-10-12 — P3 SubAlbum 구현 및 검증

- Friend PR #145 병합 후 develop/add5c0b에서 `refactor/feature-subalbum`를 생성했다.
- subalbum:api/impl을 추가하고 목록/카드 2개 파일과 SubAlbumList 키를 이관했다. 키의 기존 직렬화 이름을 유지했다.
- AlbumDetail의 MVI·상태 소유권을 유지한 채 상태 조각/동작 콜백을 받는 Composable 슬롯과 app 어댑터를 도입했다. 상세 내부 NavHost는 SubAlbum API만 참조한다.
- subalbum api/impl 및 app 빌드·Lint·API 직렬화 테스트 1개/app 단위 테스트 통과(10s), 계측 테스트 추가 후 Lint도 통과(2s).
- 실제 API 및 기존 배포 앱 업데이트/저장 백스택 복원은 배포 복구 후 확인 항목으로 유지한다.
- SM_N976N/API 31의 기기 테스트 5개 통과(2m 34s): 기존 Auth/Friend 3개, 상세 진입·멤버/SubAlbum 탭 복귀, 어댑터 일반 클릭·편집 선택 콜백.

### 2026-10-12 — P4 AlbumDetail 구현 및 검증

- SubAlbum PR #146 병합 후 develop/0640da6에서 `refactor/feature-album-detail`를 생성했다.
- album-detail:api/impl 추가, 상세·멤버·설정·이미지 변경의 UI/MVI/내부 그래프 12개 파일과 루트 키 이관. app은 공개 albumDetailScreen()과 SubAlbum 슬롯만 조립한다.
- 루트 키 및 내부 멤버/설정/이미지 변경 키의 기존 직렬화 이름을 유지했다. SubAlbum 구현 직접 의존 없이 API와 app 슬롯을 사용한다.
- api/impl 및 app 빌드·Lint·단위 테스트 통과(최종 6s). 루트 인자 왕복과 내부 키 3개 호환성 테스트 각 1개 통과.
- 실제 API 및 배포 앱 업데이트/저장 백스택 복원은 미확인으로 유지한다.
- SM_N976N/API 31 기기 테스트 5개 통과(2m 39s): 상세 SavedStateHandle 인자 복원·멤버/설정/SubAlbum 탭 전환, 슬롯 콜백, 기존 Auth/Friend 회귀.

### 2026-10-12 — P5 Album 구현 및 검증

- AlbumDetail PR #147 병합 후 develop/09a7bad에서 `refactor/feature-album`를 생성했다.
- album:api/impl 추가, Album/CreateAlbum UI/MVI 8개 파일과 경로 키 이관. app은 공개 albumScreens()와 기존 callback으로 연결한다.
- 두 키의 기존 직렬화 이름 유지. API 직렬화·JSON 왕복 테스트 2개 통과.
- api/impl 및 app 빌드·Lint·단위 테스트 통과(17s). 실제 API 및 배포 앱 업데이트/저장 백스택 복원은 미확인으로 유지한다.
- SM_N976N/API 31 기기 테스트 6개 통과(2m 44s): 기존 5개와 Album→CreateAlbum→목록→루트 복귀.

### 2026-10-12 — P6 Photo 구현 및 검증

- Album PR #148 병합 후 develop/1f8411e에서 `refactor/feature-photo`를 생성했다.
- photo:api/impl 추가, 목록·상세·댓글/설명 BottomSheet UI/MVI 11개 파일과 두 경로 키 이관. app은 공개 photoScreens()로 이미지 로더·insets controller·콜백을 전달한다.
- 두 키의 기존 직렬화 이름과 모든 인자 유지. API 직렬화·JSON 왕복 테스트 2개 통과.
- api/impl 및 app 빌드·Lint·단위 테스트 통과(14s). app main 소스에는 앱 시작/루트 조립 파일 6개만 남았다.
- 실제 API 및 배포 앱 업데이트/저장 백스택 복원은 미확인으로 유지한다.
- SM_N976N/API 31 기기 테스트 7개 통과(2m 46s): 기존 6개와 PhotoList/PhotoDetail 인자 복원·몰입 모드 전환·목록/루트 복귀.

### 2026-10-12 — Phase 3 app 슬림화 및 최종 검증

- Photo PR #149 병합 및 Phase 2 종료 검증 후 develop/b913d8d에서 별도 `refactor/app-slimming`을 생성했다.
- app의 Room/serialization 플러그인과 BuildConfig 생성 설정, 불필요한 Core/데이터·네트워크·Paging·Lifecycle·아이콘·Coil Compose 직접 의존 제거. 실제 루트 조립에 필요한 의존과 coroutines-core 직접 선언만 유지했다.
- app main 소스 6개는 앱 시작·루트 Navigation·슬롯 조립만 담당한다. 빈 legacy MomentwoDestination과 core:navigation serialization 설정, 미사용 색상·예제 테스트를 제거했다. core:navigation은 기존 모듈 계약을 유지한다.
- README/전체 계획/convention 가이드를 현재 코드로 갱신하고 module-graph.md에 실제 실행·테스트 프로젝트 의존을 기록했다.
- 변경 후 app 빌드·Lint 통과(13s). 최종 clean 전체 빌드·app Lint·전체 Android 단위 테스트 통과(39s), 실제 Feature 테스트 18개 통과. app의 산술 예제 대신 실제 그래프 계측 테스트를 사용한다.
- 금지 프로젝트 의존/타 impl 및 app import 없음, 24개 include/Feature ViewModel 9개/app main 파일 6개 확인.
- 백엔드 API와 정식 업데이트·프로세스 복원·회전/백그라운드 통합 회귀는 후속 #150에서 확인한다.
- 최종 SM_N976N/API 31 별도 패키지 기기 테스트 7개 통과(2m 37s). app 의존/플러그인 제거 후에도 실제 Hilt/Navigation/슬롯/몰입 모드와 백스택 회귀를 확인했다.
