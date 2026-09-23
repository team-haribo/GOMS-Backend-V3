# GOMS Backend V3 AI 개발 규칙

이 문서는 GOMS Backend V3에서 AI 개발 도구가 따라야 하는 단일 기준 문서다. 도구별 지침이나 스킬이 이 문서와 충돌하면 이 문서를 우선한다. 문서에 없는 규칙은 실제 코드와 테스트, 기존 PR을 먼저 확인하고 최소한의 가정만 사용한다.

## 1. 프로젝트 개요

GOMS는 기숙사 학생의 외출·복귀와 지각을 관리하고 장소, 후기, 신고, 알림, 학생회 운영 기능을 제공하는 Spring 기반 백엔드다. 인증·회원, 외출/복귀, 지각, 장소 검색·추천·동기화, 후기·신고, FCM 알림, Discord 학생회 동기화, 스케줄러와 운영 모니터링을 포함한다.

## 2. 기술 스택

- Kotlin 2.2.21, Java 21, Spring Boot 4.0.2
- Gradle Groovy DSL, 단일 Gradle 모듈
- Spring MVC와 일부 WebFlux/WebClient
- Spring Data JPA, MariaDB/MySQL, H2 테스트 DB
- Spring Security, JWT, Redis
- Firebase Admin SDK, AWS S3 SDK, Spring Mail, Swagger/OpenAPI
- 테스트: Kotest 6.1.3, MockK 1.14.9
- CI 기준 빌드: ./gradlew build --no-daemon

버전과 의존성은 build.gradle을 기준으로 다시 확인한다. ktlint, Spotless, Detekt, Flyway, Liquibase 또는 별도 포맷터가 설치되어 있다고 가정하지 않는다.

## 3. 패키지 구조

기본 패키지는 com.teamharibo.goms다.

- domain/{도메인}/controller: HTTP 진입점
- domain/{도메인}/dto/request, response: 요청·응답 Kotlin data class
- domain/{도메인}/entity: JPA 엔티티
- domain/{도메인}/repository: JPA Repository 및 Redis Repository
- domain/{도메인}/service, service/impl: 서비스 인터페이스와 구현체
- domain/{도메인}/exception: 도메인 예외가 필요한 경우의 위치
- domain/{도메인}/job: 스케줄러·배치성 작업
- domain/common: 공통 enum 등 도메인 공용 타입
- global/config, discord, exception, jwt, log, security, util: 전역 설정과 공통 인프라

현재 도메인에는 auth, discord, health, late, member, notification, outing, place, report, review, studentcouncil 등이 있다. 새 패키지는 기존 도메인 경계를 먼저 확인하고 추가한다.

## 4. 도메인별 Layer 규칙

Controller는 요청 변환·검증·인증 경계·응답 상태 코드만 담당한다. 비즈니스 판단과 데이터 조합은 Service로 이동한다. Service 인터페이스와 ServiceImpl을 분리하는 현재 패턴을 따른다. Repository는 조회·저장·쿼리 캡슐화만 담당하고 정책을 갖지 않는다. Entity는 영속 상태와 도메인 상태 변경에 필요한 최소 메서드를 가진다.

## 5. Controller 규칙

