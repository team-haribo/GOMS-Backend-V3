---
name: test
description: GOMS의 Kotest, MockK, H2와 Gradle 검증을 일관되게 수행한다.
---

# 테스트

## 발동 조건

프로덕션 로직 변경, API 계약 변경, 버그 수정, 하네스 검증 또는 테스트 실패 분석 시 사용한다.

## 필요한 도구

Kotest 6.1.3, MockK 1.14.9, H2, Gradle wrapper, 관련 소스·테스트를 사용한다.

## 조사 단계

같은 도메인의 DescribeSpec, context/it, shouldBe/shouldThrow, mockk/every/verify 패턴과 테스트 fixture를 읽는다. Controller는 standaloneSetup과 GlobalExceptionHandler 등록 여부를 확인한다.

## 실행 단계

Given/When/Then 의미가 분명한 테스트를 추가한다. 외부 협력자는 MockK로 격리하고 JPA가 필요한 경우 H2 기반 테스트를 사용한다. 서비스 수정에는 성공·실패·권한·경계 회귀 테스트를 검토한다.

## 검증 단계

빠른 검증은 ./gradlew compileKotlin test, 전체 검증은 ./gradlew build --no-daemon이다. Windows에서는 gradlew.bat을 사용한다. 보고에는 전체 테스트 수, 통과/실패/스킵, 실패 테스트, 첫 프로젝트 코드 stack frame, 원인, 하네스 관련 여부, 실행 시간을 포함한다.

## 중단 조건

환경·credential·외부 서비스가 필요한 테스트를 안전한 대체 없이 실행할 수 없으면 실패를 숨기지 않고 구분해 보고한다.

## 금지 행동

테스트를 삭제하거나 assertion을 약화하거나 무조건 production/test 중 하나만 옳다고 가정하지 않는다.

## 최종 보고

실행 명령별 결과와 테스트 집계, 실패 원인, 관련 변경 여부, 미실행 검증을 정확히 보고한다.
