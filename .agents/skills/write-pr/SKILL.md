---
name: write-pr
description: GOMS PR 템플릿·base·라벨·검증 결과를 반영해 PR을 작성한다.
---

# Pull Request 작성

## 발동 조건

사용자가 PR 생성 또는 PR 본문 작성을 요청했을 때 사용한다.

## 필요한 도구

PR template, git diff/log, gh pr list/view/create, gh label list, Gradle 검증을 사용한다.

## 조사 단계

1. .github/pull_request_template.md와 최근 상세 PR을 읽는다.
2. 일반 개발 PR의 base가 develop인지 확인한다. main은 배포 PR에만 사용한다.
3. 실제 라벨·assignee·reviewer 관례를 확인한다.
4. 변경 파일과 검증 결과를 사실대로 정리한다.

## 실행 단계

간결한 한국어 제목과 기존 템플릿 구조를 유지한 본문을 작성한다. 작업내용, 참고사항, 체크리스트, 검증, 주요 구성, 제외 사항을 포함한다. 실제 수행한 항목만 체크한다.

## 검증 단계

JSON/TOML/shell/하네스 일관성, Gradle, git diff --check 결과를 본문에 기록하고 push 후 PR의 base/head, draft, assignee, label, 파일, CI를 다시 조회한다.

## 중단 조건

base·권한·변경 범위·라벨이 불명확하거나 관련 없는 파일이 섞였으면 PR을 만들지 않는다.

## 금지 행동

새 라벨 생성, main 대상 일반 PR, 실제로 안 한 검증 체크, merge/auto merge/release/deploy를 하지 않는다.

## 최종 보고

브랜치, commit, 제목, base/head, assignee/label, 검증, PR URL과 merge하지 않았음을 보고한다.
