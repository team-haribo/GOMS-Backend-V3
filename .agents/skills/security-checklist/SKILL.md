---
name: security-checklist
description: GOMS 인증·인가·secret·소유권·외부 연동 보안 위험을 점검한다.
---

# 보안 체크리스트

## 발동 조건

인증/인가, JWT·Refresh Token, 내부 API, 환경 변수, 외부 URL/Header, 로그 또는 민감 데이터가 변경·리뷰 대상일 때 사용한다.

## 필요한 도구

rg, SecurityConfig/JwtProvider/filter, Redis repository, 환경변수 이름, 관련 테스트와 git diff를 사용한다.

## 조사 단계

JWT 서명·만료, Refresh Token 저장·폐기, Spring Security 역할, 학생/학생회 검증, 내부 API secret, Firebase/AWS/SMTP/DB/Redis/Kakao/Discord credential, 로그와 사용자 소유권을 확인한다.

## 실행 단계

secret은 환경 주입으로 유지하고, 입력값과 외부 URL/Header를 검증한다. 사용자 자원 접근에는 ownership check를 적용한다. token·password·credential은 로그·응답·테스트 fixture에 원문을 남기지 않는다.

## 검증 단계

인증 실패, 만료, 역할 부족, 소유권 위반, 잘못된 입력, secret 누락을 관련 테스트로 확인하고 ./gradlew test를 실행한다.

## 중단 조건

secret을 읽어야만 확인할 수 있거나 권한 모델이 불명확하면 값을 출력하지 말고 사용자 확인을 요청한다.

## 금지 행동

.env·private key·Firebase JSON·token을 출력·커밋하지 않는다. 보안을 이유로 검증 없이 모든 endpoint를 차단하지 않는다.

## 최종 보고

검사한 경계, 발견·미발견 위험, 수정 파일, secret 비노출 검증과 테스트 결과를 보고한다.
