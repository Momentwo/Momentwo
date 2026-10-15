# 데이터 입력·DTO 경계 (#157)

## 회원가입 계약

회원가입은 Feature의 SignUpState → core:model의 SignUpInput → core:data의 SignUpRepository → 내부 User DTO → 기존 Retrofit Service 순서다. Repository 인터페이스의 소유권은 core:data에 유지한다.

SignUpInput은 기본값 없는 non-null String 여섯 개(name, email, password, nickname, birthdayDigits, phoneDigits)를 가진 불변 입력 모델이다. JSON/Android 직렬화나 영속 저장 계약은 추가하지 않는다. Feature의 toSignUpInput은 현재 값을 복사하며 제출 시 한 번 캡처한다. 입력·검증·중복 제출·늦은 결과·Effect 규칙은 기존 MVI와 #156을 유지한다.

| 입력 | 서버 JSON 키 | 변환 |
|---|---|---|
| name | name | 그대로 |
| email | username | 그대로 |
| password | password | 그대로 |
| nickname | nickname | 그대로 |
| birthdayDigits | birthday | 20000102 → 2000-01-02 |
| phoneDigits | phone | 01012345678 → 010-1234-5678 |

UI의 ASCII 숫자 필터링과 8자리/11자리 제한을 유지한다. Data Mapper는 같은 길이·ASCII 숫자 여부를 확인하고 잘못된 입력은 Repository에서 IllegalArgumentException의 Result.failure로 반환한다. 이때 DataSource와 네트워크를 호출하지 않는다. 달력 유효성·전화번호 정책·이메일 대소문자·공백·비밀번호를 새로 정규화하지 않는다.

전송 경로는 POST /signup, 기존 여섯 JSON 필드와 content-type, isAuthorizable:false 메타데이터를 유지한다. 실제 AuthInterceptor가 메타데이터와 Authorization을 제거하며 세션을 조회하지 않는다. 중복 확인 메서드의 시그니처·위임·성공 판정은 이번 변경 대상이 아니다.

Repository는 DTO 변환과 DataSource 결과의 getOrThrow를 runSuspendCatching으로 감싼다. 일반 예외는 Result.failure로 보존하며 CancellationException·Error는 다시 던진다. 실행 전후 ensureActive로 하위 계층이 취소를 삼켜도 성공 결과를 반환하지 않는다.

## 공개 범위

- 공개: SignUpInput, SignUpRepository.
- core:data 내부: User, SignUpRepositoryImpl, SignUpDataSource, SignUpRemoteDataSource, SignUpService, SignUpModule, toUserDto.
- User와 같은 파일의 UserProfile은 기존 Login/Profile 응답에서 사용하므로 public과 JSON 계약을 유지한다.
- User와 SignUpInput의 toString은 필드 값을 출력하지 않는다. 입력 전체 로그가 없는 현재 상태와 HTTP 로깅 NONE을 유지한다. 실제 전송 JSON에는 필요한 원래 값이 포함되며 로그 표현과 전송 표현을 혼동하지 않는다.

## 다른 공개 DTO 감사

운영·테스트의 Feature, core:domain, app을 조사했다. #157 이전의 직접 DTO import는 Auth의 회원가입 Mapper와 FakeSignUpRepository였고 이번에 제거했다. Repository 구현 내부에서 DTO를 사용하는 것은 외부 계약 노출과 구분한다. 예를 들어 AccountLikeRepository의 LikeRequest는 private write 내부에서만 사용한다.

| 영역 | 남은 public DTO 경계 | 실제 의존·후속 범위 |
|---|---|---|
| Repository 인터페이스 | 회원가입은 SignUpInput으로 전환. 다른 Repository 인터페이스의 네트워크 DTO 직접 노출은 확인되지 않음(core:model·기본 타입·Paging/Flow·Uri 등) | 이번 Feature DTO 의존 제거 완료 |
| Login / Profile | LoginDataSource·LoginService, ProfileDataSource·ProfileService의 UserProfile·LoginRequest | core:data 안에서 변환·사용. 이번에는 가시성을 일괄 변경하지 않음 |
| Album / SubAlbum | DataSource·Service의 생성/수정 요청, 목록/권한·presigned DTO | core:data 내부 소비. 후속 가시성 정리 시 DI·업로드 계약과 함께 검증 |
| Friend | Remote DataSource·Service의 요청/검색/페이지 DTO | core:data 내부 소비. Local DataSource의 Room Entity 경계는 DTO와 별도 책임 |
| Photo / Comment / Description / Like / Member | DataSource·Service의 페이지·쓰기/응답 DTO | core:data 내부 소비. 기능별로 internal 전환과 DI 노출을 함께 정리할 후속 후보 |

이 목록은 Feature가 모든 public DTO에 의존한다는 뜻이 아니다. 현재 실제 Feature DTO import는 없으며, 다른 영역의 public 선언 정리는 이번 PR에 포함하지 않는다. 전체 경계 검사의 CI 자동화는 #158이다.

## 검증

- Feature Mapper/기존 Auth 회귀는 숫자 입력과 제출 시점의 캡처를 검증한다.
- Data Mapper/Repository 테스트는 필드 보존·잘못된 입력·일반 실패·취소·Error·기존 중복 확인 위임을 검증한다.
- 기존 Moshi와 실제 Retrofit·AuthInterceptor를 경유하는 MockWebServer 테스트는 정확한 요청 JSON/헤더, 인증·세션 불필요, HTTP 실패 보존을 검증한다. 서버는 테스트용 loopback이며 실제 가입 성공을 확인한 결과가 아니다.
- 내부 User의 생성 Moshi 어댑터 round-trip과 기존 UserProfile 디코딩을 유지한다. Model과 DTO의 로그 문자열에 입력 값이 포함되지 않는지 검증한다.
- 실제 서버의 회원가입 성공/실패 골든 패스는 #150, 회원가입 UX 변경은 #18에 남긴다.


### 실행 결과 (2026-10-15)

- 전체 단위 테스트를 `:core:model:test testDebugUnitTest --rerun-tasks`로 재실행했다. Model JVM 1건 + Android 단위 238건 = **239건**, 실패·오류·skip 0이다. 이번 변경에서 17건을 보완했으며 Data 46건·Auth 20건을 포함한다.
- app Debug/Release·AndroidTest APK 빌드, core:data·feature:auth:impl·app lintDebug와 Release lintVital을 통과했다. Hilt/Moshi 생성 코드와 internal 경계가 함께 검증됐다.
- SM_N976N API 31의 별도 `cord.eoeo.momentwo.migrationcheck` 앱에서 비행기 모드 Navigation 11건 + 게스트 계정 연결 취소 1건 = **12건**을 통과했다. 회원가입·로그인·뒤로가기·Hilt·nullable Profile·Paging·Effect/lifecycle·시스템 바와 로그인 취소 후 초안 보존/콘텐츠 API 요청 0을 확인했다.
- 원본 앱을 보존하고 비행기 모드는 원래 disabled로 복원했다. 임시 applicationId 설정·기기 산출물은 커밋하지 않는다. 실제 서버 가입 성공은 #150의 미확인 항목으로 유지한다.
