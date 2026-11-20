---
name: database-schema
description: Reviews GOMS JPA Entities, Repositories, data preservation, and impact on the production DB.
---

# Database Schema

## Trigger

Use when adding or changing an Entity, column, relation, index, Repository query, or deletion/state change.

## Required Tools

Use rg, Entity and Repository sources, build.gradle, docker-compose.yml, .env.example, H2 tests, and Gradle test.

## Investigation Steps

1. Check nullable, unique, enum, id, and relation fetch settings on JPA Entities.
2. Check Repository queries, locks, modifying queries, existing indexes, and callers.
3. Check production MariaDB/MySQL compatibility and docker volume preservation.
4. Check whether an actual migration tool such as Flyway/Liquibase exists.

## Execution Steps

Design the minimal change that fits the current schema management approach. If there is no separate migration tool, do not assume Flyway commands or migration files; document how the Entity change is applied in production, compatibility with existing data, and the rollback procedure. Prioritize user resource ownership and data preservation.

## Verification Steps

Run H2 tests, Repository query tests, schema validation if needed, and ./gradlew build --no-daemon. Check the SQL dialect and nullable/unique contracts.

## Stop Conditions

Stop code changes if there is no basis for deciding how to apply changes to the production DB, how to convert data, or the deletion policy.

## Prohibited Actions

Do not delete production volumes, perform broad data deletion, introduce non-existent migration tasks, or make unjustified cascade/fetch changes.

## Final Report

Report Entity/Repository impact, data preservation, whether a migration tool exists, tests, and rollback caveats.
