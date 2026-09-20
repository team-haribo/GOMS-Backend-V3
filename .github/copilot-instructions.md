# GOMS Backend V3 Copilot 지침

프로젝트 규칙의 단일 기준은 루트 AGENTS.md다. 이 문서는 코드 제안에 필요한 핵심만 요약한다.

- Kotlin 2.2.21, Java 21, Spring Boot 4.0.2, Gradle Groovy DSL의 단일 모듈 프로젝트다.
- 기존 domain/{도메인}/controller, dto/request·response, entity, repository, service·impl 구조를 유지한다.
- 생성자 주입과 val을 우선하고, DTO는 Kotlin data class를 사용한다.
- 요청 검증 어노테이션에는 실제 코드처럼 @field:NotBlank, @field:Email, @field:NotNull 등을 사용한다.
- Controller는 /api/v3/** 기존 경로와 OpenAPI 패턴을 따르며 ResponseEntity<T> 또는 ResponseEntity<Void>를 반환한다.
- 비즈니스 로직은 Service에 두고, 조회는 @Transactional(readOnly = true), 상태 변경은 @Transactional을 사용한다.
- 예외는 GlobalException과 ErrorCode를 재사용하고 GlobalExceptionHandler 계약을 우회하지 않는다.
- 로그는 LogFormat.message()를 사용하며 이메일·비밀번호·JWT·Refresh Token·인증 코드·cloud credential을 남기지 않는다.
- JPA Entity의 nullable ID는 사용 전에 확인하고 새 코드에서 !!를 피한다.
- 테스트는 Kotest DescribeSpec과 MockK를 현재 프로젝트 방식으로 작성하고 회귀 테스트를 추가한다.
- GOMS에 없는 SDK response wrapper, ExpectedException, Java record, Lombok 규칙, Spotless/ktlint/Detekt task를 제안하지 않는다.
- 소스 코드, API, DB, Security, CI/CD, dependency 변경 요청이 아니라면 그 영역을 수정하지 않는다.

자세한 규칙, 검증 명령, 금지 행동은 AGENTS.md를 읽는다.
