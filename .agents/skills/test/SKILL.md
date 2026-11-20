---
name: test
description: Consistently performs GOMS verification with Kotest, MockK, H2, and Gradle.
---

# Test

## Trigger

Use for production logic changes, API contract changes, bug fixes, harness verification, or test failure analysis.

## Required Tools

Use Kotest 6.1.3, MockK 1.14.9, H2, the Gradle wrapper, and related sources and tests.

## Investigation Steps

Read the DescribeSpec, context/it, shouldBe/shouldThrow, and mockk/every/verify patterns and test fixtures in the same domain. For Controllers, check standaloneSetup and whether GlobalExceptionHandler is registered.

## Execution Steps

Add tests whose Given/When/Then meaning is clear. Isolate external collaborators with MockK, and use H2-based tests when JPA is needed. For service changes, consider success, failure, permission, and boundary regression tests.

## Verification Steps

Quick verification is ./gradlew compileKotlin test, and full verification is ./gradlew build --no-daemon. On Windows, use gradlew.bat. The report includes the total number of tests, passed/failed/skipped, failing tests, the first project-code stack frame, the cause, whether it is harness-related, and execution time.

## Stop Conditions

If tests that require environment, credentials, or external services cannot be run without a safe substitute, do not hide the failure; report it separately.

## Prohibited Actions

Do not delete tests, weaken assertions, or unconditionally assume that only one of production or test is correct.

## Final Report

Accurately report results per command, test totals, failure causes, whether related to the change, and verifications not run.
