---
name: security-checklist
description: Checks GOMS authentication, authorization, secrets, ownership, and external integration security risks.
---

# Security Checklist

## Trigger

Use when authentication/authorization, JWT/Refresh Token, internal APIs, environment variables, external URLs/Headers, logs, or sensitive data are being changed or reviewed.

## Required Tools

Use rg, SecurityConfig/JwtProvider/filter, Redis repositories, environment variable names, related tests, and git diff.

## Investigation Steps

Check JWT signing and expiration, Refresh Token storage and revocation, Spring Security roles, student/student council verification, internal API secrets, Firebase/AWS/SMTP/DB/Redis/Kakao/Discord credentials, logs, and user ownership.

## Execution Steps

Keep secrets injected via the environment, and validate inputs and external URLs/Headers. Apply ownership checks to user resource access. Do not leave tokens, passwords, or credentials in plain text in logs, responses, or test fixtures.

## Verification Steps

Verify authentication failure, expiration, insufficient role, ownership violation, invalid input, and missing secrets with related tests, and run ./gradlew test.

## Stop Conditions

If something can only be verified by reading a secret, or the permission model is unclear, do not print values and ask the user for confirmation.

## Prohibited Actions

Do not print or commit .env, private keys, Firebase JSON, or tokens. Do not block all endpoints without verification in the name of security.

## Final Report

Report the boundaries inspected, risks found and not found, modified files, verification that no secrets were exposed, and test results.
