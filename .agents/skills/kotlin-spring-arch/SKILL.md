---
name: kotlin-spring-arch
description: GOMS Kotlin/Spring 계층과 실제 코드 컨벤션을 검토한다.
---

# Kotlin Spring 아키텍처

## 발동 조건

Kotlin/Spring Controller, Service, Repository, Entity, DTO, security 또는 transaction을 추가·리뷰할 때 사용한다.

## 필요한 도구

rg, 관련 Kotlin 파일, build.gradle, 테스트와 Gradle compileKotlin test를 사용한다.

## 조사 단계

동일 도메인의 구현과 테스트를 먼저 읽고 생성자 주입, val/var, null safety, @field: validation, ResponseEntity, GlobalException, LogFormat, transaction 위치를 비교한다.

## 실행 단계

data class DTO, 생성자 주입, val 우선, Service/ServiceImpl 분리와 책임 분리를 따른다. 조회에는 readOnly transaction, write에는 transaction을 적용한다. 외부 API 호출을 장시간 transaction에 넣지 않는다. Coroutine/Reactive는 실제 동일 흐름이 있을 때만 사용한다.

## 검증 단계

compileKotlin과 관련 테스트를 실행하고 전체 build를 가능하면 실행한다. 새 formatter나 lint task를 가정하지 않는다.

## 중단 조건

기존 스타일이 서로 다르거나 API·DB·Security 경계가 바뀌는 판단이 필요하면 근거를 모아 사용자에게 보고한다.

## 금지 행동

!! 남용, 무단 전역 리팩터링, record/SDK wrapper/ExpectedException 도입, Controller에 비즈니스 로직 집중을 하지 않는다.

## 최종 보고

참조한 기존 패턴, 계층별 변경, transaction·null safety 판단, 테스트 결과를 보고한다.
