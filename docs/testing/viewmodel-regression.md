# Feature ViewModel 회귀 대응표 (#156)

## 기준

2026-10-15 develop 기준 17개 ViewModel을 Event → 요청 인자 → State/Effect로 검증한다. 기존 검증을 재사용하며 신규 기능은 만들지 않는다. 실제 API/정식 서명 업데이트는 #150, Profile 조회 구현은 #161이다.

| 대상 | 기존 검증 | 이번 보완 / 실행 단계 |
|---|---|---|
| Login / SignUp | 입력 경쟁, 중복 제출, 취소, 세션/입력 보존 | 마지막 PR에서 누락 감사 |
| Friend | 최초 선택 탭, 응답 역전, ID 기반 변경 | 검색/요청 실패·재시도·취소·Effect (PR 2) |
| Album / CreateAlbum | 빈 성공, refresh 실패, 제출 이후 입력 | 재시도, 생성 실패/초대, 탐색 Effect (PR 2) |
| AlbumDetail | 필수 권한, 탭 격리, 선택/초대 보존 | CRUD·설정·멤버 실패와 후속 조회 (PR 2) |
| LocalLibrary / LibraryCreate | 로컬 최초 진입·초안·중복/해제 | 계정 생성/초대·owner·실패 (PR 2) |
| LocalAlbum | 초안·일괄 삭제·선택·취소 | 계정 멤버·권한·탭·쓰기 실패 (PR 2) |
| AccountGate / AccountLibrary | 순수 Reducer | Fake ViewModel: 구독·계정 전환·조회·로그아웃·Effect (PR 1) |
| PhotoList / PhotoDetail | 선택·댓글·설명·좋아요 경쟁/취소 | 조회/쓰기 실패·재시도·취소·Effect (PR 3) |
| LocalPhotoList / LocalPhoto | 상세 초안/개인 기록·삭제 | 목록·계정 공유 기록·내보내기·Uri 요청 (PR 3) |
| AccountSync | 순수 Reducer·실제 저장소 계측 | Fake ViewModel: 복사/생성/후보/충돌/명시적 작업 (PR 4) |
| Profile | 경로 직렬화·실제 Hilt nullable 인자 | 현재 State/오류 Effect/해제 (PR 4); 조회는 #161 |

## 테스트 작성 규칙

- MainDispatcherRule(StandardTestDispatcher())와 동일 scheduler의 runTest를 사용한다.
- Fake는 각 impl 테스트 소스에 둔다. 사용하지 않는 계약은 즉시 실패하며 인자·횟수·구독을 기록한다.
- ControlledCall로 성공/실패/취소와 응답 역전을 제어한다. 실시간 sleep이나 실제 API에 기대지 않는다.
- EffectRecorder는 하나의 collector로 순서·횟수·추가 발행 없음을 검증한다. collector는 backgroundScope가 종료 시 취소한다.
- ViewModelStore를 정리해 관찰/요청을 종료한다. 취소를 삼키는 Fake 응답은 테스트 안에서 반드시 해제한다.
- 최신 State/입력/선택을 보존하고 살아 있는 요청의 pending만 정리하는지 검증한다. 해제 후 State/Effect는 변경하지 않는다.
- Android Uri 요청은 실제 Uri를 사용하는 별도 Feature 계측 테스트, 파일/Room 원자성은 기존 저장소 테스트로 확인한다.
- Paging의 LoadState 표시 계약은 core:ui에서 유지하며 ViewModel에 상태 복제나 라이브러리 업그레이드를 추가하지 않는다.

## 진행

- PR 1: 계정 인터페이스와 Hilt binding, 계정 gate/library의 구독·실패·전환·Effect 신규 8건. Album impl 단위 33건과 app Debug/Hilt 빌드, 영향 모듈 테스트·Lint 검증.
- 나머지 항목은 각 PR에서 대응표와 검증 결과를 갱신한다.
