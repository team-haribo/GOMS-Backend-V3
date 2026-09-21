#!/usr/bin/env bash
set -euo pipefail
pattern="${1:-}"
if [[ -z "$pattern" ]]; then echo "테스트 패턴이 필요합니다" >&2; exit 2; fi
./gradlew test --tests "$pattern" --no-daemon
