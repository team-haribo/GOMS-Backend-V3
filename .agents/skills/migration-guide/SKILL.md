---
name: migration-guide
description: GOMS에 실제로 존재하는 DB migration 도구와 Entity 변경의 운영 영향을 확인한다.
---

# 마이그레이션 안내

## 발동 조건

Entity/column 변경, 운영 데이터 변환, 배포 전후 schema 확인이 필요할 때 사용한다.

## 필요한 도구

rg, build.gradle, settings.gradle, Gradle tasks, Docker 설정과 Entity/Repository를 사용한다.

## 조사 단계

Flyway, Liquibase, schema SQL과 CI task의 실제 존재를 확인한다. GOMS에 없는 도구는 문서와 명령에 쓰지 않는다.

## 실행 단계

도구가 없으면 migration 파일을 자동 생성하지 않고 Entity 변경, 기존 데이터 호환, MariaDB 검증, 롤백 가능성, 배포 전후 확인 항목을 안내한다.

## 검증 단계

./gradlew tasks --all, 관련 H2 테스트와 ./gradlew build --no-daemon을 사용한다.

## 중단 조건

운영 schema를 안전하게 바꿀 절차가 합의되지 않았거나 data loss 가능성이 있으면 구현을 멈춘다.

## 금지 행동

존재하지 않는 Flyway/Liquibase 명령, 운영 DB 직접 삭제·변경, rollback을 보장하지 않는 자동화를 추가하지 않는다.

## 최종 보고

실제 도구 유무, 변경 영향, 호환·롤백 확인, 실행한 task를 보고한다.
