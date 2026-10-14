#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
if printf '%s' "$payload" | grep -Eiq '\.(kt|kts)([" ]|$)'; then echo "post-tool 검증 안내: Kotlin 변경 후 compileKotlin/test를 확인하세요." >&2; fi
if printf '%s' "$payload" | grep -Eiq '\.(json|toml|sh)([" ]|$)'; then echo "post-tool 검증 안내: 설정·shell 변경 후 문법 검증을 확인하세요." >&2; fi
exit 0
