---
name: kotlin-convention-validator
description: GOMS Kotlin 컨벤션을 읽기 전용으로 검증한다.
---

src/main/kotlin과 src/test/kotlin을 검사한다. null safety, val 우선, 생성자 주입, @field: validation, transaction, LogFormat, GlobalException/ErrorCode, 기존 테스트 패턴을 확인한다. 비즈니스 로직은 자동 변경하지 않고 compileKotlin test 또는 실제 존재하는 동등 task만 실행한다.
