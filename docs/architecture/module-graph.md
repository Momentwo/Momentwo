# Momentwo 실제 모듈 의존성

2026-10-19 #160 기준 settings.gradle.kts 및 각 모듈 build.gradle.kts, Feature convention plugin의 프로젝트 의존을 정리했다. 총 23개 include 모듈이며 build-logic은 별도 included build다. 아래 표는 외부 라이브러리를 제외한다. 단위·기기 테스트 전용 의존을 실행 의존과 구별한다.

| 모듈 | 실행 프로젝트 의존 (convention 기본값 포함) | 테스트 프로젝트 의존 |
|---|---|---|
| `:app` | `:feature:auth:api`, `:feature:auth:impl`, `:feature:friend:api`, `:feature:friend:impl`, `:feature:subalbum:impl`, `:feature:album-detail:api`, `:feature:album-detail:impl`, `:feature:album:api`, `:feature:album:impl`, `:feature:photo:api`, `:feature:photo:impl`, `:core:designsystem`, `:core:model`, `:core:common` | `:core:common`, `:core:ui`, `:core:data`, `:core:database`, `:core:datastore`, `:core:network` |
| `:core:designsystem` | 없음 | 없음 |
| `:core:model` | 없음 | 없음 |
| `:core:common` | 없음 | `:core:testing` |
| `:core:ui` | `:core:common`, `:core:designsystem`, `:core:model` | `:core:testing` |
| `:core:datastore` | `:core:common`, `:core:model` | `:core:testing` |
| `:core:network` | `:core:datastore`, `:core:model` | 없음 |
| `:core:database` | `:core:model` | 없음 |
| `:core:data` | `:core:common`, `:core:model`, `:core:network`, `:core:database`, `:core:datastore` | `:core:testing` |
| `:core:domain` | `:core:common`, `:core:data`, `:core:model`, `:core:datastore` | `:core:testing` |
| `:core:testing` | 없음 | 없음 |
| `:feature:auth:api` | 없음 | 없음 |
| `:feature:auth:impl` | `:feature:auth:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain` | `:core:testing`, `:core:datastore` |
| `:feature:friend:api` | 없음 | 없음 |
| `:feature:friend:impl` | `:feature:friend:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain` | `:core:testing` |
| `:feature:subalbum:api` | 없음 | 없음 |
| `:feature:subalbum:impl` | `:feature:subalbum:api`, `:core:common`, `:core:model`, `:core:ui`, `:core:designsystem`, `:core:domain` | 없음 |
| `:feature:album-detail:api` | 없음 | 없음 |
| `:feature:album-detail:impl` | `:feature:subalbum:api`, `:feature:album-detail:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain` | `:core:testing` |
| `:feature:album:api` | 없음 | 없음 |
| `:feature:album:impl` | `:feature:album:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain` | `:core:testing` |
| `:feature:photo:api` | 없음 | 없음 |
| `:feature:photo:impl` | `:feature:photo:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain` | `:core:testing` |

모든 Feature API는 현재 Kotlin serialization 라이브러리만 사용하며 Core/Compose/Repository/다른 impl에 의존하지 않는다. Kotlin serialization compiler plugin은 키를 선언하는 API와 내부 키를 선언하는 album-detail:impl에 적용한다.

## 연결 규칙

- app은 경로 키를 쓰는 API 5개와 impl 6개를 직접 사용한다. subalbum:api는 album-detail이 내부 목적지로 사용한다.
- 화면 이동은 app callback으로 연결한다. 서로 다른 Feature 사이의 유일한 프로젝트 의존은 album-detail:impl → subalbum:api이다.
- SubAlbum의 상태·MVI·CRUD는 AlbumDetail이 소유한다. app의 SubAlbumContentAdapter가 상태 조각/콜백을 실제 SubAlbumListScreen에 전달하며 두 impl이 서로 import하지 않는다.
- Feature→app, Core→Feature, impl→타 Feature impl, api→impl/data 의존은 금지한다.
- 이 규칙과 빌드 맥락별 의존 사이클은 `./gradlew checkArchitecture`로 검사한다. [경계·사이클 검사 및 CI](module-boundaries.md)를 따른다.
- #160에서 공용 소스가 없는 core:navigation과 Feature plugin 기본 의존을 제거했다. 경로 키는 Feature API, 루트 그래프는 app이 소유한다. 기존 @SerialName 문자열은 저장 경로 호환성을 위해 유지한다.
- core:testing은 MainDispatcherRule과 범용 응답 제어·Effect 기록 도구를 제공한다. Fake 구현은 사용하는 Feature의 테스트 소스에만 둔다.

