#!/usr/bin/env bash
set -euo pipefail
pattern="${1:-}"
if [[ -z "$pattern" ]]; then echo "A test pattern is required" >&2; exit 2; fi
./gradlew test --tests "$pattern" --no-daemon
