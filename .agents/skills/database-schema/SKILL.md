---
name: database-schema
description: GOMS JPA Entity, Repository, 데이터 보존과 운영 DB 영향성을 검토한다.
---

# 데이터베이스 스키마

## 발동 조건

Entity, column, relation, index, Repository query, 삭제·상태 변경을 추가·변경할 때 사용한다.

## 필요한 도구

rg, Entity·Repository 소스, build.gradle, docker-compose.yml, .env.example, H2 테스트, Gradle test를 사용한다.

## 조사 단계

1. JPA Entity의 nullable, unique, enum, id와 relation fetch를 확인한다.
2. Repository query, lock, modifying, 기존 index와 호출자를 확인한다.
3. 운영 MariaDB/MySQL 호환성과 docker volume 보존을 확인한다.
4. Flyway/Liquibase 등 실제 migration 도구가 있는지 확인한다.

## 실행 단계

현재 스키마 관리 방식에 맞춰 최소 변경을 설계한다. 별도 migration 도구가 없으면 Flyway 명령이나 migration 파일을 가정하지 않고, Entity 변경의 운영 반영·기존 데이터 호환·롤백 절차를 문서화한다. 사용자 자원 소유권과 데이터 보존을 우선한다.

## 검증 단계

H2 테스트, Repository query 테스트, 필요 시 schema validation을 실행하고 ./gradlew build --no-daemon을 실행한다. SQL dialect와 nullable/unique 계약을 확인한다.

## 중단 조건

운영 DB 반영 방법, 데이터 변환, 삭제 정책을 결정할 근거가 없으면 코드 변경을 멈춘다.

## 금지 행동

운영 volume 삭제, 광범위한 데이터 삭제, 존재하지 않는 migration task 도입, 무근거 cascade/fetch 변경을 하지 않는다.

## 최종 보고

Entity·Repository 영향, 데이터 보존, migration 도구 존재 여부, 테스트와 롤백 주의사항을 보고한다.
