---
name: systematic-debugging
description: Reproduces GOMS errors and verifies the root cause with a single hypothesis and a minimal fix.
---

# Systematic Debugging

## Trigger

Use when a test failure, runtime error, API contract mismatch, or concurrency/transaction problem occurs.

## Required Tools

Use Gradle test, failure logs, git log/diff, rg, H2 tests, and related external integration settings.

## Execution Steps

1. Reproduce the error with the same command.
2. From the exception and logs, identify the first project-code stack frame and the input/state.
3. Trace recent changes and the call and data flow.
4. Do not fix multiple causes at once; form a single hypothesis.
5. Add a minimal reproduction test or extend an existing test.
6. Fix the root cause with a minimal change.
7. Run the related tests and the full test suite.

## Verification Steps

Record each failure count and hypothesis, and run ./gradlew compileKotlin test and ./gradlew build --no-daemon to the extent possible.

## Stop Conditions

If the same hypothesis fails 3 times, do not keep modifying code blindly; re-examine the structure, environment, and test isolation. Stop any reproduction that requires secrets or production data.

## Prohibited Actions

Do not make speculative fixes that ignore logs, delete or weaken tests, manipulate the production DB directly, or do unrelated refactoring.

## Final Report

Report the reproduction command, observed error, single hypothesis, root cause, fix, test results, and remaining risks.
