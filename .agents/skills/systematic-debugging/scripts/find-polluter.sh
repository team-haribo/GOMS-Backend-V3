#!/usr/bin/env bash
set -euo pipefail

pattern="${1:-}"
if [[ -z "$pattern" ]]; then
  echo "사용법: find-polluter.sh <테스트명 또는 패턴>" >&2
  exit 2
fi
./gradlew test --tests "$pattern" --no-daemon
