---
name: api-design
description: GOMS API 계약을 설계하거나 변경할 때 Controller, validation, security, compatibility를 검토한다.
---

# API 설계

## 발동 조건

새 endpoint, request/response DTO, 상태 코드, Header, 인증·인가, Swagger 계약을 추가·변경할 때 사용한다.

## 필요한 도구

rg, git diff, GitHub PR 조회, Gradle test와 가능하면 Controller 계약 테스트 도구를 사용한다.

## 조사 단계

1. 기존 /api/v3/** 경로와 같은 도메인의 Controller를 읽는다.
2. ResponseEntity 반환 타입, 성공 상태 코드, Header, @Valid와 @field: validation을 확인한다.
3. SecurityConfig, JWT filter, 역할 제한, GlobalExceptionHandler와 ErrorCode를 확인한다.
4. 관련 request/response DTO와 테스트·Swagger annotation을 확인한다.

## 실행 단계

하위 호환성을 먼저 판단하고, Controller는 변환·검증·위임만 담당하게 한다. 서비스에 기존 ErrorCode와 권한 규칙을 재사용하고 ResponseEntity<T> 또는 ResponseEntity<Void>를 기존 형태로 반환한다. Washer의 SDK 자동 응답 래핑이나 CommonApiResDto를 사용하지 않는다.

## 검증 단계

상태 코드·JSON 필드·Header·역할별 접근을 테스트하고 ./gradlew compileKotlin test를 실행한다. 변경이 API 계약에 영향을 주면 standaloneSetup 기반 계약 테스트를 우선한다.

## 중단 조건

기존 계약과 새 요구가 충돌하거나 권한·응답 하위 호환성을 결정할 근거가 없으면 사용자 판단을 요청한다.

## 금지 행동

프로덕션 API를 문서만 보고 추측해 변경하지 않는다. 전역 예외를 Controller try/catch로 우회하지 않는다.

## 최종 보고

영향 endpoint, 인증·검증 변화, 상태 코드, 호환성 판단, 추가 테스트와 실행 결과를 보고한다.
