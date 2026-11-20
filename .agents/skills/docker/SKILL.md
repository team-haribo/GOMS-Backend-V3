---
name: docker
description: Safely changes and verifies GOMS Docker Compose and the runtime environment.
---

# Docker

## Trigger

Use when changing docker-compose.yml, service ports/networks/volumes, .env.example, or container execution.

## Required Tools

Use rg and Get-Content for reading, and docker compose config for verification.

## Investigation Steps

Check port and environment variable names in docker-compose.yml, .env.example, .dockerignore, CI/CD, and application settings. Do not read or print actual secrets or the contents of the secrets directory.

## Execution Steps

Preserve the existing MariaDB/Redis services, ports, networks, and volumes, and use placeholders only. Record whether the change affects application behavior or production deployment.

## Verification Steps

Run docker compose config when possible and check git diff --check. Pull images or actually start services only when the user requests it.

## Stop Conditions

If required environment variables, production volumes, or the secret injection method are unclear, stop and report the decisions needed.

## Prohibited Actions

Do not commit real secrets, delete volumes broadly, run docker system prune, or deploy to production.

## Final Report

Report the changed services/ports/volumes, config verification, confirmation that no secrets are included, and production caveats.
