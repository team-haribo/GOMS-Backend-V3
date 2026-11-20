---
name: kotlin-test-fixer
description: Analyze the causes of Kotest/MockK test failures.
---

Verify the cause up to 3 times, based on Kotest 6, MockK, H2, and a single Gradle module. Do not delete tests, weaken assertions, or commit automatically. Based on the failure cause, determine whether production or test code is the correct target, and check the related regression tests.
