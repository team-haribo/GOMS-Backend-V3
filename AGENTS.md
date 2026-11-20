# GOMS Backend V3 AI Development Rules

This document is the single source of truth that AI development tools must follow in GOMS Backend V3. If a tool-specific instruction or skill conflicts with this document, this document takes precedence. For rules not covered here, check the actual code, tests, and existing PRs first, and use only minimal assumptions.

## 1. Project Overview

GOMS is a Spring-based backend that manages dormitory students' outings, returns, and lateness, and provides place, review, report, notification, and student council operation features. It covers authentication and members, outing/return, lateness, place search/recommendation/sync, reviews and reports, FCM notifications, Discord student council sync, schedulers, and operational monitoring.

## 2. Tech Stack

- Kotlin 2.2.21, Java 21, Spring Boot 4.0.2
- Gradle Groovy DSL, single Gradle module
- Spring MVC and some WebFlux/WebClient
- Spring Data JPA, MariaDB/MySQL, H2 test DB
- Spring Security, JWT, Redis
- Firebase Admin SDK, AWS S3 SDK, Spring Mail, Swagger/OpenAPI
- Testing: Kotest 6.1.3, MockK 1.14.9
- CI build: ./gradlew build --no-daemon

Re-verify versions and dependencies against build.gradle. Do not assume ktlint, Spotless, Detekt, Flyway, Liquibase, or any separate formatter is installed.

## 3. Package Structure

The base package is com.teamharibo.goms.

- domain/{domain}/controller: HTTP entry points
- domain/{domain}/dto/request, response: request/response Kotlin data classes
- domain/{domain}/entity: JPA entities
- domain/{domain}/repository: JPA Repositories and Redis Repositories
- domain/{domain}/service, service/impl: service interfaces and implementations
- domain/{domain}/exception: location for domain exceptions when needed
- domain/{domain}/job: scheduler and batch-style jobs
- domain/common: domain-shared types such as common enums
- global/config, discord, exception, jwt, log, security, util: global configuration and common infrastructure

Current domains include auth, discord, health, late, member, notification, outing, place, report, review, and studentcouncil. Check existing domain boundaries before adding a new package.

## 4. Layer Rules per Domain

A Controller handles only request conversion, validation, the authentication boundary, and response status codes. Move business decisions and data assembly to the Service. Follow the current pattern of separating the Service interface and ServiceImpl. A Repository handles only lookup, persistence, and query encapsulation, and holds no policy. An Entity holds persistent state and the minimal methods needed for domain state changes.

## 5. Controller Rules

