#!/usr/bin/env bash
set -euo pipefail

pattern="${1:-}"
if [[ -z "$pattern" ]]; then
  echo "Usage: find-polluter.sh <test name or pattern>" >&2
  exit 2
fi
./gradlew test --tests "$pattern" --no-daemon
