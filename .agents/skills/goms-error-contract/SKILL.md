---
name: goms-error-contract
description: Reviews GOMS GlobalException, ErrorCode, HTTP status, and the existing API exception contract.
---

# GOMS Exception Contract

## Trigger

Use when adding or changing domain errors, authentication/authorization errors, validation, HTTP status, ErrorResponse, or exception tests.

## Required Tools

Use ErrorCode, GlobalException, GlobalExceptionHandler, ErrorResponse, Security handlers, and related Controllers, Services, and tests.

## Investigation Steps

Find existing ErrorCodes and check status/message, where exceptions are thrown, GlobalExceptionHandler mappings, and differences in the authentication/authorization handlers. Do not create duplicate codes with the same meaning.

## Execution Steps

Reuse an appropriate existing ErrorCode. If a new code is needed, assess its domain, HTTP status, message, whether it is safe to expose, and backward compatibility, and propagate it through GlobalException(ErrorCode). Ad hoc try/catch and ad hoc responses in Controllers are prohibited.

## Verification Steps

Test the HTTP status and body for normal, validation, authentication, authorization, business, and unexpected errors. Run ./gradlew test.

## Stop Conditions

Do not add a new ErrorCode without user confirmation if its meaning, status code, or message could break the existing client contract.

## Prohibited Actions

Do not introduce Washer ExpectedException, SDK response wrappers, exposure of internal stack traces or secrets, or weakened test assertions.

## Final Report

Report the reuse/new ErrorCode decision, exception paths, HTTP contract, tests, and compatibility risks.
