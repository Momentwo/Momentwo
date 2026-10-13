# MVI 개선 계획 (#155)

상위 계획 #154. ViewModel 9개에 순수 Reducer, 파일 분리, 취소를 보존하는 요청 유틸과 화면 상태를 적용한다. 실제 API 통합 회귀는 백엔드 복구 후 #150에서 확인한다.

## 설계 결정

- Event는 UI 의도, Action은 상태 변경 입력, Effect는 UI의 일회성 명령이다.
- Reducer는 최신 State를 순수하게 변환한다. API, coroutine, 시간 조회, Effect 발행을 넣지 않는다.
- 최초 조회는 ScreenState.Loading/Success/Error로 전체 본문을 분기한다. Success만 필수 Content를 소유한다.
- 현재 탭의 필수 조회만 로딩 조건에 포함한다. 탭별 상태가 원본이며 현재 화면은 여기서 파생한다.
- 성공 후 재조회는 Content를 유지하고 Success.refresh의 Idle/Loading/Error로 처리한다. 빈 목록은 Success다.
- 입력, 선택, 다이얼로그와 작업별 pending은 조회 결과와 분리한다. 입력은 수정 가능하고 동일 쓰기만 중복 차단한다.
- Paging의 LoadState를 원본으로 Route에서 표시 상태를 파생한다. 수집은 Loading/Error에서도 유지한다.
- CancellationException 및 Error는 runSuspendCatching에서 재전파한다. 실행 전후·결과 반영 전 ensureActive와 요청 식별자를 검사한다.
- 조회는 LatestWins, 쓰기는 IgnoreWhileRunning. 이미 성공한 서버 쓰기와 후속 조회 실패를 구분한다.
- Channel(BUFFERED), STARTED 단일 Effect 수집을 유지한다. 영속 전달 및 수신 직후 취소 경계의 완전한 전달은 보장하지 않는다.
- SubAlbum 상태는 AlbumDetail 소유. DTO·Orbit·Profile 기능·Offline-First·디자인 시스템·HTTP 인증 재설계는 별도 이슈다.

## 단계별 PR

모든 PR의 base는 develop이다. 앞 PR을 검증·병합한 후 최신 develop에서 다음 브랜치를 만든다.

1. 취소 보존 함수, Data/Domain/DataStore 전환 및 취소 회귀 테스트
2. Reducer/ScreenState/요청/Effect 기반과 Login 시범 전환
3. 나머지 Auth와 SignUp 검증 분리
4. Friend의 탭·ID 처리 및 마지막 페이지 저장
5. AlbumDetail의 Content·권한·탭 상태와 역할별 Reducer
6. Album/CreateAlbum 전환
7. Photo의 Paging·Mediator·낙관적 갱신
8. 구 기반 제거, 가이드와 최종 통합 검증

## 검증 기준

StandardTestDispatcher와 제어 가능한 Fake로 취소, 역순 응답, 중복 제출, 최신 입력·선택 보존을 재현한다. 상태별 UI, 빈 Success, 탭 전환, Paging 캐시·refresh·append·마지막 페이지, Effect 순서·lifecycle과 시스템 바 복원을 확인한다.

각 PR에서 영향 모듈 테스트·빌드·Lint를 수행하고 마지막 PR에서 전체 검사와 가능한 기기 확인을 수행한다. 별도 applicationId로 설치하여 기존 앱을 보존한다. 백엔드 미배포로 API 골든 패스는 미확인이다.

## 완료 단계

