---
name: kotlin-spring-arch
description: Reviews GOMS Kotlin/Spring layering and actual code conventions.
---

# Kotlin Spring Architecture

## Trigger

Use when adding or reviewing Kotlin/Spring Controllers, Services, Repositories, Entities, DTOs, security, or transactions.

## Required Tools

Use rg, related Kotlin files, build.gradle, tests, and Gradle compileKotlin test.

## Investigation Steps

First read the implementation and tests of the same domain, and compare constructor injection, val/var, null safety, @field: validation, ResponseEntity, GlobalException, LogFormat, and transaction placement.

## Execution Steps

Follow data class DTOs, constructor injection, val first, Service/ServiceImpl separation, and separation of responsibilities. Apply readOnly transactions to reads and transactions to writes. Do not put external API calls inside long-running transactions. Use Coroutine/Reactive only when an equivalent flow actually exists.

## Verification Steps

Run compileKotlin and related tests, and run the full build when possible. Do not assume new formatter or lint tasks.

## Stop Conditions

If existing styles differ from each other or a decision changes API, DB, or Security boundaries, gather evidence and report to the user.

## Prohibited Actions

Do not overuse !!, perform unauthorized global refactoring, introduce record/SDK wrapper/ExpectedException, or concentrate business logic in Controllers.

## Final Report

Report the existing patterns referenced, changes per layer, transaction and null safety decisions, and test results.
