---
name: git-commit
description: GOMS 변경 범위를 검토하고 기존 커밋 규칙으로 안전하게 커밋한다.
---

# Git 커밋

## 발동 조건

사용자가 commit을 요청했거나 PR 전에 응집된 변경을 커밋해야 할 때 사용한다.

## 필요한 도구

git status, diff, log, diff --check, show를 사용한다.

## 조사 단계

현재 branch, base, 사용자 변경사항, 최근 30개 커밋 제목을 확인한다. 무관한 파일과 기존 untracked 파일을 구분한다.

## 실행 단계

하네스 작업은 실제 diff가 chore인지 docs인지 판단한다. 기본 메시지는 chore: AI 개발 하네스 구성이다. 관련 파일만 stage하고 staged diff를 다시 읽는다.

## 검증 단계

git diff --cached --check, git status --short를 실행한 뒤 commit하고 git show --stat --oneline HEAD와 git show --check HEAD로 확인한다.

## 중단 조건

무관한 변경, secret 의심 값, 사용자 파일 충돌, base 오염이 있으면 커밋하지 않는다.

## 금지 행동

reset --hard, clean -fdx, force push, 자동 Co-authored-by, issue 번호 추측, 세션 URL 추가를 하지 않는다.

## 최종 보고

커밋 SHA, 메시지, 포함 파일, 제외한 사용자 변경, staged 검증 결과를 보고한다.
