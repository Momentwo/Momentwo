# core:navigation

Phase 1의 통합 MomentwoDestination 키는 Phase 2에서 각 feature:api로 이관했다. Phase 3에서 빈 legacy 타입과 사용하지 않는 serialization 설정을 제거했다.

현재 공용 Navigation 소스는 없다. 기존 24개 모듈 계약과 Feature convention 기본 의존성을 유지하며, 공용 Navigation 코드가 실제로 필요할 때 이 모듈에 추가한다. Feature 키는 각 API가 소유하고 app이 루트 NavHost·이동 콜백·SubAlbum 슬롯을 조립한다.
