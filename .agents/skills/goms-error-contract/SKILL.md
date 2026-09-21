---
name: goms-error-contract
description: GOMS GlobalException, ErrorCode, HTTP 상태와 기존 API 예외 계약을 검토한다.
---

# GOMS 예외 계약

## 발동 조건

도메인 오류, 인증·인가 오류, validation, HTTP 상태, ErrorResponse 또는 예외 테스트를 추가·변경할 때 사용한다.

## 필요한 도구

ErrorCode, GlobalException, GlobalExceptionHandler, ErrorResponse, Security handler, 관련 Controller·Service·테스트를 사용한다.

## 조사 단계

기존 ErrorCode를 찾고 status/message, 예외 위치, GlobalExceptionHandler 매핑, 인증·인가 handler의 차이를 확인한다. 같은 의미의 코드를 중복 생성하지 않는다.

## 실행 단계

적절한 기존 ErrorCode를 재사용한다. 새 코드가 필요하면 도메인·HTTP 상태·메시지·노출 가능성·하위 호환성을 판단하고 GlobalException(ErrorCode)을 통해 전파한다. Controller 임의 try/catch와 임의 response를 금지한다.

## 검증 단계

정상·validation·인증·인가·비즈니스·예상하지 못한 오류의 HTTP status와 body를 테스트한다. ./gradlew test를 실행한다.

## 중단 조건

새 ErrorCode의 의미·상태 코드·메시지가 기존 클라이언트 계약을 깨뜨릴 수 있으면 사용자 확인 없이는 추가하지 않는다.

## 금지 행동

Washer ExpectedException, SDK response wrapper, 내부 stack trace·secret 노출, 테스트 assertion 약화를 도입하지 않는다.

## 최종 보고

재사용·신규 ErrorCode 판단, 예외 경로, HTTP 계약, 테스트와 호환성 위험을 보고한다.
