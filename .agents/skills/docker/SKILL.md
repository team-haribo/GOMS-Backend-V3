---
name: docker
description: GOMS Docker Compose와 실행환경을 안전하게 변경·검증한다.
---

# Docker

## 발동 조건

docker-compose.yml, 서비스 포트·network·volume, .env.example 또는 컨테이너 실행을 변경할 때 사용한다.

## 필요한 도구

읽기에는 rg와 Get-Content, 검증에는 docker compose config를 사용한다.

## 조사 단계

docker-compose.yml, .env.example, .dockerignore, CI/CD와 application 설정의 포트·환경변수 이름을 확인한다. 실제 secret과 secrets 디렉터리 내용은 읽거나 출력하지 않는다.

## 실행 단계

기존 MariaDB/Redis service, port, network, volume을 보존하고 placeholder만 사용한다. 변경이 애플리케이션 동작이나 운영 배포에 영향을 주는지 기록한다.

## 검증 단계

가능하면 docker compose config를 실행하고 git diff --check를 확인한다. 이미지 pull이나 실제 서비스 기동은 사용자가 요청했을 때만 한다.

## 중단 조건

필수 환경변수, 운영 volume, secret 주입 방식이 불명확하면 중단하고 필요한 결정을 보고한다.

## 금지 행동

실제 secret 커밋, 광범위한 volume 삭제, docker system prune, 운영 배포를 하지 않는다.

## 최종 보고

변경한 service·port·volume, config 검증, secret 비포함 여부와 운영 주의사항을 보고한다.