- Survey the existing /api/v3/** paths and per-domain URLs first.
- Use constructor injection and declare immutable dependencies with val.
- Return ResponseEntity<T> or ResponseEntity<Void> as the existing code does.
- Match success status codes, noContent responses, and Header names with the existing APIs.
- Use @Valid for request bodies, and use @Validated for method parameter validation as the actual code does.
- For OpenAPI documentation, follow the existing @Tag, @Operation, @ApiResponses, @SecurityRequirement approach.
- Apply authentication and role restrictions after checking the Security configuration and existing Controller contracts.
- Do not bypass the GlobalException contract with ad hoc try/catch in a Controller.
- For APIs that need backward compatibility, preserve existing JSON fields, status codes, and Headers.

## 6. Service and ServiceImpl Rules

- Put public service contracts in an interface.
- Implementations use the ServiceImpl naming and constructor injection.
- Apply @Transactional(readOnly = true) to read-only services and @Transactional to state-changing services.
- Do not perform external API calls inside long-running DB transactions. Refer to the existing place sync case, which separates the collection phase from the DB write phase.
- Assemble DTOs explicitly in the service, and handle exceptions and authorization checks in a consistent order.
- For state transitions, concurrency, retries, and duplicate requests, check the existing Repository locking and test patterns first.

## 7. Repository Rules

- JPA Repositories follow JpaRepository and the current project's Optional/List return conventions.
- For complex queries, check how @Query, @Param, @Lock, and @Modifying are already used.
- When looking up user resources, always verify owner and role conditions in the query or the service.
- Redis access follows the existing StringRedisTemplate wrapper pattern under domain/{domain}/repository/redis.
- Reuse existing key prefix and TTL rules, and do not log raw Tokens or verification codes.

## 8. Entity Rules

- JPA Entities follow the current project's plain class and nullable Long id pattern.
- If an ID is nullable, verify the persisted state the existing way before use. Avoid Kotlin's !! in new code.
- Prefer val for immutable values, but follow the existing var pattern for fields that need JPA state changes.
- For nullable, unique, enum storage, columnDefinition, and relation fetch, check the actual Entity and the impact on production data.
- Choose deletion, state deactivation, and cascade carefully with production data preservation in mind.

## 9. Request and Response DTO Rules

- DTOs use Kotlin data classes. Do not use Java records or SDK response wrappers.
- Request validation annotations use @field:NotBlank, @field:Email, @field:Pattern, @field:NotNull, @field:Size, etc., as the actual code does.
- JSON field names follow the current camelCase contract.
- Put response DTOs in domain/{domain}/dto/response and expose only the fields the API contract needs.
- Do not unnecessarily include sensitive information such as passwords, JWTs, Refresh Tokens, email verification codes, and Firebase credentials in DTOs or logs.

## 10. Validation and Exception Handling

- Request format validation follows Jakarta Validation and the existing ErrorCode.INVALID_REQUEST contract.
- Business exceptions use GOMS's GlobalException and ErrorCode.
- Reuse existing ErrorCodes first, and add a new code only when needed, after reviewing the status code, message, and API compatibility.
- GlobalExceptionHandler handles GlobalException, invalid requests, and unexpected exceptions as ResponseEntity<ErrorResponse>.
- Distinguish authentication/authorization exceptions from domain business exceptions.
- Do not change the global exception contract with ad hoc responses or try/catch in a Controller.
- Consider that exception messages may be exposed externally, and do not put internal stack traces or secrets in responses.

## 11. Logging Rules

- Logs use GOMS's LogFormat.message(domain, event, fields...).
- As in most existing cases, domain is an uppercase English identifier such as AUTH or EXCEPTION, event is the Korean description used in the existing code, and field keys use camelCase such as memberId.
- Do not log emails, passwords, JWTs, Refresh Tokens, email verification codes, Firebase credentials, AWS/SMTP/Redis/DB secrets, Kakao keys, Discord tokens, or webhooks.
- For request and failure causes, check the existing RequestLogConstants and RequestLoggingFilter flow.
- Record only safe minimal information such as identifiers, states, and causes instead of full values.

## 12. Transaction, External API, Scheduler

- Distinguish transaction boundaries for read-only and state-changing work.
- Separate external API calls (WebClient/WebFlux or the current Client) from DB write transactions.
- For Scheduler/Job, check duplicate execution, re-execution on failure, and duplicate notifications, and delegate to the service.
- Handle external responses, timeouts, and partial failures according to GOMS ErrorCode and existing contracts.

## 13. Redis and Security/JWT

- In Redis, state the TTL, key prefix, and deletion timing explicitly, and preserve the Refresh Token and verification code revocation flow.
- JWT signing algorithm, access/refresh expiration, and Bearer Header parsing are based on JwtProvider.
- Do not bypass Spring Security's SecurityFilterChain, JwtAuthenticationFilter, entry point, and access denied handler.
- Verify all of the student and student council roles, the internal API secret, and user-owned resource checks.
- Inject JWT secret, Firebase, AWS, SMTP, Kakao, Discord, DB, and Redis credentials only from environment variables or secret files, and do not add real values to the repository.

## 14. Testing Rules

- Place tests under src/test/kotlin with the same domain structure.
- Use Kotest 6's DescribeSpec, the describe/context/it pattern, and shouldBe/shouldThrow according to current usage.
- Use MockK mockk/every/justRun/verify for external collaborators.
- Tests that need JPA use H2 and the current Spring test configuration.
- Controller contract tests follow the existing approach of standaloneSetup, GlobalExceptionHandler, Jackson converter, and status/jsonPath assertions.
- Add regression tests for business logic changes. For concurrency, transaction scope, authentication/authorization, and API contract changes, prioritize the related tests.
- Do not weaken assertions or delete tests to make tests pass.
- The run commands are ./gradlew test, ./gradlew compileKotlin test, and ./gradlew build --no-daemon. On Windows, use gradlew.bat.

## 15. GitHub Metadata, Commit, PR

- The base of regular development PRs is develop; only release PRs target main.
- Actual branch prefixes follow feat/, fix/, refactor/, test/, chore/, hotfix/, ci/, etc. This task uses chore/ai-harness.
- Commits follow formats such as feat:, fix:, refactor:, test:, chore:, docs:, ci: with a concise Korean description.
- Write PR titles in concise Korean without unnecessary prefixes.
- Keep .github/pull_request_template.md and check only the verifications actually performed.
- Use only Labels that actually exist; if unsure, do not add a label.
- Without a user request, do not commit, push, create a PR, or merge. For this task the user explicitly asked, so proceed through commit/push/PR creation, but do not merge, auto merge, or deploy.

### Before and After Creating an Issue

- Before creating, use `gh` to check for identical or similar Issues, recent title conventions, the repository's current label list, and existing assignees.
- Apply only labels that actually exist in the repository to an Issue. Do not auto-create new labels; usually select only 1-2 labels with close meaning.
- If the user specifies an assignee, follow it as is. If another assignee already exists, do not change or add one arbitrarily.
- Only when there is no assignee and the user explicitly says they will do the work may you assign the currently authenticated user. Do not arbitrarily assign anyone to a simple backlog Issue; if unsure, ask the user.
- After creating an Issue, query `title`, `assignees`, `labels`, and `state` again to confirm they match the intended metadata.

### Before and After Creating a PR

- Before creating, check the related Issue's existence, assignee, and labels, the PR title convention, the base branch, and PR labels.
- Fix the base of regular development PRs to `develop`, and use `main` only when the repository convention is clear, such as for deployment or release.
- A PR states the Issue it resolves and distinguishes it from merely referenced Issues. If there is no related Issue or the linking is unclear, do not make up a number.
- Apply only labels that actually exist to PRs as well, and do not create new labels. Usually limit required labels to 1-2.
- After creating a PR, query `title`, `baseRefName`, `headRefName`, `labels`, `closingIssuesReferences`, `isDraft`, and `assignees` again. If they differ from the intent, fix them within the allowed scope and re-verify.
- Do not create real test Issues or PRs to verify metadata.

### Writing the PR Body

- Keep the heading order and names of `.github/pull_request_template.md`, and do not arbitrarily change or remove the existing checklist wording or order.
- In the `관련 이슈` (Related issues) section, use `Closes #<issue-number>` for an Issue it resolves and `Related to #<issue-number>` for a mere association. Do not guess Issue numbers, and do not link a parent/tracking Issue with `Closes`.
- If one PR actually resolves multiple Issues, you may write `Closes #<issue-number>` on multiple lines.
- In `작업내용` (Changes), write only the key changes needed for review as short sentences or bullets; do not list every file or implementation detail.
- In `참고사항` (Notes), write only what is actually needed, such as operational impact, compatibility, special verification results, and important constraints or exclusions. If there is nothing, do not force content.
- Do not add headings not in the template by default, such as `Summary`, `Motivation`, `Background`, `Architecture`, `Implementation Details`, `Testing`, `Risks`, `Future Work`, `Files Changed`. Use additional checklists only when needed for actual verification.
- Before writing a PR, check the sentence length and bullet usage of the 3-5 most recently merged PRs, but the current template and this document take precedence.
- In the checklist and body, record only items actually checked and verifications actually run. Do not check or write about tests not run or CI results not confirmed.
- Match the `Closes` and `Related to` notation to the format readable by the Workflow (#102) that auto-closes Issues on develop merge.

### Scope of Responsibility

- Issue/PR metadata checks do not replace the reviewer auto-assignment Workflow (#93), the PR Template structure (#99), or the code comment/KDoc rules (#100).

## 16. Documentation and Language

AI harness instructions are written in English. Project documentation (README, docs/) and outputs stay in Korean. Keep values that are English by existing contract, such as code identifiers and log domain/field. Do not use Washer-specific SDK, ExpectedException, or response wrapper rules as GOMS rules.

### Code Comments and KDoc

- By default, do not add new general comments that repeat behavior the code already expresses. Avoid method/variable descriptions, step-by-step implementation notes, change history, AI work descriptions, and mechanical Given/When/Then comments. For example, do not write a comment like `// 회원을 조회한다.` ("Look up the member.") when the code right below reveals its meaning.
- Write short comments only when the reason is hard to see from the code alone, such as concurrency, lock order, transaction boundaries, security or external spec constraints, workarounds, non-obvious side effects, and fragile invariants. Explain why it must be kept rather than the implementation flow.
- Do not mandate KDoc on every public class/function. Add it briefly only where a caller could misread the code alone, such as call contracts, allowed states, important exceptions/side effects, concurrency/transaction constraints, and external integration contracts.
- Do not add mechanical KDoc that repeats the name to simple CRUD, private helpers, DTO properties, Entity fields, simple repository/controller forwarding, mappers, or getters/setters. Do not overuse `@param`/`@return` that are obvious from the name.
- Update existing comments only when a related code change makes them wrong, and do not delete or clean up unrelated comments. Write a TODO only when there is a real follow-up task and reason, following the existing Issue/TODO convention.
- This standard is the basis for judging code written from now on; selective KDoc reinforcement of existing code is handled separately in the scope of Issue #101.

## 17. Post-Change Verification

After harness changes, check JSON/TOML/YAML front matter/Shell syntax, core rules across documents, the .agents/skills and .claude/skills lists, forbidden keywords, and reference paths. Then run git diff --check, ./gradlew compileKotlin test, and ./gradlew build --no-daemon. If something fails, report whether it is caused by the harness change or is an existing application failure.

## 18. Things AI Must Not Do

- Do not create or modify .harness/** or .reviewbot/**.
- Do not arbitrarily change application production code, APIs, DB schema, environment variables, Security, CI/CD, or Gradle dependencies.
- Do not document unverified rules, nonexistent Gradle tasks, formatters, or migration tools.
- Do not delete existing user changes or hide them with stash/reset.
- Do not read, print, or commit real secrets.
- Do not run git reset --hard, git clean -fdx, force push, or broad deletions.
- Do not push directly to main/develop, merge PRs, auto merge, release, or deploy.
- Do not delete or weaken tests to hide failures, and do not commit unrelated files together.

## 19. Manual Harness Maintenance

- AGENTS.md is the single source of truth for project rules.
- The canonical copy of common skills is .agents/skills, and .claude/skills is the Claude-compatible entry.
- When modifying skills, run a contradiction check that the lists and decision rules in both locations match.
- Do not use automatic sync bots or file-overwrite sync features. For skill changes, a human reviews both locations and reference paths.
- In harness PRs, check required files, JSON/TOML/Shell syntax, forbidden keywords, and real Gradle task references in read-only mode.
