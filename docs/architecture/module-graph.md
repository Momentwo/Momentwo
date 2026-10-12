# Momentwo 실제 모듈 의존성

2026-10-12 기준 settings.gradle.kts 및 각 모듈 build.gradle.kts, Feature convention plugin의 프로젝트 의존을 정리했다. 총 24개 include 모듈이며 build-logic은 별도 included build다. 아래 표는 외부 라이브러리를 제외한다. Auth 테스트 전용 datastore/testing 의존을 runtime 의존과 구별한다.

| 모듈 | 실행 프로젝트 의존 (convention 기본값 포함) | 테스트 프로젝트 의존 |
|---|---|---|
| `:app` | `:feature:auth:api`, `:feature:auth:impl`, `:feature:friend:api`, `:feature:friend:impl`, `:feature:subalbum:impl`, `:feature:album-detail:api`, `:feature:album-detail:impl`, `:feature:album:api`, `:feature:album:impl`, `:feature:photo:api`, `:feature:photo:impl`, `:core:designsystem`, `:core:model` | 없음 |
| `:core:designsystem` | 없음 | 없음 |
| `:core:model` | 없음 | 없음 |
| `:core:common` | 없음 | 없음 |
| `:core:ui` | `:core:designsystem`, `:core:model` | 없음 |
| `:core:datastore` | 없음 | 없음 |
| `:core:network` | `:core:datastore` | 없음 |
| `:core:database` | `:core:model` | 없음 |
| `:core:data` | `:core:common`, `:core:model`, `:core:network`, `:core:database`, `:core:datastore` | 없음 |
| `:core:domain` | `:core:data`, `:core:model`, `:core:datastore` | 없음 |
| `:core:navigation` | 없음 | 없음 |
| `:core:testing` | 없음 | 없음 |
| `:feature:auth:api` | 없음 | 없음 |
| `:feature:auth:impl` | `:feature:auth:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | `:core:testing`, `:core:datastore` |
| `:feature:friend:api` | 없음 | 없음 |
| `:feature:friend:impl` | `:feature:friend:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | 없음 |
| `:feature:subalbum:api` | 없음 | 없음 |
| `:feature:subalbum:impl` | `:feature:subalbum:api`, `:core:common`, `:core:model`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | 없음 |
| `:feature:album-detail:api` | 없음 | 없음 |
| `:feature:album-detail:impl` | `:feature:subalbum:api`, `:feature:album-detail:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | 없음 |
| `:feature:album:api` | 없음 | 없음 |
| `:feature:album:impl` | `:feature:album:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | 없음 |
| `:feature:photo:api` | 없음 | 없음 |
| `:feature:photo:impl` | `:feature:photo:api`, `:core:common`, `:core:model`, `:core:data`, `:core:ui`, `:core:designsystem`, `:core:domain`, `:core:navigation` | 없음 |

모든 Feature API는 현재 Kotlin serialization 라이브러리만 사용하며 Core/Compose/Repository/다른 impl에 의존하지 않는다. Kotlin serialization compiler plugin은 키를 선언하는 API와 내부 키를 선언하는 album-detail:impl에 적용한다.

## 연결 규칙

- app은 경로 키를 쓰는 API 5개와 impl 6개를 직접 사용한다. subalbum:api는 album-detail이 내부 목적지로 사용한다.
- 화면 이동은 app callback으로 연결한다. 서로 다른 Feature 사이의 유일한 프로젝트 의존은 album-detail:impl → subalbum:api이다.
- SubAlbum의 상태·MVI·CRUD는 AlbumDetail이 소유한다. app의 SubAlbumContentAdapter가 상태 조각/콜백을 실제 SubAlbumListScreen에 전달하며 두 impl이 서로 import하지 않는다.
- Feature→app, Core→Feature, impl→타 Feature impl, api→impl/data 의존은 금지한다.
- core:navigation은 통합 키 이관 후 현재 공용 소스가 없다. 기존 모듈 계약과 Feature plugin 기본 의존을 유지한다.
- core:testing은 MainDispatcherRule을 제공한다. Fake 구현은 사용하는 Feature의 테스트 소스에만 둔다.

## 앱과 MVI

app main에는 MomentwoApplication, MainActivity, MomentwoApp, MomentwoNavGraph, MomentwoNavigationActions, SubAlbumContentAdapter의 6개 파일만 남아 있다. ViewModel 9개는 auth(3), friend(1), album-detail(1), album(2), photo(2)가 소유하고 SubAlbum에는 ViewModel을 중복 생성하지 않는다.

기존 BaseViewModel의 StateFlow/SharedFlow/Channel과 lifecycle 상태 수집을 유지했다. MVI의 동시 상태 갱신·Effect lifecycle 개선, Orbit 도입, 공개 DTO 경계 정리와 미완성 Profile 기능은 이번 모듈 이동 범위에 포함하지 않는다.

검증 결과는 [Phase 2 실행 기록](phase2-feature-migration-plan.md), 실제 API 및 업데이트/복원 회귀는 [#150](https://github.com/Momentwo/Momentwo/issues/150)을 따른다.
