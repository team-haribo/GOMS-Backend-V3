---
name: planning
description: Organizes unclear GOMS feature requirements into an impact scope and verification plan before implementation.
---

# Planning

## Trigger

Use when a feature requirement is ambiguous or affects multiple domains, permissions, the DB, or external integrations.

## Required Tools

Use source and test search, git log/PR lookup, and tools for checking API, schema, and operations documents.

## Investigation Steps

Check user flows, roles, API contracts, DB impact, concurrency, external integrations, failure handling, tests, and deployment/operations impact.

## Execution Steps

Create a short plan that separates confirmed facts from assumptions, and propose the minimal-change path within the existing architecture. Do not force unnecessary interviews for clear tasks.

## Verification Steps

Link each plan item to related code, tests, and verification commands, and compare against the actual results after implementation.

## Stop Conditions

Do not start implementation while conflicts that require a user decision, such as permissions, data preservation, or API compatibility, remain unresolved.

## Prohibited Actions

Do not document unverified requirements as facts or change production code or the DB during the planning phase.

## Final Report

Report confirmed facts, assumptions, impact scope, step-by-step tasks, verification, and remaining decisions.