| 단계 | PR | 내용 |
|---|---|---|
| 1 | [#164](https://github.com/Momentwo/Momentwo/pull/164) | 취소 보존 함수와 하위 계층 |
| 2 | [#165](https://github.com/Momentwo/Momentwo/pull/165) | 공통 Reducer·State·요청·Effect, Login |
| 3 | [#166](https://github.com/Momentwo/Momentwo/pull/166) | SignUp 검증·폼 보존, Profile 기반 |
| 4 | [#167](https://github.com/Momentwo/Momentwo/pull/167) | Friend 탭·ID 처리, 마지막 페이지 |
| 5 | [#168](https://github.com/Momentwo/Momentwo/pull/168) | AlbumDetail 권한·Content·역할별 Reducer |
| 6 | [#169](https://github.com/Momentwo/Momentwo/pull/169) | Album·CreateAlbum |
| 7 | [#170](https://github.com/Momentwo/Momentwo/pull/170) | Photo·Paging·낙관적 갱신 |
| 8 | 최종 PR | 구 기반 제거·가이드·전체 검증 |

현재 구현 규칙은 [MVI 구현 가이드](mvi-guide.md)를 따른다.

## 진행 기록

- 단계 1: 취소를 실패로 변환하던 suspend runCatching을 전환하고 연속 로그인 저장 작업의 취소 전파와 프로필 저장 실패 처리를 보강한다.

  검증: Common 6개, Domain 2개, DataStore 1개 회귀 테스트와 app 빌드·Lint 및 영향 Core Lint 통과.

- 단계 1 병합: #164. SM_N976N API 31 별도 앱 로그인/회원가입/뒤로가기 smoke 1개 통과.
- 단계 2: ReducerViewModel, ScreenState/RefreshState/UiError, 요청별 정책과 취소 보존 Result 해제, STARTED 단일 Effect 수집 및 Login 파일 분리.
  Login의 요청 중 입력 유지·중복 제출 차단·수동 로그인에 의한 자동 로그인 교체·멱등 초기화를 검증한다.

## 공통 기반 사용 규칙

- `onEvent`와 요청 등록은 Main에서 실행한다. Reducer의 update는 재실행될 수 있어 상태 외 부수 효과를 금지한다.
- `launchRequest`는 Result를 getOrThrow로 해제한 요청 부분만 runSuspendCatching으로 감싼다. 성공/실패 콜백의 프로그래밍 오류는 요청 오류로 바꾸지 않는다.
- 요청 키는 기능별 RequestKey 구현과 대상 ID로 구성한다. 새 토큰을 등록한 뒤 기존 작업을 취소하여 이전 완료 정리가 최신 pending을 해제하지 않게 한다.
- 화면 초기화는 Route에서 멱등 initialize를 호출한다. 최초 진입은 LaunchedEffect(viewModel), 무효화된 자식 화면 복귀는 LifecycleStartEffect를 사용하며 Effect 수집과 분리한다.
- Snackbar와 애니메이션은 CollectEffect의 STARTED CoroutineScope에서 launch한다. 동기 Navigation 처리는 직접 수행한다.
- ScreenContent는 전체 본문을 Loading/Success/Error로 분기한다. Success의 refresh만 진행/실패를 표시하며 동일 조회 오류를 Snackbar로 중복 발행하지 않는다.

- 단계 2 병합: #165. Common 18개 + Auth 13개 테스트, app 빌드·Lint, Common/UI Lint와 기기 smoke 통과.
- 단계 3: SignUp의 State/Event/Action/Effect/Reducer/Validator/Mapper 분리. 첫 오류 검증 순서·정규화·전송 형식 보존, 입력 변경/중복 제출/취소/실패 회귀 추가.
  Profile은 기존 기능을 확장하지 않고 생성자 경로 초기화 및 새 기반으로 전환한다. 현재 상태 변경 Action이 없어 Reducer의 Action 타입은 Nothing이다.

- 단계 3 병합: #166. Auth 16개 테스트 및 빌드·Lint, 별도 앱 smoke 2개 통과. SignUp ViewModel 최종 62줄.
- 단계 4: Friend의 요청 탭별 ScreenState·사용자 ID 결과 반영·작업별 pending·다이얼로그 변경 버전 도입.
  Paging LoadState 기반 표시·빈 성공 보존·cache/refresh/append 오류 처리와 Friend 마지막 페이지 저장 수정.
  UI 6개 + Data 3개 + Friend 9개 회귀 테스트 및 app/영향 모듈 빌드·Lint 통과.

- 단계 4 병합: #167. 새 회귀 18개 및 빌드·Lint, Friend 기기 smoke 통과.
- 단계 5: AlbumDetail의 역할별 Reducer, 탭별 필수 Content·권한, 타입화된 다이얼로그와 제출/편집 버전으로 전환.
  가짜 ADMIN 및 경로에 없는 count=0 헤더 데이터를 제거한다. Hilt 경로 디코딩과 초기 State 주입을 분리하여 업무 로직을 JVM에서 검증한다.
  삭제/초대는 제출 대상만 정리하고 새 선택·다이얼로그·이미지 초안을 유지한다. 이미지 저장 성공 후 취소는 마지막으로 확인된 저장 이미지로 복원한다.
  PhotoList 방문 시 서브앨범을 무효화해 복귀 시 재조회하되 초기 콜백/재구성은 중복 조회하지 않는다. 삭제 실패 이전의 UI 강제 이동 및 지연 이미지 정리를 제거한다.
  AlbumDetail 18개 테스트와 앱/Detail/UI 빌드·Lint 통과.

- 단계 5 병합: #168. Detail 18개 테스트 및 빌드·Lint, 기기 smoke 2개 통과. ViewModel 189줄.
- 단계 6: Album 전체 본문 상태와 생성/상세 방문 시 무효화·복귀 재조회. CreateAlbum 임시/확정 선택 원본 분리, 타이틀 SavedStateHandle 보존과 제출 스냅샷/중복 차단.
  생성 중 새 입력이 생긴 경우 성공 안내만 표시하고 새 초안을 닫지 않는다. 친구 재조회는 유효한 선택을 유지하고 사라진 친구만 정리한다.
  Album/CreateAlbum 회귀 9개 및 app/Album 빌드·Lint 통과.

- 단계 6 병합: #169. Album/CreateAlbum 9개 테스트 및 빌드·Lint, 기기 앨범/생성 경로 smoke 통과.
- 단계 7: PhotoList/PhotoDetail의 분리된 계약과 역할별 Reducer, 필수 설명·좋아요 수의 원자적 Success 전환 및 입력/선택/다이얼로그 버전 보존.
  좋아요는 해당 필드만 롤백하며 서버 ACK 이후 로컬 캐시 실패와 설명 재조회 실패를 구분한다. 댓글 대상은 ID로 처리하고 제출 중 새 초안을 보존한다.
  Photo Mediator를 Pager별 immutable 인자로 생성하며 마지막 페이지·실제 좋아요 값을 저장한다. 사진/커서 저장은 앨범/서브앨범 범위의 Room 트랜잭션으로 수행하며 스키마는 유지한다.
  Photo 15개 및 Photo Mediator 6개 회귀, 영향 모듈 빌드·Lint 통과. 시스템 바는 지속 State와 lifecycle로 적용/복원한다.
  SM_N976N API 31 별도 앱 기기 6개 통과: 사진 초기 상태/경로, ScreenContent, Effect 복귀/순서, 시스템 바 pause/resume/dispose, 실제 Room 저장 범위/취소 롤백.

- 단계 7 병합: #170. 새 자동 회귀 21개 및 기기 6개 통과.
- 단계 8: 9개 ViewModel의 새 기반 사용을 확인하고 BaseViewModel·SharedFlow 이벤트 기반과 미사용 lifecycle 키를 제거했다.
  LAZY 예약 중 재진입 요청의 중복 차단과 Presigned 연속 작업의 중첩 Result/삼킨 취소 전파를 추가 검증했다. README·모듈 의존성·구현 가이드를 현재 구조로 갱신했다.
  최종 clean 전체 debug 빌드·전체 Android 모듈 Lint·단위 테스트 123개·기기 테스트 APK 빌드 통과(1m 14s). 기존 API 직렬화와 라이브러리 버전·24개 모듈 경계를 유지한다.
  기기 테스트의 재구성/Navigation 그래프 준비 대기 조건을 명시한 뒤 SM_N976N API 31 별도 앱 전체 13개 통과(59s): 경로/Hilt/탭/슬롯/뒤로가기, 필수 데이터 비노출·refresh 보존, 숨겨진 본문의 Paging 수집·빈 Success 재조회, Effect STARTED 복귀·최신 handler, 실제 시스템 바 표시/숨김·복원, Room 저장 범위·취소 롤백.

## 최종 검증 결과 (2026-10-13)

- 전체 clean debug 빌드, 전체 Android 모듈 Lint 및 전체 단위 테스트 123개 통과. 실패·오류·skip 0.
- 별도 패키지의 실제 기기 회귀 13개 통과. 원본 앱과 제품 applicationId는 보존한다.
- 9개 ViewModel 모두 Reducer 기반. 구 BaseViewModel/SharedFlow 이벤트 및 하위 계층의 직접 suspend runCatching 잔존 없음. runCatching은 취소 보존 함수 내부에서만 사용한다.
- app main 소스 6개와 모듈 24개 유지. 금지 모듈/소스 의존 없음. 기존 API·모델·경로 직렬화 및 라이브러리 버전 변경 없음.
- 실제 API·정식 업데이트/프로세스 복원은 #150에서 별도로 확인한다. #156~#163과 HTTP 인증 재설계는 이번 완료 범위에 포함하지 않는다.
