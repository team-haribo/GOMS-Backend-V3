# GOMS Backend V3 Claude Code 지침

프로젝트 규칙의 기준은 저장소 루트의 AGENTS.md다. 이 파일은 Claude Code의 실행·도구 사용 지침만 보완하며 AGENTS.md와 충돌하면 AGENTS.md를 따른다.

## 작업 순서

1. 현재 경로, git status, branch, origin/develop과 관련 PR을 확인한다.
2. 변경 전에 관련 코드·테스트·PR 템플릿·프로젝트 문서를 읽는다.
3. 요청된 범위를 벗어나지 않고 최소 변경을 한다. 애플리케이션 동작 변경이 없는 하네스 작업에서는 src와 build.gradle을 수정하지 않는다.
4. 변경 후 diff를 자체 리뷰하고 문법·일관성·Gradle 검증을 실행한다.
5. 사용자가 명시한 경우에만 commit, push, PR 생성까지 진행한다. merge, auto merge, release, deploy는 하지 않는다.

## 도구 사용

- 읽기에는 rg, git diff, git log, gh를 우선 사용한다.
- 파일 수정은 기존 파일을 덮어쓰지 않도록 patch 방식으로 한다.
- secret, .env, Firebase credential, private key의 내용을 읽거나 출력하지 않는다.
- 위험 명령은 저장소의 .codex/hooks와 .claude/hooks가 차단한다. 차단을 우회하지 않는다.
- 새 dependency, formatter, plugin을 추가하지 않는다.
- 존재 여부를 확인하지 않은 Gradle task를 실행 지침에 쓰지 않는다.

## GOMS 핵심 규칙

DTO는 Kotlin data class와 @field: validation을 사용하고, Controller는 ResponseEntity를 반환한다. 예외는 GlobalException/ErrorCode 계약을 따르고, 로그는 LogFormat.message()를 사용한다. 조회에는 readOnly transaction, 상태 변경에는 transaction을 적용한다. 상세 규칙과 실제 예시는 AGENTS.md와 .agents/skills를 읽는다.

## 검증 기본값

가능하면 ./gradlew compileKotlin test와 ./gradlew build --no-daemon을 실행한다. Windows에서는 gradlew.bat을 사용한다. 테스트 실패를 성공으로 보고하지 말고 첫 번째 프로젝트 코드 stack frame과 원인을 기록한다.
