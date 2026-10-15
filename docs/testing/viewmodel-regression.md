# Feature ViewModel 회귀 대응표 (#156)

## 기준

2026-10-15 develop 기준 17개 ViewModel을 Event → 요청 인자 → State/Effect로 검증한다. 기존 검증을 재사용하며 신규 기능은 만들지 않는다. 실제 API/정식 서명 업데이트는 #150, Profile 조회 구현은 #161이다.

| 대상 | 기존 검증 | 보완 검증 / PR |
|---|---|---|
| Login / SignUp | 입력 경쟁, 중복 제출, 취소, 세션/입력 보존 | 기존 16건 유지, 종료 시 취소 정리 drain (PR 4) |
| Friend | 최초 선택 탭, 응답 역전, ID 기반 변경 | 검색/요청 실패·재시도·취소·Effect (PR 2) |
| Album / CreateAlbum | 빈 성공, refresh 실패, 제출 이후 입력 | 재시도, 생성 실패/초대, 탐색 Effect (PR 2) |
| AlbumDetail | 필수 권한, 탭 격리, 선택/초대 보존 | CRUD·설정·멤버 실패와 후속 조회 (PR 2) |
| LocalLibrary / LibraryCreate | 로컬 최초 진입·초안·중복/해제 | 계정 생성/초대·owner·실패 (PR 2) |
| LocalAlbum | 초안·일괄 삭제·선택·취소 | 계정 멤버·권한·탭·쓰기 실패 (PR 2) |
| AccountGate / AccountLibrary | 순수 Reducer | AccountAccessViewModelTest: 구독·전환·조회·로그아웃·Effect (PR 1) |
| PhotoList / PhotoDetail | 선택·댓글·설명·좋아요 경쟁/취소 | 조회/쓰기 실패·재시도·취소·Effect (PR 3) |
| LocalPhotoList / LocalPhoto | 상세 초안/개인 기록·삭제 | 목록·계정 공유 기록·내보내기·Uri 요청 (PR 3) |
| AccountSync | 순수 Reducer·실제 저장소 계측 | AccountSyncViewModelTest: 복사/생성/후보/충돌/명시적 작업 (PR 4) |
| Profile | 경로 직렬화·실제 Hilt nullable 인자 | 현재 State/오류 Effect/해제 (PR 4); 조회는 #161 |

## 테스트 작성 규칙

- MainDispatcherRule(StandardTestDispatcher())와 동일 scheduler의 runTest를 사용한다.
- Fake는 각 impl 테스트 소스에 둔다. 사용하지 않는 계약은 즉시 실패하며 인자·횟수·구독을 기록한다.
- ControlledCall로 성공/실패/취소와 응답 역전을 제어한다. 실시간 sleep이나 실제 API에 기대지 않는다.
- EffectRecorder는 하나의 collector로 순서·횟수·추가 발행 없음을 검증한다. collector는 backgroundScope가 종료 시 취소한다.
- ViewModelStore를 정리하고 Main 교체 해제 전에 scheduler.runCurrent()로 취소 정리를 실행한다. 취소를 삼키는 Fake 응답은 테스트 안에서 반드시 해제한다.
- 최신 State/입력/선택을 보존하고 살아 있는 요청의 pending만 정리하는지 검증한다. 해제 후 State/Effect는 변경하지 않는다.
- Android Uri 요청은 실제 Uri를 사용하는 별도 Feature 계측 테스트, 파일/Room 원자성은 기존 저장소 테스트로 확인한다.
- Paging의 LoadState 표시 계약은 core:ui에서 유지하며 ViewModel에 상태 복제나 라이브러리 업그레이드를 추가하지 않는다.

## 진행

- PR 1: 계정 인터페이스와 Hilt binding, 계정 gate/library의 구독·실패·전환·Effect 신규 8건. Album impl 단위 33건과 app Debug/Hilt 빌드, 영향 모듈 테스트·Lint 검증.
- 나머지 항목은 각 PR에서 대응표와 검증 결과를 갱신한다.
- PR 2: Friend 14건, Album 38건, AlbumDetail 33건 (합계 85건, 신규 16건) 통과. 검색 취소/불일치 인자, 양쪽 요청 탭 실패·재시도, 쓰기 실패·취소, 초대 payload와 계정 epoch, 상세 쓰기/후속 조회 실패, 로컬 멤버 재시도·계정 연결 안내를 보완했다. 영향 모듈 Debug 빌드와 app Lint 통과. 기존 요청 목록은 제거 대신 isUpdated로 표시하는 계약을 유지한다.
- PR 3: Photo impl JVM 35건 (신규 11건), 별도 PhotoUriViewModelTest 기기 2건 통과. Paging source 확보 재시도·삭제/이름 변경 실패·초안·내보내기·개인/공유 기록 분리와 실제 Uri의 업로드 중복/실패/취소·가져오기 부분 실패/취소를 보완했다. 별도 패키지는 cord.eoeo.momentwo.photo.migrationcheck.test이며 AndroidJUnitRunner는 기존 app의 해석 버전 1.6.1을 명시했다. APK 빌드·Lint 통과. 서버/파일 자체 대신 Fake 요청 결과를 제어하며 실제 저장소 검증은 기존 기기 회귀를 유지한다.

