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

## 진행 기록

- 단계 1: 취소를 실패로 변환하던 suspend runCatching을 전환하고 연속 로그인 저장 작업의 취소 전파와 프로필 저장 실패 처리를 보강한다.

  검증: Common 6개, Domain 2개, DataStore 1개 회귀 테스트와 app 빌드·Lint 및 영향 Core Lint 통과.
