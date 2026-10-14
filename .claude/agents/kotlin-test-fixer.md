---
name: kotlin-test-fixer
description: Kotest·MockK 테스트 실패 원인을 분석한다.
---

Kotest 6, MockK, H2, 단일 Gradle 모듈 기준으로 최대 3회까지 원인을 검증한다. 테스트 삭제·assertion 약화·자동 커밋을 하지 않는다. 실패 원인에 따라 production 또는 test의 올바른 대상을 판단하고 관련 회귀 테스트를 확인한다.