## 앱과 MVI

app main에는 MomentwoApplication, MainActivity, MomentwoApp, MomentwoNavGraph, MomentwoNavigationActions, SubAlbumContentAdapter의 6개 파일만 남아 있다. 기존 9개 ViewModel에 로컬 라이브러리·앨범·사진과 선택 계정·복사 화면의 ViewModel을 추가했다. SubAlbum에는 ViewModel을 중복 생성하지 않는다.

#155에서 9개 ViewModel을 ReducerViewModel과 순수 Reducer로 전환했다. StateFlow·Channel(BUFFERED), STARTED 단일 수집과 ScreenState/Paging 표시 규칙은 [MVI 구현 가이드](mvi-guide.md)를 따른다. Orbit·공개 DTO 경계·Profile 기능은 별도 이슈다. app은 core:common의 BackgroundTasksInitializer로 시작 훅만 호출한다. 동기화 구현과 서비스는 core:data에 있으며 app의 Core 데이터/UI 추가 참조는 기기 테스트 전용이다. 실행 모듈 경계를 유지한다.

검증 결과는 [Phase 2 실행 기록](phase2-feature-migration-plan.md), 실제 API 및 업데이트/복원 회귀는 [#150](https://github.com/Momentwo/Momentwo/issues/150)을 따른다.


## Offline-First 저장 책임

- `core:model`: Device/Account 라이브러리, UUID 콘텐츠, 계정 ID와 동기화 표시 모델.
- `core:database`: Room v4의 영속 원본·계정 캐시·작업/연결/파일 참조와 v1→v2→v3→v4 migration.
- `core:data`: 로컬 CRUD·영속 파일, 세션에 고정된 계정 조회, 선택 복사·동기화와 JobScheduler 서비스.
- `core:datastore`: 원자적 세션과 화면 편집 초안. 로그인·로그아웃은 로컬 콘텐츠 수명과 분리한다.
- `feature:album`: 기본 로컬 앨범, 선택 계정과 복사·동기화 화면. `album-detail`·`photo`는 UUID 기반 로컬/계정 콘텐츠를 렌더링한다.

기존 서버 ID 경로는 유지하며 로컬 UUID 경로를 추가했다. Feature api/impl 경계는 유지한다. Offline-First 도입 당시 24개 모듈에서 #160의 빈 navigation 제거 후 23개 모듈이 됐다. [Offline-First 가이드](offline-first-guide.md)를 참고한다.


## #160 검증 기준 (2026-10-19)

공용 소스가 없는 navigation 모듈·settings 등록·Feature 기본 의존을 제거했다. app1+Core10+Feature12=23개이며 기존 Destination 식별자와 app 그래프/콜백은 변경하지 않았다.
로컬 checkArchitecture 실측은23모듈/180맥락/651간선·위반0이다. 삭제 전24/188/695에서 프로젝트와 실제 의존이 줄었으며 검사 정책이나 모듈 수 상수는 변경하지 않았다.
기존 Feature API/AlbumDetail 내부 탭 경로 테스트10건을 새로 실행해 실패·오류·skip0, 별도 앱 Debug/Hilt 빌드를2분21초(실행509/재사용3 task)에 통과했다.
로컬은 단일 Gradle·워커1·최대 힙2GB·Kotlin in-process, 테스트 worker256MB로 제한했고 전체 clean 빌드/Lint·맥 에뮬레이터는 실행하지 않았다.
SM_N976N/API31 별도 앱에 새 Debug만 설치하고 기존 테스트 APK로 Navigation7건을12.224초에 통과했다. 원본 앱 설치 정보와 테스트 APK는 동일하며 태블릿에는 설치하지 않았다.
원격 전체 검증 결과는 #160/연결 PR에 기록한다. 과거24개 구성·검증 이력은 해당 시점의 기록으로 보존한다.