- 기존 /api/v3/** 경로와 도메인별 URL을 먼저 조사한다.
- 생성자 주입을 사용하고 불변 의존성은 val로 선언한다.
- 응답은 기존 코드처럼 ResponseEntity<T> 또는 ResponseEntity<Void>를 사용한다.
- 성공 상태 코드, noContent 응답, Header 이름을 기존 API와 맞춘다.
- 요청 본문은 @Valid를 사용하고, 메서드 파라미터 검증은 실제 코드처럼 @Validated를 사용한다.
- OpenAPI 문서에는 기존 @Tag, @Operation, @ApiResponses, @SecurityRequirement 방식을 따른다.
- 인증·역할 제한은 Security 설정과 기존 Controller 계약을 확인한 뒤 적용한다.
- Controller에서 임의 try/catch로 GlobalException 계약을 우회하지 않는다.
- 하위 호환성이 필요한 API는 기존 JSON 필드, 상태 코드, Header를 보존한다.

## 6. Service와 ServiceImpl 규칙

- public 서비스 계약은 interface에 둔다.
- 구현체는 ServiceImpl 명명과 생성자 주입을 사용한다.
- 조회 전용 서비스에는 @Transactional(readOnly = true), 상태 변경 서비스에는 @Transactional을 적용한다.
- 외부 API 호출을 장시간 DB 트랜잭션 안에서 수행하지 않는다. 수집 단계와 DB 반영 단계를 분리하는 기존 장소 동기화 사례를 참고한다.
- 서비스에서 DTO를 명시적으로 조합하고, 예외와 권한 검증을 일관된 순서로 처리한다.
- 상태 전이·동시성·재시도·중복 요청은 기존 Repository 잠금과 테스트 패턴을 먼저 확인한다.

## 7. Repository 규칙

- JPA Repository는 JpaRepository와 현재 프로젝트의 Optional/List 반환 관례를 따른다.
- 복잡한 조회는 기존 @Query, @Param, @Lock, @Modifying 사용 방식을 확인한다.
- 사용자 자원 조회는 소유자·역할 조건을 쿼리 또는 서비스에서 반드시 검증한다.
- Redis 접근은 domain/{도메인}/repository/redis 아래 기존 StringRedisTemplate 래퍼 패턴을 따른다.
- Key prefix와 TTL은 기존 규칙을 재사용하고 Token·인증코드 원문을 로그에 남기지 않는다.

## 8. Entity 규칙

- JPA Entity는 현재 프로젝트의 일반 class와 nullable Long id 패턴을 따른다.
- ID가 nullable이면 사용 전에 저장 상태를 기존 방식으로 검증한다. Kotlin의 !!는 새 코드에서 피한다.
- 불변 값은 val을 우선하되 JPA 상태 변경이 필요한 필드는 기존 var 패턴을 따른다.
- nullable, unique, enum 저장 방식, columnDefinition, 관계 fetch는 실제 Entity와 운영 데이터 영향을 확인한다.
- 운영 데이터 보존을 고려해 삭제·상태 비활성화·cascade를 신중하게 선택한다.

## 9. Request와 Response DTO 규칙

- DTO는 Kotlin data class를 사용한다. Java record나 SDK 응답 wrapper를 사용하지 않는다.
- 요청 검증 어노테이션은 실제 코드처럼 @field:NotBlank, @field:Email, @field:Pattern, @field:NotNull, @field:Size 등을 사용한다.
- JSON 필드명은 현재 camelCase 계약을 따른다.
- 응답 DTO는 domain/{도메인}/dto/response에 두고 API 계약에 필요한 필드만 제공한다.
- 민감정보인 비밀번호, JWT, Refresh Token, 이메일 인증 코드와 Firebase credential은 DTO나 로그에 불필요하게 포함하지 않는다.

## 10. Validation과 예외 처리

- 요청 형식 검증은 Jakarta Validation과 기존 ErrorCode.INVALID_REQUEST 계약을 따른다.
- 비즈니스 예외는 GOMS의 GlobalException과 ErrorCode를 사용한다.
- 기존 ErrorCode를 우선 재사용하고 새 코드가 필요할 때만 상태 코드, 메시지, API 호환성을 검토해 추가한다.
- GlobalExceptionHandler가 GlobalException, 잘못된 요청, 예상하지 못한 예외를 ResponseEntity<ErrorResponse>로 처리한다.
- 인증·인가 예외와 도메인 비즈니스 예외를 구분한다.
- Controller에서 임의의 응답이나 try/catch로 전역 예외 계약을 바꾸지 않는다.
- 예외 메시지는 외부 노출 가능성을 고려하고 내부 stack trace와 secret을 응답에 넣지 않는다.

## 11. Logging 규칙

- 로그는 GOMS의 LogFormat.message(domain, event, fields...)를 사용한다.
- 기존 다수 사례처럼 domain은 AUTH, EXCEPTION 등 영문 대문자 식별자, event는 기존 코드의 한국어 설명, field key는 memberId 같은 camelCase 형식을 따른다.
- 로그에 이메일, 비밀번호, JWT, Refresh Token, 이메일 인증 코드, Firebase credential, AWS/SMTP/Redis/DB secret, Kakao key, Discord token 또는 webhook을 남기지 않는다.
- 요청·실패 원인은 기존 RequestLogConstants와 RequestLoggingFilter 흐름을 확인한다.
- 값 전체 대신 식별자·상태·원인 등 안전한 최소 정보만 기록한다.

## 12. Transaction, 외부 API, Scheduler

- 읽기 전용과 상태 변경의 트랜잭션 경계를 구분한다.
- 외부 API(WebClient/WebFlux 또는 현재 Client) 호출과 DB write 트랜잭션을 분리한다.
- Scheduler/Job은 중복 실행, 실패 시 재실행, 알림 중복 여부를 확인하고 서비스에 위임한다.
- 외부 응답·timeout·부분 실패를 GOMS ErrorCode와 기존 계약에 맞게 처리한다.

## 13. Redis와 Security/JWT

- Redis에는 TTL, key prefix, 삭제 시점을 명시하고 Refresh Token·인증 코드 폐기 흐름을 보존한다.
- JWT 서명 알고리즘, access/refresh 만료, Bearer Header parsing은 JwtProvider를 기준으로 한다.
- Spring Security의 SecurityFilterChain, JwtAuthenticationFilter, entry point와 access denied handler를 우회하지 않는다.
- 학생과 학생회 역할, 내부 API secret, 사용자 소유 자원 검증을 모두 확인한다.
- JWT secret, Firebase, AWS, SMTP, Kakao, Discord, DB, Redis credentials는 환경 변수·secret 파일에서만 주입하고 저장소에 실제 값을 추가하지 않는다.

## 14. 테스트 규칙

- 테스트는 src/test/kotlin 아래 같은 도메인 구조로 둔다.
- Kotest 6의 DescribeSpec, describe/context/it 패턴과 shouldBe/shouldThrow를 현재 사용 방식에 맞게 쓴다.
- 외부 협력자는 MockK mockk/every/justRun/verify를 사용한다.
- JPA가 필요한 테스트는 H2와 현재 Spring 테스트 구성을 사용한다.
- Controller 계약 테스트는 기존처럼 standaloneSetup, GlobalExceptionHandler, Jackson converter, status/jsonPath 검증 방식을 따른다.
- 비즈니스 로직 변경에는 회귀 테스트를 추가한다. 동시성, 트랜잭션 범위, 인증·인가, API 계약 변경은 관련 테스트를 우선한다.
- 테스트를 통과시키기 위해 assertion을 약화하거나 테스트를 삭제하지 않는다.
- 실행 명령은 ./gradlew test, ./gradlew compileKotlin test, ./gradlew build --no-daemon이다. Windows에서는 gradlew.bat을 사용한다.

## 15. GitHub Metadata, Commit, PR

- 일반 개발 PR의 base는 develop, 배포 PR만 main이다.
- 실제 브랜치 접두사는 feat/, fix/, refactor/, test/, chore/, hotfix/, ci/ 등을 따른다. 이번 작업은 chore/ai-harness다.
- 커밋은 feat:, fix:, refactor:, test:, chore:, docs:, ci: 같은 형식과 간결한 한국어 설명을 따른다.
- PR 제목은 불필요한 prefix 없이 간결한 한국어로 작성한다.
- .github/pull_request_template.md를 유지하고 실제로 수행한 검증만 체크한다.
- 실제 존재하는 Label만 사용하고, 애매하면 라벨을 붙이지 않는다.
- 사용자 요청이 없으면 commit, push, PR 생성, merge를 수행하지 않는다. 이번 작업은 사용자가 명시했으므로 commit/push/PR 생성까지 수행하되 merge·auto merge·deploy는 하지 않는다.

### Issue 생성 전후

- 생성 전에 동일하거나 유사한 Issue, 최근 제목 convention, 현재 repository의 label 목록, 기존 assignee를 `gh`로 확인한다.
- Issue에는 repository에 실제로 존재하는 label만 적용한다. 새 label을 자동 생성하지 않고, 의미가 가까운 label을 보통 1~2개만 선택한다.
- 사용자가 assignee를 지정하면 그대로 따른다. 이미 다른 assignee가 있으면 임의로 변경하거나 추가하지 않는다.
- assignee가 없고 사용자가 자신이 작업한다고 명시한 경우에만 현재 인증 사용자를 지정할 수 있다. 단순 backlog Issue에는 임의로 assignee를 지정하지 않으며, 애매하면 사용자에게 확인한다.
- Issue 생성 후 `title`, `assignees`, `labels`, `state`를 다시 조회해 의도한 metadata와 일치하는지 확인한다.

### PR 생성 전후

- 생성 전에 관련 Issue의 존재·assignee·label, PR title convention, base branch, PR label을 확인한다.
- 일반 개발 PR의 base는 `develop`으로 고정하고, `main`은 배포·release 등 repository convention이 명확한 경우에만 사용한다.
- PR은 해결하는 Issue를 명시하고, 단순 참고 Issue와 구분한다. 관련 Issue가 없거나 연결 방식이 애매하면 임의로 번호를 만들지 않는다.
- PR에도 실제로 존재하는 label만 적용하며 새 label을 만들지 않는다. 필요한 label은 보통 1~2개로 제한한다.
- PR 생성 후 `title`, `baseRefName`, `headRefName`, `labels`, `closingIssuesReferences`, `isDraft`, `assignees`를 다시 조회한다. 의도와 다르면 허용된 범위에서 수정하고 재검증한다.
- metadata 검증을 위해 실제 테스트 Issue나 PR을 새로 만들지 않는다.

### 책임 범위

- Issue·PR metadata 확인은 reviewer 자동 지정 Workflow(#93), PR Template 구조(#99), 코드 주석·KDoc 규칙(#100)을 대신하지 않는다.

## 16. 문서와 언어

프로젝트 문서와 AI 지침은 한국어로 작성한다. 코드 식별자와 로그의 domain/field처럼 기존 계약상 영문인 값은 유지한다. Washer 전용 SDK, ExpectedException, response wrapper 규칙을 GOMS 규칙으로 사용하지 않는다.

## 17. 변경 후 검증

하네스 변경 후 JSON/TOML/YAML front matter/Shell 문법, 문서 간 핵심 규칙, .agents/skills와 .claude/skills 목록, 금지 키워드, 참조 경로를 확인한다. 이어서 git diff --check, ./gradlew compileKotlin test, ./gradlew build --no-daemon을 실행한다. 실패하면 하네스 변경 때문인지 기존 애플리케이션 실패인지 구분해 보고한다.

## 18. AI가 하면 안 되는 행동

- .harness/**와 .reviewbot/**를 만들거나 수정하지 않는다.
- 애플리케이션 프로덕션 코드, API, DB schema, 환경 변수, Security, CI/CD와 Gradle 의존성을 임의로 바꾸지 않는다.
- 확인하지 않은 규칙, 존재하지 않는 Gradle task, formatter, migration 도구를 문서화하지 않는다.
- 기존 사용자 변경사항을 삭제하거나 stash/reset으로 숨기지 않는다.
- 실제 secret을 읽어 출력·커밋하지 않는다.
- git reset --hard, git clean -fdx, force push, 광범위한 삭제를 하지 않는다.
- main/develop 직접 push, PR merge, auto merge, release, deploy를 하지 않는다.
- 실패를 숨기기 위해 테스트를 삭제·약화하거나 무관한 파일을 함께 커밋하지 않는다.

## 19. 하네스 수동 유지

- AGENTS.md가 프로젝트 규칙의 단일 기준이다.
- 공통 스킬의 기준본은 .agents/skills이며 .claude/skills는 Claude 호환 entry다.
- 스킬을 수정할 때 두 위치의 목록과 판단 규칙이 일치하는지 contradiction 검사를 수행한다.
- 자동 동기화 봇이나 파일 덮어쓰기 동기화 기능은 사용하지 않는다. 스킬 변경은 사람이 두 위치와 참조 경로를 검토한다.
- 하네스 PR에서는 필수 파일, JSON/TOML/Shell 문법, 금지 키워드, 실제 Gradle task 참조를 읽기 전용으로 확인한다.
