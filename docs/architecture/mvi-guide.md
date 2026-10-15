# Momentwo MVI 구현 가이드

#155에서 기존 ViewModel 9개를 `ReducerViewModel`로 전환했다. 구현·검증 기록은 [MVI 개선 계획](mvi-improvement-plan.md)에 남긴다. 이 문서는 새 화면과 유지보수에 적용할 규칙이다.

## 상태 변경 흐름과 파일 책임

```text
UI Event → ViewModel → Action → 순수 Reducer → StateFlow → UI
                 └→ 비동기 요청 결과 / Channel Effect → UI
```

| 파일 | 책임 |
|---|---|
| State / Content | 화면 상태와 상호작용 데이터. 성공 Content는 필수 조회 결과를 소유 |
| Event | 클릭·입력·탭 전환 등 UI 의도 |
| Action | 요청 시작·결과·입력 변경 등 Reducer에 필요한 최소 입력 |
| Effect | 화면 이동·Snackbar·Paging refresh·스크롤 등 일회성 명령 |
| Reducer | `reduce(state, action)`으로 최신 State를 순수하게 변환 |
| ViewModel | 요청 인자 스냅샷, 업무 순서, 오류 로깅·변환, Action·Effect 발행 |

`ReducerViewModel(initialState, reducer)`는 생성자에서 초기 상태를 받고 `onEvent()`를 Main에서 처리한다. Action은 추가 coroutine 없이 `StateFlow.update`로 반영한다. Reducer는 재실행돼도 같은 결과를 내야 하므로 IO·시간 조회·Effect 발행·입력 변경을 금지한다. 컬렉션은 새 값으로 복사한다.

AlbumDetail·PhotoDetail은 역할별 Reducer가 하나의 루트 State를 변환한다. 현재 화면과 탭 캐시를 별도로 저장하지 않는다. Profile처럼 상태 변경이 없는 화면은 Action을 `Nothing`으로 두며 불필요한 Action을 만들지 않는다. SignUp 검증과 전송 매핑은 별도 파일로 둔다. 업무 흐름을 별도 Executor/Middleware로 옮기지 않는다.

## 최초 데이터와 새로고침

`core:common`의 `ScreenState<T>`는 `Loading / Success(content, refresh) / Error(UiError)`다. Success만 non-null Content를 소유한다. 공통 sealed 구현은 Core의 같은 모듈·패키지에 두고 Feature는 Content를 제네릭 인자로 전달한다. `when`은 `else` 없이 모든 상태를 처리한다.

| 상황 | 상태 |
|---|---|
| 최초 조회·최초 오류 재시도 | Loading → 필수 조회 모두 성공하면 Success, 실패하면 Error |
| 성공 후 재조회 | Success(기존 Content, RefreshState.Loading) |
| 재조회 성공 | Success(새 Content, Idle), 최신 입력·선택 보존 |
| 재조회 실패 | Success(기존 Content, RefreshState.Error) |
| 쓰기 진행·실패 | 본문 유지, 작업·대상별 pending과 쓰기 오류만 변경 |

빈 목록은 `Success(emptyList())`에서 안내한다. Loading/Error에는 과거 데이터·가짜 권한·임시 count를 넣지 않는다. Content에는 DTO·Repository·Job·Compose 상태를 넣지 않는다. 선택·입력·다이얼로그와 pending은 상호작용 상태에 둔다. Login·SignUp·CreateAlbum은 폼 상태를 유지한다.

AlbumDetail은 현재 탭의 권한과 필수 목록을 함께 확보한 뒤 하나의 Loaded Action을 전달한다. 설정 탭에는 경로 헤더와 조회된 권한을 사용한다. 다른 탭 응답은 해당 탭만 갱신한다. 성공 탭 복귀 시 캐시를 즉시 표시하며 필요한 경우 refresh한다. 서로 배타적인 다이얼로그는 화면 전용 sealed 타입으로 표현한다.

