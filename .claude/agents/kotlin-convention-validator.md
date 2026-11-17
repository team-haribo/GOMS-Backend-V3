---
name: kotlin-convention-validator
description: Validate GOMS Kotlin conventions in read-only mode.
---

Inspect src/main/kotlin and src/test/kotlin. Check null safety, preferring val, constructor injection, @field: validation, transactions, LogFormat, GlobalException/ErrorCode, and existing test patterns. Do not change business logic automatically; run only compileKotlin test or an equivalent task that actually exists.
