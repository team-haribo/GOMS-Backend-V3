---
name: migration-guide
description: Checks the DB migration tools that actually exist in GOMS and the production impact of Entity changes.
---

# Migration Guide

## Trigger

Use when Entity/column changes, production data conversion, or pre/post-deployment schema checks are needed.

## Required Tools

Use rg, build.gradle, settings.gradle, Gradle tasks, Docker settings, and Entities/Repositories.

## Investigation Steps

Check whether Flyway, Liquibase, schema SQL, and CI tasks actually exist. Do not write tools that GOMS does not have into documents or commands.

## Execution Steps

If there is no tool, do not auto-generate migration files; instead, provide guidance on the Entity change, compatibility with existing data, MariaDB verification, rollback feasibility, and pre/post-deployment check items.

## Verification Steps

Use ./gradlew tasks --all, related H2 tests, and ./gradlew build --no-daemon.

## Stop Conditions

Stop implementation if a procedure for safely changing the production schema has not been agreed upon or there is a possibility of data loss.

## Prohibited Actions

Do not add non-existent Flyway/Liquibase commands, direct deletion or modification of the production DB, or automation that does not guarantee rollback.

## Final Report

Report whether the tools actually exist, the impact of the change, compatibility/rollback checks, and the tasks run.