Route의 `LaunchedEffect(viewModel)`에서 멱등 `initialize()`를 호출한다. 자식 화면의 쓰기로 목록이 무효화되는 화면은 `LifecycleStartEffect`에서 초기화/복귀 확인을 호출하되 dirty일 때만 재조회한다. Effect 수집이나 재구성을 초기 조회 트리거로 사용하지 않는다. Loading/Error에서도 뒤로가기·탭은 유지한다. 검색·초대·댓글은 해당 UI를 열 때 조회한다.

`UiError`는 Network/Server/Unauthorized/Forbidden/NotFound/Unknown이다. UI 리소스로 메시지를 표시하며 원본 예외는 ViewModel 로그에 남긴다. Network/Server와 일반 fallback에는 수동 재시도를 제공하고 인증·권한·대상 없음에는 무조건 재시도를 제공하지 않는다. 같은 조회 오류를 본문과 Snackbar에 중복 표시하지 않는다. 쓰기 실패는 초안을 유지하고 Snackbar를 사용한다. 자동 재시도·인증 복구는 이번 기반에 없다.

## 취소, 중복과 오래된 응답

- suspend 작업은 `runSuspendCatching`으로 감싼다. 실행 전후 `ensureActive()`를 확인하고 `CancellationException`과 JVM `Error`를 다시 던진다.
- 중첩 `Result`는 `getOrThrow()`로 해제한다. `Result.failure(CancellationException)`을 일반 예외로 감싸거나 UI 오류로 변환하지 않는다. 연속 원격 작업은 각 경계에서도 활성 상태를 확인한다.
- ViewModel별 `RequestController`에 기능/대상 ID 기반 키를 등록한다. 조회는 LatestWins, 같은 쓰기는 IgnoreWhileRunning이다. LAZY 예약 단계도 중복 차단에 포함한다.
- 새 요청을 등록한 후 이전 요청을 취소한다. 결과 콜백 직전 활성 상태·요청 ID를 검사하며 이전 정리는 새 pending을 해제하지 못한다. ViewModel 해제 시 등록을 닫고 Effect 채널도 취소한다.
- 요청 부분만 예외를 잡는다. 성공/실패 콜백 자체의 오류를 API 실패로 다시 처리하지 않는다. 취소 시 결과 Action·사용자 Effect를 보내지 않고 현재 요청의 pending/등록만 정리한다.
- 요청 인자만 캡처한다. Reducer는 최신 State에 결과를 반영한다. 대상 ID·입력 revision·다이얼로그 session으로 새 입력/선택/다른 편집 화면을 보호한다.
- 좋아요 실패는 해당 버전의 좋아요 필드만 롤백한다. 서버 ACK 이후 로컬 갱신 실패는 롤백하지 않는다. 사진 삭제의 `CommittedWriteException`은 서버 반영과 로컬 실패를 구분한다. 설명 저장 후 재조회 실패도 성공한 쓰기를 실패로 보고하지 않고 초안을 유지한다.

## Paging 표시와 저장

Paging의 `LoadState`가 원본이다. Route에서 먼저 `collectAsLazyPagingItems()`를 호출하고 `rememberPagingScreenState()`로 본문 상태를 파생한다. Loading/Error로 본문을 숨겨도 수집은 유지한다. 같은 LoadState를 ViewModel에 다시 저장하지 않는다.

- 로컬 항목이 있으면 콘텐츠를 먼저 표시하고 원격 refresh 진행/실패를 함께 표시한다.
- 항목과 최초 성공 결과가 없으면 초기 Loading/Error를 표시한다. 정상 완료된 빈 결과는 빈 Success다.
- 성공한 빈 결과의 refresh도 본문을 유지한다. 이 이력은 LazyPagingItems/조회 조건별로 구분한다.
- append 진행/실패는 목록 하단 `PagingAppendContent`에서 처리한다. 고정 시간으로 새로고침을 표시하지 않는다.
- Friend/Photo의 마지막 페이지도 저장한 뒤 종료한다. Photo는 Pager마다 독립 Mediator를 만들고 사진/커서를 앨범·서브앨범 범위의 Room 트랜잭션에 저장한다. 취소되면 함께 롤백한다. DB 스키마와 TTL 정책은 유지한다. 동기화 전면 변경은 #162다.

