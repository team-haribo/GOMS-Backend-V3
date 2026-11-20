# GOMS Backend V3 Copilot Instructions

The single source of truth for project rules is AGENTS.md at the root. This document summarizes only the essentials needed for code suggestions.

- This is a single-module Kotlin 2.2.21, Java 21, Spring Boot 4.0.2, Gradle Groovy DSL project.
- Keep the existing domain/{domain}/controller, dto/request/response, entity, repository, service/impl structure.
- Prefer constructor injection and val, and use Kotlin data classes for DTOs.
- For request validation annotations, use @field:NotBlank, @field:Email, @field:NotNull, etc., as the actual code does.
- Controllers follow the existing /api/v3/** paths and OpenAPI patterns and return ResponseEntity<T> or ResponseEntity<Void>.
- Put business logic in the Service; use @Transactional(readOnly = true) for reads and @Transactional for state changes.
- Reuse GlobalException and ErrorCode for exceptions, and do not bypass the GlobalExceptionHandler contract.
- Logs use LogFormat.message() and must not include emails, passwords, JWTs, Refresh Tokens, verification codes, or cloud credentials.
- Check a JPA Entity's nullable ID before use, and avoid !! in new code.
- Write tests with Kotest DescribeSpec and MockK the way the current project does, and add regression tests.
- Do not suggest SDK response wrappers, ExpectedException, Java records, Lombok rules, or Spotless/ktlint/Detekt tasks that GOMS does not have.
- Unless the request is for source code, API, DB, Security, CI/CD, or dependency changes, do not modify those areas.

For detailed rules, verification commands, and forbidden actions, read AGENTS.md.
