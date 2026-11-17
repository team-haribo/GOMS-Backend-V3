---
name: api-design
description: Reviews Controller, validation, security, and compatibility when designing or changing GOMS API contracts.
---

# API Design

## Trigger

Use when adding or changing a new endpoint, request/response DTO, status code, Header, authentication/authorization, or Swagger contract.

## Required Tools

Use rg, git diff, GitHub PR lookup, Gradle test, and, when possible, Controller contract test tooling.

## Investigation Steps

1. Read the existing /api/v3/** paths and Controllers of the same domain.
2. Check the ResponseEntity return type, success status code, Header, @Valid, and @field: validation.
3. Check SecurityConfig, the JWT filter, role restrictions, GlobalExceptionHandler, and ErrorCode.
4. Check the related request/response DTOs, tests, and Swagger annotations.

## Execution Steps

Assess backward compatibility first, and keep the Controller responsible only for conversion, validation, and delegation. Reuse existing ErrorCodes and authorization rules in the service, and return ResponseEntity<T> or ResponseEntity<Void> in the existing form. Do not use Washer's SDK automatic response wrapping or CommonApiResDto.

## Verification Steps

Test status codes, JSON fields, Headers, and per-role access, and run ./gradlew compileKotlin test. If the change affects the API contract, prefer standaloneSetup-based contract tests.

## Stop Conditions

If the existing contract conflicts with the new requirement, or there is no basis for deciding authorization or response backward compatibility, ask the user for a decision.

## Prohibited Actions

Do not change production APIs by guessing from documentation alone. Do not bypass global exception handling with try/catch in the Controller.

## Final Report

Report the affected endpoints, authentication/validation changes, status codes, compatibility assessment, and added tests with their results.