- PR 4: AccountSyncViewModelTest 신규 7건, ProfileViewModelTest 신규 3건, 계정 생성의 늦은 실패 신규 1건. Account/Album 46건과 Auth/Profile 19건을 통과했다.

## 재현 후 수정한 문제

1. 계정 복사 화면에서 A → B 전환 시 선택과 다이얼로그만 지우고 이전 계정의 draft 문자열을 State에 남겼다. 기존 전환 초기화에 draft를 포함했다.
2. A 계정의 앨범 생성 실패가 B 전환 후에도 오류 Effect를 발행했다. 제출 시점의 계정·epoch와 현재 identity를 대조한 뒤 실패 메시지를 발행한다.

두 검증은 수정 전 실패를 확인하고 수정 후 통과했다. 저장/전송 정책은 바꾸지 않았다. Fake의 예기치 않은 호출은 AssertionError로 실패시켜 요청 실패 UI와 혼동하지 않는다.

## 남기는 범위

- Profile 조회/재시도 State와 실제 사용자 표시는 #161에서 구현·검증한다. 여기서는 nullable 초기 인자, 현재 오류 Effect, 해제 경계를 검증하며 실제 Hilt 경로 계측 테스트를 유지한다.
- 실제 서버 로그인·전송·공유 및 정식 서명 업데이트/OS 복원은 #150이다. Fake 완료나 프로세스 재생성을 실서버·물리 재부팅 검증으로 표현하지 않는다.
- 회원가입 DTO 경계 #157, CI 자동화 #158, Orbit #159, 디자인 #163 및 기존 미완성 기능 #18/#63/#64는 별도 범위다.

## 실행

JDK 17로 전체 단위 테스트를 `./gradlew testDebugUnitTest --rerun-tasks --max-workers=2`로 다시 실행한다. Debug/Release·lintDebug와 AndroidTest APK도 검증한다. 원본 앱을 설치/삭제/초기화하지 않는다.

기기 검증은 임시 applicationId 설정으로 빌드한 `cord.eoeo.momentwo.migrationcheck`와 기존 `scripts/verify-offline-device.py`를 사용한다. Photo Feature Uri 검증은 별도 `cord.eoeo.momentwo.photo.migrationcheck.test/androidx.test.runner.AndroidJUnitRunner`의 `PhotoUriViewModelTest`를 실행한다. 기기 테스트는 `OK (N tests)`를 확인하며 프로세스 crash/INSTRUMENTATION_FAILED를 성공으로 처리하지 않는다.

## 최종 결과 (2026-10-15)

- 전체 JVM **222건** (기존 173 + 보완 49), 실패·오류·skip 0. 전체 재실행에서 816개 Gradle 작업을 실행했고 테스트 캐시를 재사용하지 않았다.
- Feature impl은 Auth/Profile 19, Friend 16, Album/Account/Sync 46, AlbumDetail 33, Photo 36건이다. 공통 상태·요청·취소·Paging 및 API 경로 테스트도 전체 결과에 포함한다. 마지막 감사에서 Friend 검색 실패/응답 역전 2건과 개인 메모 편집/삭제 1건을 추가 보완했다.
- Debug/Release, 전체 lintDebug·Release lintVital, app/Photo AndroidTest APK 빌드 통과.
- SM_N976N API 31: 별도 앱 비행기 모드 suite 39건 + 외부 force-stop 전후 seed/verify 2건, Photo Uri Fake 계측 2건 = **43건** 통과. 기존 Navigation/Hilt/nullable Profile·Paging·Effect/lifecycle·시스템 바·오프라인/계정 격리·Room/파일 회귀를 유지했다.
- 콘텐츠 API 요청 0인 로컬 흐름, 실제 프로세스 경계와 원본 파일 보존을 확인했다. 원본 앱 cord.eoeo.momentwo는 보존했고 비행기 모드는 원래 disabled로 복원했다. 임시 applicationId 설정·기기 산출물·인증 정보는 제품 코드에 포함하지 않는다.
- 4개 PR은 선행 병합 후 최신 develop의 별도 브랜치에서 진행했다. #156은 현재 기능의 대응표를 충족하면 종료하며 Profile 조회 #161과 실제 서버/정식 업데이트 #150은 남긴다.

## #157 후속 회원가입 입력 검증

Auth의 FakeSignUpRepository는 서버 DTO 대신 SignUpInput을 기록한다. 기존 제출 성공·입력 변경 테스트는 birthdayDigits/phoneDigits의 원래 숫자 값을 검증하고, 별도 Feature Mapper 테스트로 여섯 필드 보존을 확인한다. 전송 구분자·JSON·인증 헤더 검증은 Data 테스트가 소유한다. [데이터 경계 가이드](../architecture/data-boundaries.md)를 따른다.
