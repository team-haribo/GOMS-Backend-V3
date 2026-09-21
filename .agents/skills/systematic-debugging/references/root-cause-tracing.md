# 원인 추적 질문

- Controller 입력이 어떤 Service와 Repository를 거치는가?
- transaction 시작·종료 시점과 외부 호출 시점은 어디인가?
- Redis key/TTL 또는 JWT claim이 어느 단계에서 변하는가?
- 예외가 ErrorCode와 HTTP 상태로 어떻게 매핑되는가?
- 실패를 재현하는 최소 입력과 회귀 테스트는 무엇인가?
