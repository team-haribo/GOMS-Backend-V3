#!/usr/bin/env bash
set -euo pipefail

base="${1:-develop}"
title="${2:?PR 제목이 필요합니다}"
body_file="${3:?PR 본문 파일이 필요합니다}"
gh pr create --base "$base" --title "$title" --body-file "$body_file"