## Effect와 시스템 바

Effect는 명시적 `Channel(Channel.BUFFERED)`를 `receiveAsFlow()`로 노출한다. 채널이 가득 차면 suspend하며 임의 drop 정책은 없다. 활성 UI 수집자는 하나만 둔다. `CollectEffect`는 STARTED에서 수집하고 `rememberUpdatedState`로 최신 핸들러를 사용한다.

Navigation은 즉시 처리한다. Snackbar·애니메이션은 핸들러의 STARTED scope 안에서 `launch`해 다른 Effect 처리를 막지 않는다. 한 업무의 Effect는 같은 coroutine에서 순서대로 `emitEffect()`한다. 화면 초기화는 별도 처리한다.

채널은 영속 큐가 아니다. 수신 전 버퍼에 남은 항목은 복귀 시 수집할 수 있지만 수신 직후 수집 coroutine이 취소되면 처리 완료를 보장하지 못한다. 프로세스 사망 시 Effect와 메모리 상태는 복원되지 않는다. exactly-once/ack/자동 replay를 전제로 서버 업무를 설계하지 않는다. SavedStateHandle은 명시적으로 저장한 경로·폼 값만 복원한다.

시스템 바는 지속 State를 `SystemBarsEffect`로 적용한다. RESUMED에서 현재 값을 적용하고 PAUSE·화면 제거에서 표시 상태로 복원한다. 복귀 시 현재 State를 다시 적용한다. Loading/Error에서는 기본 탐색 UI를 보여준다.

## 검증과 경계

가상 시간·제어 가능한 Fake로 부분 실패, 취소, 응답 역전, 중복, 최신 입력/선택 보존과 Effect 순서를 검증한다. 실제 기기에서는 Hilt 경로·UI 상태·Paging 수집·STARTED 복귀·시스템 바와 실제 Room 트랜잭션을 검증한다. 영향 모듈 테스트/빌드/Lint 후 전체 검증을 수행한다.

백엔드 중단으로 실제 API 골든 패스 및 정식 업데이트·프로세스 복원 회귀는 [#150](https://github.com/Momentwo/Momentwo/issues/150)에 남긴다. 별도 applicationId의 테스트 앱으로 원래 앱을 보존하며 임시 Gradle 설정은 제품 코드에 커밋하지 않는다. API 직렬화·모듈 경계와 라이브러리 버전은 유지한다. DTO #157, Orbit #159, Profile #161, 디자인 시스템 #163과 HTTP 인증 재설계는 별도 작업이다. 후속 Offline-First #162와 회귀 테스트 #156의 구현·검증 결과는 각 가이드에서 관리한다.

## 설계 참고

- [Kotlin sealed 타입의 모듈·패키지 제한](https://kotlinlang.org/docs/sealed-classes.html)
- [Android 상태 생성과 초기화](https://developer.android.com/topic/architecture/ui-layer/state-production)
- [Paging LoadState](https://developer.android.com/topic/libraries/architecture/paging/load-state)

## Feature 회귀 검증 (#156)

17개 ViewModel의 테스트 대응표·Fake 주입 경계·취소/Effect 수명 규칙과 실행 방법은 [ViewModel 회귀 가이드](../testing/viewmodel-regression.md)를 따른다. 계정 Repository는 core:data의 AccountLibraryAccess/AccountSyncOperations를 통해 주입하며 기존 저장·인증 정책은 구현이 소유한다. Profile 조회는 #161, 실제 서버/정식 업데이트 검증은 #150에서 이어간다.
